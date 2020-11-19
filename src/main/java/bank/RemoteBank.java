package bank;

import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyConnection;
import io.atomix.catalyst.transport.netty.NettyTransport;
import log.*;
import messages.*;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * RemoteBank is a stub that makes the client and bookstore
 * operations with the system more transparent.
 * It allows the client get his payment history and the bookstore 
 * perform the transfer operation.
 */
public class RemoteBank {
    private final Address a;
    private final SingleThreadContext tc;
    private Connection c;
    private final Transport t;

    public RemoteBank(Address a, int id) {
        this.a=a;
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
        try {
            c = tc.execute(() ->
                    t.client().connect(a)
            ).join().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

    public boolean transfer(int from, int to, float quantity, int transaction_id) {
        try {
            if(c==null) {
                c = tc.execute(() ->
                        t.client().connect(a)
                ).join().get();
            }

            BankTransferReply btr = (BankTransferReply) tc.execute(() ->
                    c.sendAndReceive(new BankTransferRequest(from, to, quantity, transaction_id))
            ).join().get();
            return btr.getResult();
        } catch (InterruptedException | ExecutionException e) {
            return false;
        }
    }

    public List<String> getHistory(int from) {
        BankHistoryReply bhr = null;
        try {
            bhr = (BankHistoryReply) tc.execute(() ->
                    c.sendAndReceive(new BankHistoryRequest(from))
            ).join().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return bhr.getHistory();
    }


}
