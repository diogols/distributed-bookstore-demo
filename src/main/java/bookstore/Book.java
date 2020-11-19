package bookstore;

public class Book {
    private final String isbn, title, author;
    private final float price;

    public Book(String isbn, String title, String author, float price) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public String getISBN() {
        return isbn;
    }
    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }
    public float getPrice() { return price;}

}
