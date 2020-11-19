package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;


/**
 * Reply message to the participate on the Two Phase Commit request message.
 * It is sent by the coodinator.
 */
public class ParticipateReply implements CatalystSerializable {
    private boolean success;

    public ParticipateReply() {}

    public ParticipateReply(boolean success) {
        this.success = success;
    }

    public boolean getResult() {
        return success;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeBoolean(success);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        success = bufferInput.readBoolean();
    }
}
