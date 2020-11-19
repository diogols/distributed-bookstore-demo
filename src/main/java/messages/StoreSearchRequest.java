package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Request message for searching a book on a store. 
 */
public class StoreSearchRequest implements CatalystSerializable {
    public int store_id;
    public String book_title;

    public StoreSearchRequest() {}

    public StoreSearchRequest(int store_id, String book_title) {
        this.store_id = store_id;
        this.book_title = book_title;
    }

    public int getStoreId() {
        return store_id;
    }

    public String getBookTitle() {
        return book_title;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(store_id);
        bufferOutput.writeString(book_title);

    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        store_id = bufferInput.readInt();
        book_title = bufferInput.readString();
    }
}
