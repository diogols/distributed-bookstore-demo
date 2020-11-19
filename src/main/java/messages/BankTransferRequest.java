package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Request message for a bank transfer between two accounts, given an amount.
 */
public class BankTransferRequest implements CatalystSerializable {
    private int from;
    private int to;
    private float quantity;
    private int transaciton_id;

    public BankTransferRequest() {}

    public BankTransferRequest(int from, int to, float quantity, int transaciton_id) {
        this.from = from;
        this.to = to;
        this.quantity = quantity;
        this.transaciton_id = transaciton_id;
    }

    public int getTransactionId() {
        return transaciton_id;
    }

    public int getFromAccount() {
        return from;
    }


    public int getToAccount() {
        return to;
    }

    public float getQuantity() {
        return quantity;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(from);
        bufferOutput.writeInt(to);
        bufferOutput.writeFloat(quantity);
        bufferOutput.writeInt(transaciton_id);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        from = bufferInput.readInt();
        to = bufferInput.readInt();
        quantity = bufferInput.readFloat();
        transaciton_id = bufferInput.readInt();
    }
}
