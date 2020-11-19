package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Request message to start a transaction. 
 */
public class TransactionRequest implements CatalystSerializable {
    public TransactionRequest() {}

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
    }
}
