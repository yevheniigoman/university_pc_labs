import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Matrix {
    public static void main(String[] args) {
        CommandLineOptions options = CommandLineOptionsParser.parse(args);
        int matrixDim = options.dim();
        int numThreads = options.threads();

        // матриця зберігається як одновимірний масив, згрупований за стовпцями
        List<Integer> matrix = new ArrayList<>(matrixDim * matrixDim);

        Random rand = new Random();
        for (int i = 0; i < matrixDim * matrixDim; i++) {
            matrix.add(rand.nextInt(0, 100));
        }

        if (options.print()) {
            printMatrix(matrix, matrixDim);
            System.out.println();
        }

        long startTime = System.nanoTime();
        if (numThreads == 1) {
            fillSequentially(matrix, matrixDim);
        } else {
            fillParallel(matrix, matrixDim, numThreads);
        }
        long elapsedTime = System.nanoTime() - startTime;

        if (options.print()) {
            printMatrix(matrix, matrixDim);
        }

        System.out.print(elapsedTime);
    }

    public static void fillSequentially(final List<Integer> matrix, int matrixDim) {
        int sum = 0;
        for (int col = 0; col < matrixDim; col++) {
            for (int row = 1; row < matrixDim; row += 2) {
                sum += matrix.get(col * matrixDim + row);
            }
            matrix.set(col * matrixDim + col, sum);
            sum = 0;
        }
    }

    public static void fillParallel(final List<Integer> matrix, int matrixDim, int numThreads) {
        numThreads = Math.min(numThreads, matrixDim);
        List<Thread> threads = new ArrayList<>(numThreads);

        // розподілення стовпців матриці між потоками
        int colsPerThread = matrixDim / numThreads;
        int fromColumn, toColumn;
        for (int i = 0; i < numThreads; i++) {
            fromColumn = i * colsPerThread;
            toColumn = (i == numThreads - 1) ? matrixDim : (i + 1) * colsPerThread;
            Runnable task = new FillMatrixColumnsTask(matrix, matrixDim, fromColumn, toColumn);
            threads.add(new Thread(task));
        }

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void printMatrix(final List<Integer> matrix, int matrixDim) {
        for (int row = 0; row < matrixDim; row++) {
            for (int col = 0; col < matrixDim; col++) {
                System.out.print(Integer.toString(matrix.get(col * matrixDim + row)) + ' ');
            }
            System.out.println();
        }
    }
}
