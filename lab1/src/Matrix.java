import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Matrix {
    public static void main(String[] args) {
        int matrixDim = 5;

        // матриця зберігається як одновимірний масив, згрупований за стовпцями
        List<Integer> matrix = new ArrayList<>(matrixDim * matrixDim);

        Random rand = new Random();
        for (int i = 0; i < matrixDim * matrixDim; i++) {
            matrix.add(rand.nextInt(0, 100));
        }

        printMatrix(matrix, matrixDim);
        System.out.println();

        long startTime = System.nanoTime();
        fillSequentially(matrix, matrixDim);
        long elapsedTime = System.nanoTime() - startTime;

        printMatrix(matrix, matrixDim);
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

    public static void printMatrix(final List<Integer> matrix, int matrixDim) {
        for (int row = 0; row < matrixDim; row++) {
            for (int col = 0; col < matrixDim; col++) {
                System.out.print(Integer.toString(matrix.get(col * matrixDim + row)) + ' ');
            }
            System.out.println();
        }
    }
}
