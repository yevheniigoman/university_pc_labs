import java.util.Random;

public class AtomicParallel {
    public record AtomicResultAndTime(AtomicResult result, long elapsedTime) {}

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("You need to enter data size.");
            return;
        }

        long size = Integer.parseInt(args[0]);
        int origin = 0;
        int bound = 100;
        int threshold = (bound - origin) / 2;
        int processors = Runtime.getRuntime().availableProcessors();

        Random rand = new Random();
        int[] data = rand.ints(size, origin, bound).toArray();

        var atomicResultAndTime = findCountAndMin(data, threshold, processors);
//        var atomicResult = atomicResultAndTime.result();
        var elapsedTime = atomicResultAndTime.elapsedTime();

//        var sequentialResult = Sequential.findCountAndMin(data, threshold);

//        if (atomicResult.count.get() == sequentialResult.count && atomicResult.min.get() == sequentialResult.min) {
//            System.out.println("Test passed");
//        } else {
//            System.out.println("Test failed");
//        }
        System.out.println(elapsedTime);
    }

    public static AtomicResultAndTime findCountAndMin(int[] data, int threshold, int numThreads) {
        var result = new AtomicResult(0, Integer.MAX_VALUE);

        Thread[] threads = new Thread[numThreads];
        int itemsPerThread = data.length / numThreads;

        for (int i = 0; i < numThreads; i++) {
            int start = i * itemsPerThread;
            int end = (i == numThreads - 1) ? data.length : (i + 1) * itemsPerThread;
            threads[i] = new AtomicThread(data, start, end, threshold, result);
        }

        long startTime = System.nanoTime();
        for (var thread : threads) {
            thread.start();
        }

        try {
            for (var thread : threads) {
                thread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long elapsedTime = System.nanoTime() - startTime;
        return new AtomicResultAndTime(result, elapsedTime);
    }
}
