package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Request message for creating a cart on a given store. 
 */
public class StoreMakeCartRequest implements CatalystSerializable {
    public int store_id;

    public StoreMakeCartRequest() {}

    public StoreMakeCartRequest(int store_id) {
        this.store_id = store_id;
    }

    public int getStoreId() {
        return store_id;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(store_id);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        store_id = bufferInput.readInt();
    }
}
