public class SynchronizedCounter {

    private int counter;

    public SynchronizedCounter(int counter) {
        this.counter = counter;
    }

    public SynchronizedCounter() {
        this(0);
    }

    public synchronized void increment() {
        counter = counter + 1;
    }

    public synchronized void decrement() {
        counter = counter - 1;
    }

    public String toString() {
        return "" + counter;
    }
}
