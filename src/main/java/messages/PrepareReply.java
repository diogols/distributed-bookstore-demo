package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Reply message informing if a process is prepared to commit a transaction. 
 */
public class PrepareReply implements CatalystSerializable {
    private int transaction_id;
    private boolean success;

    public PrepareReply() {}

    public PrepareReply(int transaction_id, boolean success) {
        this.success = success;
        this.transaction_id = transaction_id;
    }

    public boolean getSuccess() {
        return success;
    }

    public int getTransactionId() {
        return transaction_id;
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
