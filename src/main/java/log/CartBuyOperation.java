package log;

import bookstore.Bookstore;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.List;

/**
 * Log marker informing a cart buy operation.
 */
public class CartBuyOperation implements CatalystSerializable {
    private int transaction_id;
    private int cart_id;
    private List<String> books;

    public CartBuyOperation(int transaction_id, int cart_id, List<String> books) {
        this.transaction_id = transaction_id;
        this.cart_id = cart_id;
        this.books = books;
    }

    public CartBuyOperation() {}

    public int getTransactionId() {
        return transaction_id;
    }

    public int getCartId() {
        return cart_id;
    }

    public List<String> getBooks() {
        return books;
    }


    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(transaction_id);
        bufferOutput.writeInt(cart_id);
        serializer.writeObject(books, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transaction_id = bufferInput.readInt();
        cart_id = bufferInput.readInt();
        books = serializer.readObject(bufferInput);
    }
}
