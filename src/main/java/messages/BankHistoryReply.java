package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

import java.util.List;

/**
 * Reply message to the access bank history request message.
 */
public class BankHistoryReply implements CatalystSerializable {
    private List<String> history;

    public BankHistoryReply() {}

    public BankHistoryReply(List<String> history)
    {
        this.history = history;
    }
    public List<String> getHistory() {
        return history;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
       serializer.writeObject(history, bufferOutput);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        history = serializer.readObject(bufferInput);
    }
}
