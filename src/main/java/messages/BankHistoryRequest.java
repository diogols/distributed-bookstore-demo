package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Request message for access to the bank history.
 */
public class BankHistoryRequest implements CatalystSerializable {
    private int id;

    public BankHistoryRequest() {}

    public BankHistoryRequest(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(id);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        id = bufferInput.readInt();
    }
}
