import java.util.Arrays;
import java.util.Random;

public class Sequential {
    public static void main(String[] args) {
        long size = 10;
        int origin = 0;
        int bound = 100;
        int threshold = (bound - origin) / 2 ;

        Random rand = new Random();
        int[] data = rand.ints(size, origin, bound).toArray();

        var result = findCountAndMin(data, threshold);

        System.out.println("Data: " + Arrays.toString(data));
        System.out.println("Count: " + result.count + ", min: " + result.min);
    }

    public static Result findCountAndMin(int[] data, int threshold) {
        int count = 0;
        int min = Integer.MAX_VALUE;
        for (var num : data) {
            if (num < threshold) {
                count++;
            }

            if (num < min) {
                min = num;
            }
        }
        return new Result(count, min);
    }
}
