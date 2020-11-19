package bank;

import java.util.HashMap;
import java.util.Map;

/**
 * Bank class emulates a database of accounts and allows transfer from an account 
 * to another account, it also allows to rollback an account state. 
 */
public class Bank {
    private Map<Integer, Account> accounts;

    public Bank() {
        accounts = new HashMap<>();
        staticPopulate();
    }

    public boolean transfer(int from, int to, float quantity) {

        boolean success = false;
        if (accounts.get(from).debit(quantity)) {
            accounts.get(to).credit(quantity);
            success = true;
        }
        return success;
    }

    public void rollbackAccountState(Account a) {
        accounts.get(a.getId()).rollbackState(a);
    }

    public Account getAccountState(int id) {
        return accounts.get(id);
    }

    private void staticPopulate() {
        accounts.put(0, new Account(0,0.0f));
        accounts.put(1, new Account(1,90.0f));
        accounts.put(2, new Account(2,15.0f));
    }
}
