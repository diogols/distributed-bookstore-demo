package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Reply message to the buy a cart content request message.
 */
public class CartBuyReply implements CatalystSerializable {
    boolean result;

    public CartBuyReply() {}

    public CartBuyReply(boolean result) {
        this.result = result;
    }

    public boolean getResult() {
        return result;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeBoolean(result);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        result = bufferInput.readBoolean();
    }
}
