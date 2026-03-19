public class AtomicThread extends Thread {
    public final int[] data;
    public final int start;
    public final int end;
    public final int threshold;
    public final AtomicResult result;

    public AtomicThread(int[] data, int start, int end, int threshold, AtomicResult result) {
        this.data = data;
        this.start = start;
        this.end = end;
        this.threshold = threshold;
        this.result = result;
    }

    @Override
    public void run() {
        for (int i = this.start; i < this.end; i++) {
            if (data[i] < this.threshold) {
                // count++
                int currentCount;
                do {
                    currentCount = this.result.count.get();
                } while (!this.result.count.compareAndSet(currentCount, currentCount + 1));
            }

            // if (data[i] < min) {
            //     min = data[i];
            // }
            int currentMin;
            do {
                currentMin = this.result.min.get();
                if (data[i] >= currentMin) {
                    break;
                }
            } while (!this.result.min.compareAndSet(currentMin, data[i]));
        }
    }
}
