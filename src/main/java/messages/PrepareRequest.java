package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Request message to commit a given transaction. 
 */
public class PrepareRequest implements CatalystSerializable {
    private int transaction_id;
    public PrepareRequest() {}
    public PrepareRequest(int transaction_id) {
        this.transaction_id = transaction_id;
    }

    public int getTransactionId() {
        return transaction_id;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(transaction_id);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transaction_id = bufferInput.readInt();
    }
}
