package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Reply message to the start a transaction request.
 */
public class TransactionReply implements CatalystSerializable {
    int transaction_id;

    public TransactionReply() {}
    public TransactionReply(int transaction_id) {
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
