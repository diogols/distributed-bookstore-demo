package bookstore;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;
import io.atomix.catalyst.transport.Address;

public class ObjectReference implements CatalystSerializable {
    public Address address;
    public int object_reference;
    public String class_name;

    public ObjectReference() {}

    public Address getAddress() {
        return address;
    }

    public int getObjectReference() {
        return object_reference;
    }

    public String getClassName() {
        return class_name;
    }

    public ObjectReference(Address address, int object_reference, String class_name) {
        this.address = address;
        this.object_reference = object_reference;
        this.object_reference = object_reference;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(object_reference);
        serializer.writeObject(address, bufferOutput);
        bufferOutput.writeString(class_name);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        object_reference = bufferInput.readInt();
        serializer.readObject(bufferInput);
        class_name = bufferInput.readString();
    }
}
