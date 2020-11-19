package bank;

import bookstore.Utility;
import coordinator.CoordinatorService;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import log.*;
import messages.*;
import pt.haslab.ekit.Log;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BankServer is the main class of the entity bank, it allows 
 * access to purchases and a history of all the payments that have occured
 * to a client. It listens to various requests via handlers and 
 * participates in the two phase commit protocol.
 */
public class BankServer {
    private static Map<Integer, BankTransferOperation> history;
    private static Log l;
    private static Map<Integer, BankTransferOperation> transactions = new HashMap<>();

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        Utility u = new Utility();
        int bank_id = u.exportObject(new Bank());

        history = new HashMap<>();
        Address a = new Address(":1025");
        Transport t = new NettyTransport();
        ReentrantLock lock = new ReentrantLock();
        Flag committed = new Flag(true);
        Flag before_prepare = new Flag();
        File tf = new File("log");
        tf.mkdir();
        String path = tf.getAbsolutePath() + "\\bank";
        Log log = new Log(path);

        ThreadContext tc = new SingleThreadContext("server-%d", new Serializer());
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

        tc.execute(() -> log.handler(BankTransferOperation.class, (k, v) -> {
            before_prepare.set(true);
            transactions.put(v.getTransactionId(), v);
        })).join();

        tc.execute(() -> log.handler(CommittedMarker.class, (k, v) -> {
            committed.flip();
            history.put(v.getTransactionId(), transactions.remove(v.getTransactionId()));
        })).join();

        tc.execute(() -> log.handler(AbortedMarker.class, (k, v) ->{
            committed.flip();
            transactions.remove(v.getTransactionId());
        })).join();

        tc.execute(() -> log.handler(PreparedMarker.class, (k, v) -> {
            committed.flip();
            before_prepare.flip();
        })).join();

        l = tc.execute(() -> log.open()).join().get();

        if (!committed.get()) {
            Bank b = (Bank) u.get(bank_id);
            int transaction_id = Collections.max(transactions.entrySet(),
                    Comparator.comparingInt(Map.Entry::getKey)).getKey();

            BankTransferOperation bto = transactions.get(transaction_id);
            b.rollbackAccountState(bto.getFromAccount());
            b.rollbackAccountState(bto.getToAccount());
            CoordinatorService.prepared(bto.getTransactionId(), b.transfer(bto.getFromId(), bto.getToId(), bto.getQuantity()));
        }  else if(before_prepare.get()) {
            // due to the fact that the transaction aborted or will abort on timeout i only have
            // to maintain the log consistency
            // notice that the operation requested was not made, so there are no changes to the cart
            toLog(new PreparedMarker());
            toLog(new AbortedMarker());
        }

        tc.execute(() -> {
            t.server().listen(a, (c) -> {
                c.handler(BankTransferRequest.class, (m) -> {
                    int transaction_id = m.getTransactionId();
                    boolean success = CoordinatorService.participate(transaction_id, a.host(), a.port());
                    if (success) {
                        Bank b = (Bank) u.get(bank_id);
                        transactions.put(transaction_id,
                                new BankTransferOperation(transaction_id,
                                b.getAccountState(m.getFromAccount()), b.getAccountState(m.getToAccount()), m.getQuantity()));
                        // lock.lock();
                    }
                    return Futures.completedFuture(new BankTransferReply(success));
                });

                c.handler(PrepareRequest.class, (m) -> {
                    int transaction_id = m.getTransactionId();
                    BankTransferOperation bto = transactions.get(transaction_id);
                    toLog(bto);
                    // debug crash before prepare
                    // System.exit(2);
                    toLog(new PreparedMarker(transaction_id));
                    // debug crash after prepare
                    // System.exit(2);
                    Bank b = (Bank) u.get(bank_id);
                    CoordinatorService.prepared(transaction_id, b.transfer(bto.getFromId(), bto.getToId(), bto.getQuantity()));
                });

                c.handler(Commit.class, (m) -> {
                    int transaction_id = m.getTransactionId();
                    if (m.getCommit()) {
                        toLog(new CommittedMarker(transaction_id));
                        history.put(transaction_id, transactions.get(transaction_id));
                    } else {
                        
                        Bank b = (Bank) u.get(bank_id);
                        BankTransferOperation bto = transactions.get(transaction_id);
                        b.rollbackAccountState(bto.getFromAccount());
                        b.rollbackAccountState(bto.getToAccount());
                        toLog(new AbortedMarker(transaction_id));
                    }
                    transactions.remove(transaction_id);
                    // lock.unlock();
                });

                c.handler(BankHistoryRequest.class, (m) -> {
                    List<String> l = new ArrayList<>();
                    for(BankTransferOperation bto : history.values()) {
                        if(bto.getFromId() == m.getId()) {
                            l.add(bto.toString());
                        }
                    }
                    return Futures.completedFuture(new BankHistoryReply(l));
                });
            });
        });
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
