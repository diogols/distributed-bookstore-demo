package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;


/**
 * Request message that starts the Two Phase Commit Protocol.
 * It is sent by the client to the other participants.
 */
public class CommitRequest implements CatalystSerializable {
    private int transaction_id;
    private boolean success;

    public CommitRequest() {}

    public CommitRequest(int transaction_id, boolean success) {
        this.transaction_id = transaction_id;
        this.success = success;
    }

    public int getTransactionId() {
        return transaction_id;
    }

    public boolean getSuccess() {
        return success;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(transaction_id);
        bufferOutput.writeBoolean(success);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transaction_id = bufferInput.readInt();
        success = bufferInput.readBoolean();
    }
}
