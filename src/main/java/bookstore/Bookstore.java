package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Bookstore class emulates a database of books and allows to get and search books.
 * Cart class is also present here and allows the creation of a new cart,
 * add books to a cart, check the total price and buy all the contents. 
 */
public class Bookstore {
    private Map<String, Book> books = new HashMap<>();
    //private Map<String, Integer> stock = new HashMap<>();

    public Bookstore() {
        // emulate database

        // populate with books
        books.put("1a", new Book("1a", "one", "someone", 15));
        books.put("2b", new Book("2b", "other", "someother", 25));
        books.put("3c", new Book("3c", "thing", "something", 500));
    }

    public Book get(String isbn) {
        return books.get(isbn);
    }

    public Book search(String title) {
        for(Book b: books.values())
            if (b.getTitle().equals(title))
                return b;
        return null;
    }

    public Cart createCart() {
        return new Cart();
    }

    public class Cart {
        private List<String> content;
        private float price;

        public Cart() {
            content = new ArrayList();
            price = 0;
        }

        public List<String> getBooks() {
            return content;
        }

        public void setBooks(List<String> books) {
            content = books;
        }

        public float price() {
            return price;
        }

        public List<String> get() {
            return content;
        }

        public void add(String isbn) {
            content.add(isbn);
            price += books.get(isbn).getPrice();
        }

        public boolean buy() {
            boolean r = false;
            if (content.size() > 0) {
                content = new ArrayList<>();
                r = true;
            }
            return r;
        }
    }
}
