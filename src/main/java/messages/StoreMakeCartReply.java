package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Reply message to the create a cart on a store request message.
 */
public class StoreMakeCartReply implements CatalystSerializable {
    private int cart_id;

    public StoreMakeCartReply() {}

    public int getCartId() {
        return cart_id;
    }

    public StoreMakeCartReply(int object_reference) {
        this.cart_id = object_reference;
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
