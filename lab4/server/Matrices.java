import java.util.List;

public class Matrices {
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

    public static void fillMatrixColumns(List<Integer> matrix, int matrixDim, int fromColumn, int toColumn) {
        int sum = 0;
        int columnStartIndex, columnEndIndex;
        for (int col = fromColumn; col < toColumn; col++) {
            columnStartIndex = col * matrixDim;
            columnEndIndex = (col + 1) * matrixDim;
            // обчислення суми кожного другого елементу стовпця матриці
            for (int i = columnStartIndex + 1; i < columnEndIndex; i += 2) {
                sum += matrix.get(i);
            }
            // розміщення знайденої суми в елементі головної діагоналі
            matrix.set(columnStartIndex + col, sum);
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
