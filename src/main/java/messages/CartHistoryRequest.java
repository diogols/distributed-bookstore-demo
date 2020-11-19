package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Request message for accessing the cart history on a given cart.
 */
public class CartHistoryRequest implements CatalystSerializable {
    private int cart_id;

    public CartHistoryRequest() {}

    public CartHistoryRequest(int cart_id) {
        this.cart_id = cart_id;
    }
    public int getCartId() {
        return cart_id;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(cart_id);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        cart_id = bufferInput.readInt();
    }
}
