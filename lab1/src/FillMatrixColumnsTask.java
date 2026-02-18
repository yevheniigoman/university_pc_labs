import java.util.List;

public class FillMatrixColumnsTask implements Runnable {
    /*
    * Заповнює елементи головної діагоналі кожного стовпця
    * з fromColumn до toColumn (не включно) шляхом обчислення
    * суми кожного другого елементу стовпця матриці.
    * */
    private final List<Integer> matrix;
    private final int matrixDim;
    private final int fromColumn; // номер початкового стовпця (починаючи з 0)
    private final int toColumn; // номер останнього стовпця (не включно)

    public FillMatrixColumnsTask(final List<Integer> matrix, int matrixDim, int fromColumn, int toColumn) {
        this.matrix = matrix;
        this.matrixDim = matrixDim;
        this.fromColumn = fromColumn;
        this.toColumn = toColumn;
    }

    @Override
    public void run() {
        int sum = 0;
        int columnStartIndex, columnEndIndex;
        for (int col = this.fromColumn; col < this.toColumn; col++) {
            columnStartIndex = col * this.matrixDim;
            columnEndIndex = (col + 1) * this.matrixDim;
            // обчислення суми кожного другого елементу стовпця матриці
            for (int i = columnStartIndex + 1; i < columnEndIndex; i += 2) {
                sum += this.matrix.get(i);
            }
            // розміщення знайденої суми в елементі головної діагоналі
            this.matrix.set(columnStartIndex + col, sum);
            sum = 0;
        }
    }
}