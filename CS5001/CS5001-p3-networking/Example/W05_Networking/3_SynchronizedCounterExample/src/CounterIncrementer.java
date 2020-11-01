public class CounterIncrementer extends CounterManipulator {

    public CounterIncrementer(SynchronizedCounter counter, int numCycles) {
        super(counter, numCycles);
    }

    public void manipulateCounter() {
        counter.increment();
    }
}
