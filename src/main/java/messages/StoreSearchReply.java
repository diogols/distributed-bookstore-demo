package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Reply message for searching a book on a store.
 * If found returns the ISBN of the book.
 */
public class StoreSearchReply implements CatalystSerializable {
    public String isbn;

    public StoreSearchReply() {}

    public StoreSearchReply(String isbn) {
        this.isbn = isbn;
    }

    public String getISBN() {
        return isbn;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeString(isbn);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        isbn = bufferInput.readString();
    }
}
