public class CounterDecrementer extends CounterManipulator {

    public CounterDecrementer(SynchronizedCounter counter, int numCycles) {
        super(counter, numCycles);
    }

    public void manipulateCounter() {
        counter.decrement();
    }
}
