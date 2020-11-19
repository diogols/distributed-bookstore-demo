package client;

import bank.RemoteBank;
import bookstore.RemoteBookstore;
import bookstore.RemoteCart;
import bookstore.Utility;
import io.atomix.catalyst.transport.Address;

/**
 * API to be used by a client for managing a bookstore.
 */
public class BookstoreAPI {
    private Utility u;
    private RemoteBookstore rbs;
    private RemoteBank rb;
    private RemoteCart rc;
    private int bank_account;

    
    /**
     * Creates a new BookstoreAPI.
     */
    public BookstoreAPI() {
        u = new Utility();
        rbs =  (RemoteBookstore) u.importObject(new Address(":1024"), 0, RemoteBookstore.class, null);
        rb = (RemoteBank) u.importObject(new Address(":1025"), 0, RemoteBank.class, null);
        
        try {
            rc = rbs.newCart();     //Creates new Cart for the Bookstore
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Search the bookstore for a book.
     * 
     * @param title Book title
     * @return the book ISBN
     */
    public String searchBook(String title) {
        
        try {
            return rbs.search(title);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }

    
    /**
     * Add the book to the cart.
     * 
     * @param isbn Book ISBN
     */
    public void addBook(String isbn) {
        
        try {
            rc.addBook(isbn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Do the checkout and buy the books on the cart.
     * With the given account identification, also removes the total value
     * of the cart from the account.
     * 
     * @param bank_account account identification
     * @return success of the operation
     */
    public boolean buyBooks(int bank_account) {
        
        this.bank_account = bank_account;
        
        return rc.buyBooks(bank_account);
    }

    
    /**
     * Get the books added to the cart.
     * 
     * @return book history from the cart.
     */
    public String getPurchases() {
        return rc.getHistory().toString();
    }

    
    /**
     * Get the bank account transfers.
     * 
     * @return bank account transfer history.
     */
    public String getTransfers() {
        return rb.getHistory(bank_account).toString();
    }

}
