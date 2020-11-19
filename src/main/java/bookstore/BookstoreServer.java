package bookstore;

import coordinator.CoordinatorService;
import log.*;
import messages.*;
import io.atomix.catalyst.concurrent.Futures;
import io.atomix.catalyst.concurrent.SingleThreadContext;
import io.atomix.catalyst.concurrent.ThreadContext;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;
import io.atomix.catalyst.transport.Transport;
import io.atomix.catalyst.transport.netty.NettyTransport;
import bank.RemoteBank;
import pt.haslab.ekit.Log;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * BookstoreServer is the main class of the entity bookstore, it 
 * communicates with the entities bank and coordinator and it also
 * waits for client operations.
 * It listens to various requests via handlers and 
 * participates in the two phase commit protocol.
 */
public class BookstoreServer {
    private static final int bank_account = 0;
    private static Log l = null;

    public static void main(String[] args) {
        Utility u = new Utility();
        int bookstore_id = u.exportObject(new Bookstore());
        RemoteBank rb = (RemoteBank) u.importObject(new Address(":1025"), 0, RemoteBank.class, null);
        Map<Integer, CartBuyOperation> transactions = new HashMap<>();
        Map<Integer, CartBuyOperation> history = new HashMap<>();
        Flag committed = new Flag(true);
        Flag before_prepare = new Flag();
        File tf = new File("log");
        tf.mkdir();
        String path = tf.getAbsolutePath() + "\\bookstore";
        Log log = new Log(path);
        ReentrantLock lock = new ReentrantLock();
        Address a = new Address(":1024");
        Transport t = new NettyTransport();
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

        tc.execute(() -> log.handler(CartBuyOperation.class, (k, v) -> {
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
            before_prepare.flip();
            committed.flip();
        })).join();

        try {
            l = tc.execute(() -> log.open()).join().get();
        } catch (InterruptedException |ExecutionException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // if went down right after a prepare it should resend prepare reply to coordinator
        if (!committed.get()) {
            // the last transaction was possibly not prepared correctly
            int transaction_id = Collections.max(transactions.entrySet(),
                    Comparator.comparingInt(Map.Entry::getKey)).getKey();
           

            CartBuyOperation cbo = transactions.get(transaction_id);
            Bookstore.Cart cart = (((Bookstore) u.get(bookstore_id)).createCart());
            System.out.println(cbo.getBooks());
            // restore cart state in case the operation buy was executed
            cart.setBooks(cbo.getBooks());
            u.put(cbo.getCartId(), cart);
            // sent prepare reply
            CoordinatorService.prepared(cbo.getTransactionId(), cart.buy());
        // if i went down right before a prepare was received then coordinator aborted
        } else if(before_prepare.get()) {
            // due to the fact that the transaction aborted or will abort on timeout i only have
            // to maintain the log consistency
            // notice that the operation requested was not made, so there are no changes to the cart
            toLog(new PreparedMarker());
            toLog(new AbortedMarker());
        }

        tc.execute(() -> {
            t.server().listen(a, (c) -> {
                c.handler(StoreSearchRequest.class, (m) -> {
                    Bookstore s = (Bookstore) u.get(m.getStoreId());
                    Book b = s.search(m.getBookTitle());
                    return Futures.completedFuture(new StoreSearchReply(b.getISBN()));
                });

                c.handler(StoreMakeCartRequest.class, (m) -> {
                    Bookstore s = (Bookstore) u.get(m.getStoreId());
                    int cart_id = u.exportObject(s.createCart());
                    return Futures.completedFuture(new StoreMakeCartReply(cart_id));
                });

                c.handler(CartAddRequest.class, (m) -> {
                    Bookstore.Cart cart = (Bookstore.Cart) u.get(m.getCartId());
                    cart.add(m.getISBN());
                    return Futures.completedFuture(new CartAddReply());
                });

                c.handler(CartBuyRequest.class, (m) -> {
                    boolean success;
                    int cart_id = m.getCartId();
                    Bookstore.Cart cart = (Bookstore.Cart) u.get(cart_id);
                    int transaction_id = m.getTransactionId();
                    success = CoordinatorService.participate(transaction_id, a.host(), a.port());
                    if (success && (success = rb.transfer(m.getDebitCardInfo(), bank_account, cart.price(), transaction_id))) {
                        transactions.put(transaction_id, new CartBuyOperation(transaction_id, cart_id, cart.getBooks()));
                        // lock.lock();
                    }
                    return Futures.completedFuture(new CartBuyReply(success));
                });

                c.handler(PrepareRequest.class, (m) -> {
                    int transaction_id = m.getTransactionId();
                    CartBuyOperation cbo = transactions.get(transaction_id);
                    Bookstore.Cart cart = (Bookstore.Cart) u.get(cbo.getCartId());
                    toLog(cbo);
                    // debug crash before prepare
                    // System.exit(2);
                    toLog(new PreparedMarker());
                    // debug crash after prepare
                    // System.exit(2);
                    CoordinatorService.prepared(transaction_id, cart.buy());
                });

                c.handler(Commit.class, (m) -> {
                    int transaction_id = m.getTransactionId();
                    if (m.getCommit()) {
                        toLog(new CommittedMarker(transaction_id));
                        history.put(transaction_id, transactions.remove(transaction_id));
                    } else {
                        CartBuyOperation cbo = transactions.get(transaction_id);
                        ((Bookstore.Cart) u.get(cbo.getCartId())).setBooks(cbo.getBooks());
                        toLog(new AbortedMarker(transaction_id));
                    }
                    transactions.remove(transaction_id);
                    // lock.unlock();
                });

                c.handler(CartHistoryRequest.class, (m) -> {
                    List<String> l = new ArrayList<>();
                    for(CartBuyOperation cbo : history.values()) {
                        if(cbo.getCartId() == m.getCartId()) {
                            l.addAll(cbo.getBooks());
                        }
                    }
                    return Futures.completedFuture(new CartHistoryReply(l));
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





