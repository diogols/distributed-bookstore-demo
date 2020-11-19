package messages;

import bookstore.Book;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.List;

/**
 * Reply message to the access the cart history request message.
 */
public class CartHistoryReply implements CatalystSerializable {
    private List<String> books;

    public CartHistoryReply() {}

    public CartHistoryReply(List<String> books)
    {
        this.books = books;
    }
    public List<String> getBooks() {
        return books;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
       serializer.writeObject(books, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        books = serializer.readObject(bufferInput);
    }
}
