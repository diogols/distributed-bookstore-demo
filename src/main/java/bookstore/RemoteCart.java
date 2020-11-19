package bookstore;

import coordinator.CoordinatorService;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import io.netty.handler.timeout.TimeoutException;
import log.*;
import messages.*;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.transport.Connection;

import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * RemoteCart is a stub that makes the client 
 * operations with the system more transparent 
 * it allows the client to buy and add books to a cart.
 */
public class RemoteCart {
    private final Address a;
    private final SingleThreadContext tc;
    private final Connection c;
    private final Transport t;
    private final int store_id;
    private final int cart_id;

    public RemoteCart(Address a, int store_id, int cart_id) throws InterruptedException, ExecutionException {
        this.store_id = store_id;
        this.cart_id = cart_id;
        this.a = a;
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
    }

    public void addBook(String isbn) throws InterruptedException, ExecutionException {
        CartAddReply car = (CartAddReply) tc.execute(() ->
                c.sendAndReceive(new CartAddRequest(store_id, cart_id, isbn))
        ).join().get();
    }

    public boolean buyBooks(int bank_account) {
        try {
            int transaction_id = CoordinatorService.transaction();
            CartBuyReply cbr = (CartBuyReply) tc.execute(() ->
                    c.sendAndReceive(new CartBuyRequest(transaction_id, cart_id, bank_account))
            ).join().get();
            return CoordinatorService.commit(transaction_id, cbr.getResult());
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<String> getHistory() {
        CartHistoryReply chr = null;
        try {
            chr = (CartHistoryReply) tc.execute(() ->
                    c.sendAndReceive(new CartHistoryRequest(cart_id))
            ).join().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return chr.getBooks();
    }
}
