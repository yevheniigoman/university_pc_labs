import java.util.concurrent.atomic.AtomicInteger;

public class AtomicResult {
    public AtomicInteger count;
    public AtomicInteger min;

    public AtomicResult(int count, int min) {
        this.count = new AtomicInteger(count);
        this.min = new AtomicInteger(min);
    }
}
