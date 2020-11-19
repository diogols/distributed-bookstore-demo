package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Reply message to the Two Phase Commit Protocol starting request.
 */
public class CommitReply implements CatalystSerializable {
    boolean commit;

    public CommitReply() {}
    public CommitReply(boolean commit) {
        this.commit = commit;
    }

    public boolean getSuccess() {
        return commit;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeBoolean(commit);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        commit = bufferInput.readBoolean();
    }
}
