package log;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import messages.Commit;

/**
 * Log marker informing that a transaction is committed.
 */
public class CommittedMarker implements CatalystSerializable{

    int transaction_id;

    public CommittedMarker() {

    }

    public CommittedMarker(int transaction_id) {
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
