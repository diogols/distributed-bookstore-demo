package log;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Log marker informing that a participant is prepared to commit a transaction.
 */
public class PreparedMarker implements CatalystSerializable {
    private int transaction_id;

    public PreparedMarker() {

    }

    public PreparedMarker(int transaction_id) {
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
