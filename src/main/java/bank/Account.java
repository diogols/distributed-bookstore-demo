package bank;

import io.atomix.catalyst.buffer.BufferInput;
import io.atomix.catalyst.buffer.BufferOutput;
import io.atomix.catalyst.serializer.CatalystSerializable;
import io.atomix.catalyst.serializer.Serializer;

/**
 * Simple class with the details of a bank account.
 */
public class Account implements CatalystSerializable {
    private int id;         //Account identification
    private float balance;  //Current account balance

    /**
     * Create a new Account.
     */
    public Account() {}

    
    /**
     * Create a new Account.
     * 
     * @param id Account identification
     * @param balance Account balance
     */
    public Account(int id, float balance) {
        this.id = id;
        this.balance = balance;
    }
    
    /**
     * Debit a certain amount from the account.
     * 
     * @param quantity Amount to be debited
     * @return a boolean informing the success of the operation
     */
    public boolean debit(float quantity) {
        boolean success = false;
        if(balance - quantity >= 0) {
            balance -= quantity;
            success = true;
        }
        return success;
    }

    /**
     * Credit a certain amount to an account.
     * 
     * @param quantity Amount to be credited
     */
    public void credit(float quantity) {
        balance += quantity;
    }

    /**
     * Return the Account identification.
     * 
     * @return id of the Account
     */
    public int getId() {
         return id;
    }

   /**
     * Sets the account to a given state.
     * 
     * @param a State to be set
     */
    public void rollbackState(Account a) {
        id = a.id;
        balance = a.balance;
    }

    
    /**
     * Writes the instance variables to a BufferOutput.
     */
    @Override
    public void writeObject(BufferOutput<?> bufferOutput, Serializer serializer) {
        bufferOutput.writeInt(id);
        bufferOutput.writeFloat(balance);
    }

    /**
     * Reads the instance variables form a BufferInput.
     */
    @Override
    public void readObject(BufferInput<?> bufferInput, Serializer serializer) {
        id = bufferInput.readInt();
        balance = bufferInput.readFloat();
    }
}
