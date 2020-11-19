package log;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Log marker informing a participant operation.
 */
public class ParticipantOperation implements CatalystSerializable {
    private int transaction_id;
    private String host;
    private int port;
    public ParticipantOperation() {}

    public ParticipantOperation(int transaction_id, String host, int port) {
        this.transaction_id = transaction_id;
        this.host = host;
        this.port = port;
    }

    public int getTransactionId() {
        return transaction_id;
    }

    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }
    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(transaction_id);
        bufferOutput.writeString(host);
        bufferOutput.writeInt(port);
    }
    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transaction_id = bufferInput.readInt();
        host = bufferInput.readString();
        port = bufferInput.readInt();
    }
}
