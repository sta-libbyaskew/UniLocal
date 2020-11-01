public class TestCounter {

    public static void main(String[] args) {

        SynchronizedCounter sharedCounter = new SynchronizedCounter();
        System.out.println("Counter value before = " + sharedCounter);

        CounterIncrementer t1 = new CounterIncrementer(sharedCounter, 100000);
        CounterDecrementer t2 = new CounterDecrementer(sharedCounter, 100000);

        t1.start();
        t2.start();

        try {
            t1.join();
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("Counter value after = " + sharedCounter);
    }
}
