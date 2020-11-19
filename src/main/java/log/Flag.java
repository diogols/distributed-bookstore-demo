package log;

/**
 * Generic class to flip and set a boolean.
 */
public class Flag {
    private boolean flag;

    public Flag() {}

    public Flag(boolean flag) {
        this.flag = flag;
    }

    public synchronized void flip() {
        flag ^= true;
    }

    public synchronized boolean get() {
        return flag;
    }

    public synchronized void set(boolean newValue) {
        flag = newValue;
    }
}
