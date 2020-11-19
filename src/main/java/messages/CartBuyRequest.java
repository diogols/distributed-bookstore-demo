package messages;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;


/**
 * Request message for buying a cart content, given the bank account
 * identification for extracting the total amount.
 */
public class CartBuyRequest implements CatalystSerializable {
    private int transaction_id;
    private int cart_id;
    private int debit_card_info;

    public CartBuyRequest() {}

    public CartBuyRequest(int transaction_id, int cart_id, int debit_card_info) {
        this.transaction_id = transaction_id;
        this.cart_id = cart_id;
        this.debit_card_info = debit_card_info;
    }
    public int getTransactionId() {
        return transaction_id;
    }
    public int getCartId() {
        return cart_id;
    }
    public int getDebitCardInfo() {
        return debit_card_info;
    }

    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(transaction_id);
        bufferOutput.writeInt(cart_id);
        bufferOutput.writeInt(debit_card_info);
    }

    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        transaction_id = bufferInput.readInt();
        cart_id = bufferInput.readInt();
        debit_card_info = bufferInput.readInt();
    }
}
