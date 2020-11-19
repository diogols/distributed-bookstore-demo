package log;

import bank.Account;
import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Log marker informing a bank transfer operation.
 */
public class BankTransferOperation implements CatalystSerializable {
    private int transaction_id;
    private Account from;
    private Account to;
    private float quantity;

    public BankTransferOperation() {
    }

    public BankTransferOperation(int transaction_id, Account from, Account to, float quantity) {
        this.transaction_id = transaction_id;
        this.from = from;
        this.to = to;
        this.quantity = quantity;
    }

    public int getTransactionId() {
        return transaction_id;
    }

    public Account getFromAccount() {
        return from;
    }

    public Account getToAccount() {
        return to;
    }

    public float getQuantity() {
        return quantity;
    }

    public int getFromId() {
        return from.getId();
    }

    public int getToId() {
        return to.getId();
    }

    @Override
    public String toString() {
        return "from: " + from.getId() + " to: " + to.getId() + " quantity: " + quantity;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(transaction_id);
        serializer.writeObject(from, bufferOutput);
        serializer.writeObject(to, bufferOutput);
        bufferOutput.writeFloat(quantity);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transaction_id = bufferInput.readInt();
        from = serializer.readObject(bufferInput);
        to = serializer.readObject(bufferInput);
        quantity = bufferInput.readFloat();
        System.out.println(from.getId());
        System.out.println(to.getId());
    }
}
