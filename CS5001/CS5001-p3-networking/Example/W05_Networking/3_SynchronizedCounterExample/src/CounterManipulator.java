public abstract class CounterManipulator extends Thread {

    protected SynchronizedCounter counter;
    protected int numCycles;

    public CounterManipulator(SynchronizedCounter counter, int numCycles) {
        this.counter = counter;
        this.numCycles = numCycles;
    }

    public void run() {
        for (int i = 0; i < numCycles; i++) {
            manipulateCounter();
        }
    }

    public abstract void manipulateCounter();
}
