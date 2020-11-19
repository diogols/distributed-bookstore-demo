package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;

/**
 * Request message to participate on the Two Phase Commit Protocol.
 * Informs the coordinator if the participant that sent it is ready or not.
 */
public class ParticipateRequest implements CatalystSerializable {
    private int transaction_id;
    private String host;
    private int port;
    public ParticipateRequest() {}

    public ParticipateRequest(int transaction_id, String host, int port) {
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
