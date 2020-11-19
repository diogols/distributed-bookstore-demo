package bookstore;

import log.*;
import messages.*;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Connection;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;

import java.util.concurrent.ExecutionException;

/**
 * RemoteBookstore is a stub that makes the client 
 * operations with the system more transparent.
 * It allows the client to search for a book and create a new cart.
 */
public class RemoteBookstore {
    private final Address a;
    private final SingleThreadContext tc;
    private Connection c;
    private final Utility u;
    private final int id;
    public RemoteBookstore(Address a, int id) {
        this.id = id;
        this.a =  a;
        u = new Utility();
        Transport t = new NettyTransport();

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

    public String search(String book_title) throws Exception {
        StoreSearchReply ssr = (StoreSearchReply) tc.execute(() ->
                c.sendAndReceive(new StoreSearchRequest(id, book_title))
        ).join().get();
        return ssr.getISBN();
    }

    public RemoteCart newCart() throws Exception {
        StoreMakeCartReply smcr = (StoreMakeCartReply) tc.execute(() ->
                c.sendAndReceive(new StoreMakeCartRequest())
        ).join().get();
        return (RemoteCart) u.importObject(a, smcr.getCartId(), RemoteCart.class, new Object[]{id});
    }

}
