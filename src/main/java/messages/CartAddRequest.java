package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Request message for adding a book to a client cart, given a specific bookstore.
 */
public class CartAddRequest implements CatalystSerializable {
    private int store_id;
    private int cart_id;
    private String isbn;

    public CartAddRequest() {}

    public CartAddRequest(int store_id, int cart_id, String isbn) {
        this.store_id = store_id;
        this.cart_id = cart_id;
        this.isbn = isbn;
    }

    public int getStoreId() {
        return store_id;
    }

    public int getCartId() {
        return cart_id;
    }

    public String getISBN() {
        return isbn;
    }


    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(store_id);
        bufferOutput.writeInt(cart_id);
        bufferOutput.writeString(isbn);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        store_id = bufferInput.readInt();
        cart_id = bufferInput.readInt();
        isbn = bufferInput.readString();
    }
}
