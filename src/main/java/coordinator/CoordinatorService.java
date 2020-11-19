package coordinator;

import io.atomix.catalyst.concurrent.BlockingFuture;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import log.*;
import messages.*;

import java.util.concurrent.ExecutionException;

/**
 * CoordinatorService provides the participants and the coordinator 
 * the tools to implement the two phase commit protocol.
 */
public abstract class CoordinatorService {
    private static Address a = new Address("localhost:1026");
    private static SingleThreadContext tc;
    private static Connection c;
    private static Transport t;

    private static void connect() {
        try {
            t = new NettyTransport();
            tc = new SingleThreadContext("client-%d", new Serializer());

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

            c = tc.execute(() ->
                    t.client().connect(a)
            ).join().get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static int transaction() {
        connect();
        TransactionReply tr = null;
        try {
            tr = (TransactionReply) tc.execute(() ->
                    c.sendAndReceive(new TransactionRequest())
            ).join().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return tr.getTransactionId();
    }

    public static boolean commit(int transaction_id, boolean success) {
        connect();
        CommitReply br = null;
        try {
            br = (CommitReply) tc.execute(() ->
                    c.sendAndReceive(new CommitRequest(transaction_id, success))
             ).join().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            br = retry(5555);
        }
        return br.getSuccess();
    }

    public static boolean participate(int transaction_id, String host, int port) {
        connect();
        try {
            ParticipateReply pr = (ParticipateReply) tc.execute(() ->
                    c.sendAndReceive(new ParticipateRequest(transaction_id, host, port))
            ).join().get();
            return pr.getResult();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void prepared(int transaction_id, boolean success) {
        connect();
        try {
            c = tc.execute(() ->
                    t.client().connect(a)
            ).join().get();

            tc.execute(() ->
                    c.send(new PrepareReply(transaction_id, success))
            ).join().get();

        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public static CommitReply retry(int port) {
        BlockingFuture cf = new BlockingFuture();
        ThreadContext ntc = new SingleThreadContext("client-%d", new Serializer());
        ntc.execute(() -> {

            t.server().listen(new Address("localhost", port), (c) -> {

                c.handler(CommitReply.class, (m) -> {
                    cf.complete(m);
                });
            });
        });
        CommitReply cr = null;
        try {
            cr = (CommitReply) cf.get();
            return cr;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return cr;
    }
}