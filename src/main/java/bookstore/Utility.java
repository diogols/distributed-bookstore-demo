package bookstore;

import bank.RemoteBank;
import io.atomix.catalyst.transport.Address;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class Utility {
    Map<Integer, Object> objects = new HashMap<>();
    AtomicInteger id = new AtomicInteger(0);

    public int exportObject(Object o) {
        int r = id.getAndIncrement();
        objects.put(r, o);
        return r;
    }

    public Object get(int id) {
        return objects.get(id);
    }

    public Object put(int id, Object object) {
        return objects.put(id, object);
    }

    public Object importObject(Address a, int id, Class class_name, Object[] args) {
        Object r = null;
        try {
            switch (class_name.getSimpleName()) {
                case "RemoteCart" : r = new RemoteCart(a, (int) args[0], id);
                break;
                case "RemoteBookstore" : r = new RemoteBookstore(a, id);
                break;
                case "RemoteBank" : r = new RemoteBank(a, id);
                break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return r;
    }
}
