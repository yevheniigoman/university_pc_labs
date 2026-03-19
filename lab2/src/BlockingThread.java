public class BlockingThread extends Thread {
    public static final Object lock = new Object();

    public final int[] data;
    public final int start;
    public final int end;
    public final int threshold;
    public final Result result;

    public BlockingThread(int[] data, int start, int end, int threshold, Result result) {
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
                synchronized (lock) {
                    this.result.count++;
                }
            }

            synchronized (lock) {
                if (data[i] < this.result.min) {
                    this.result.min = data[i];
                }
            }
        }
    }
}
