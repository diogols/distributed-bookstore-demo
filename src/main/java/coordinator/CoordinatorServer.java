package coordinator;

import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.Scheduled;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import log.*;
import messages.*;
import pt.haslab.ekit.Log;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * CoordinatorServer is the main class of the entity coordinator,
 * it coordinates the two phase commit protocol between the resources 
 * bank and bookstore to guarantee atomicity between the operations.
 */
public class CoordinatorServer {
    private static final int TIMEOUT = 500;
    private static Address a = new Address(":1026");
    private static Transport t = new NettyTransport();
    private static Map<Integer, List<Connection>> participants = new ConcurrentHashMap<>();
    private static Map<Integer, AtomicInteger> remaining = new ConcurrentHashMap<>();
    private static Map<Integer, Boolean> commit = new ConcurrentHashMap<>();
    private static Map<Integer, Scheduled> schedules = new ConcurrentHashMap<>();
    private static Map<Integer, CompletableFuture> clients = new ConcurrentHashMap<>();
    private static AtomicInteger id = new AtomicInteger();
    private static ThreadContext tc = new SingleThreadContext("server-%d", new Serializer());
    private static ThreadContext ptc = new SingleThreadContext("server-%d", new Serializer());
    private static Log l;

    public static void main(String[] args) throws InterruptedException, ExecutionException {
        tc.serializer().register(BankHistoryReply.class);
        tc.serializer().register(BankHistoryRequest.class);
        tc.serializer().register(BankTransferReply.class);
        tc.serializer().register(BankTransferRequest.class);
        tc.serializer().register(CartAddReply.class);
        tc.serializer().register(CartAddRequest.class);
        tc.serializer().register(CartBuyReply.class);
        tc.serializer().register(CartBuyRequest.class);
        tc.serializer().register(CartHistoryReply.class);
        tc.serializer().register(CartHistoryRequest.class);
        tc.serializer().register(Commit.class);
        tc.serializer().register(CommitReply.class);
        tc.serializer().register(CommitRequest.class);
        tc.serializer().register(ParticipateReply.class);
        tc.serializer().register(ParticipateRequest.class);
        tc.serializer().register(PrepareReply.class);
        tc.serializer().register(PrepareRequest.class);
        tc.serializer().register(StoreMakeCartReply.class);
        tc.serializer().register(StoreMakeCartRequest.class);
        tc.serializer().register(StoreSearchReply.class);
        tc.serializer().register(StoreSearchRequest.class);
        tc.serializer().register(TransactionReply.class);
        tc.serializer().register(TransactionRequest.class);
        tc.serializer().register(AbortedMarker.class);
        tc.serializer().register(BankTransferOperation.class);
        tc.serializer().register(CartBuyOperation.class);
        tc.serializer().register(CommittedMarker.class);
        tc.serializer().register(ParticipantOperation.class);
        tc.serializer().register(PreparedMarker.class);

        File tf = new File("log");
        tf.mkdir();
        String path = tf.getAbsolutePath() + "\\coordinator";
        Log log = new Log(path);
        // if server shut after prepare
        Flag prepared = new Flag();
        // if server shut while registering participants
        Flag before_prepare = new Flag();

        tc.execute(() -> log.handler(ParticipantOperation.class, (k, v) -> {
            before_prepare.set(true);
            if (!participants.containsKey(v.getTransactionId())) {
                participants.put(v.getTransactionId(), new ArrayList<>());
            }
            try {
                participants.get(v.getTransactionId()).add(ptc.execute(() -> t.client().connect(new Address(v.getHost(), v.getPort()))).join().get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        })).join();

        tc.execute(() -> log.handler(CommittedMarker.class, (k, v) -> {
            prepared.flip();
            participants.remove(v.getTransactionId());
        })).join();

        tc.execute(() -> log.handler(AbortedMarker.class, (k, v) -> {
            prepared.flip();
            participants.remove(v.getTransactionId());
        })).join();

        tc.execute(() -> log.handler(PreparedMarker.class, (k, v) -> {
            before_prepare.set(false);
            prepared.flip();
            id.set(v.getTransactionId());
        })).join();

        l = tc.execute(() -> log.open()).join().get();

        // if the last state was prepared but not committed then the server needs to restore the previous state
        // this means that it has to re-send prepare requests
        if (prepared.get()) {
            int transaction_id = id.get();

            // restore collections
            commit.put(transaction_id, true);
            remaining.put(transaction_id, new AtomicInteger(participants.get(transaction_id).size()));

            // send prepare request to participants
            for (Connection res : participants.get(transaction_id)) {
                ptc.execute(() ->
                        res.send(new PrepareRequest(transaction_id))
                ).join();
            }
            // in case of timeout it should abort the transaction
            schedules.put(transaction_id, tc.schedule(Duration.ofMillis(TIMEOUT), () -> {
                for (Connection participant : participants.get(transaction_id)) {
                    ptc.execute(() ->
                            participant.send(new Commit(transaction_id, false))
                    ).join();
                }
                toLog(new AbortedMarker(transaction_id));
            }));
            // in case of previous participants sent participate request
            // but server didn't prepared, then it needs to abort
            // due to the fact that the server may have lost participate requests
        } else if (before_prepare.get()) {
            // get last transaction id
            int transaction_id = Collections.max(participants.entrySet(),
                    Comparator.comparingInt(Map.Entry::getKey)).getKey();
            // send abort
            commit.put(transaction_id, false);
            for (Connection participant : participants.get(transaction_id)) {
                ptc.execute(() ->
                        participant.send(new Commit(transaction_id, false))
                ).join();
            }
            l.append(new PreparedMarker());
            l.append(new AbortedMarker());
        }

        tc.execute(() -> {
            t.server().listen(a, (c) -> {
                // client request for transaction
                c.handler(TransactionRequest.class, (m) -> {
                    int transaction_id = id.incrementAndGet();
                    participants.put(transaction_id, new ArrayList<>());
                    commit.put(transaction_id, true);
                    return Futures.completedFuture(new TransactionReply(transaction_id));
                });

                // client request for commit
                c.handler(CommitRequest.class, (m) -> {
                    CompletableFuture cf = new CompletableFuture();
                    int transaction_id = m.getTransactionId();
                    clients.put(transaction_id, cf);
                    remaining.put(transaction_id, new AtomicInteger(participants.get(transaction_id).size()));

                    // debug crash before prepare
                    // try { main(args); } catch (Exception e) {}
                    toLog(new PreparedMarker(transaction_id));
                    // debug crash after prepare
                    // try { main(args); } catch (Exception e) {}

                    if(m.getSuccess()) {
                        for (Connection res : participants.get(transaction_id)) {
                            ptc.execute(() ->
                                    // first phase: prepare
                                    res.send(new PrepareRequest(transaction_id))
                            ).join();
                        }
                    }
                    // on timeout abort first phase
                    schedules.put(transaction_id, tc.schedule(Duration.ofMillis(TIMEOUT), () -> {
                        for (Connection res : participants.get(transaction_id)) {
                            ptc.execute(() ->
                                    // first phase: prepare
                                    res.send(new Commit(transaction_id, false))
                            ).join();
                            cf.complete(new CommitReply(false));
                            remaining.remove(transaction_id);
                            schedules.remove(transaction_id);
                        }
                        toLog(new AbortedMarker(transaction_id));
                    }));
                    return cf;
                });

                // resource request to participate on transaction
                c.handler(ParticipateRequest.class, (m) -> {
                    try {
                        int transaction_id = m.getTransactionId();
                        toLog(new ParticipantOperation(transaction_id, m.getHost(), m.getPort()));
                        Connection participant = ptc.execute(() -> t.client().connect(new Address(m.getHost(), m.getPort()))).join().get();
                        participants.get(transaction_id).add(participant);
                        return Futures.completedFuture(new ParticipateReply(true));
                    } catch (InterruptedException | ExecutionException | NullPointerException e) {
                        e.printStackTrace();
                        return Futures.completedFuture(new ParticipateReply(false));
                    }
                });

                // resource replies to first phase request from manager
                c.handler(PrepareReply.class, (m) -> {
                    int transaction_id = m.getTransactionId();

                    try {
                        commit.compute(transaction_id, (k, v) -> v &= m.getSuccess());
                        if (isLastParticipant(transaction_id)) {
                            schedules.get(transaction_id).cancel();
                            boolean success = commit.get(transaction_id);
                            for (Connection participants : participants.get(transaction_id)) {
                                ptc.execute(() ->
                                        participants.send(new Commit(transaction_id, success))
                                ).join();
                            }
                            if(clients.containsKey(transaction_id)) {
                                clients.get(transaction_id).complete(new CommitReply(success));
                            }
                            if(success) {
                                toLog(new CommittedMarker(transaction_id));
                            } else {
                                toLog(new AbortedMarker(transaction_id));
                            }

                            schedules.remove(m.getTransactionId());
                            remaining.remove(m.getTransactionId());
                        }
                    } catch (NullPointerException npe) {
                       System.out.println("TODO: send abort to participant");
                    }
                });
            });
        });
    }

    private static boolean isLastParticipant(int transaction_id) {
        return 0 == remaining.get(transaction_id).decrementAndGet();
    }

    private static void toLog(Object o) {
        try {
            l.append(o).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
