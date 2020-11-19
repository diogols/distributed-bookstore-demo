package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * The message that the coordinator sends to the participants informing
 * them that they can commit.
 */
public class Commit implements CatalystSerializable {
    private int transaction_id;
    private boolean commit;

    public Commit() {}
    public Commit(int transaction_id, boolean commit) {
        this.transaction_id = transaction_id;
        this.commit = commit;
    }


    public int getTransactionId() {
        return transaction_id;
    }

    public boolean getCommit() {
        return commit;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(transaction_id);
        bufferOutput.writeBoolean(commit);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transaction_id = bufferInput.readInt();
        commit = bufferInput.readBoolean();
    }
}
