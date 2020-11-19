package client;


public class DebugClient {

    public static void main(String[] args) throws Exception {

        BookstoreAPI c = new BookstoreAPI();
        
        c.addBook(c.searchBook("one"));
        c.addBook(c.searchBook("other"));
        c.addBook(c.searchBook("one"));

        System.out.println("buy result: " + c.buyBooks(1));
        System.out.println("bookstore history: " + c.getPurchases());
        System.out.println("bank history: " + c.getTransfers());
        
    }
}
