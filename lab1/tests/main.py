import os
import argparse
import subprocess
import matplotlib.pyplot as plt


PHYSICAL_CORES = 8
LOGICAL_CORES = os.cpu_count()
NUM_THREADS = (
    1,
    PHYSICAL_CORES // 2,
    PHYSICAL_CORES,
    LOGICAL_CORES,
    LOGICAL_CORES * 2,
    LOGICAL_CORES * 4,
    LOGICAL_CORES * 8,
    LOGICAL_CORES * 16
)

parser = argparse.ArgumentParser(description="Program for measuring time of matrix filling.")
parser.add_argument("matrix_size", type=int, help="Number of rows of square matrix")
parser.add_argument("iters", type=int, help="Number of iterations for one measurement")

def measure_time(matrix_size: int, num_threads: int, num_iters: int) -> float:
    """
    Запускає матричні обчислення для матриці
    розмірністю <matrix_size> і кількістю потоків <num_threads>,
    повторює їх <num_iters> разів та повертає середній час виконання у секундах.
    """
    elapsed_time = 0.0
    command = "java ../src/Matrix.java --dim {} --threads {}"
    for _ in range(num_iters):
        process = subprocess.run(
            command.format(matrix_size, num_threads),
            stdout=subprocess.PIPE,
            text=True
        )
        elapsed_time += int(process.stdout)

    # переводимо з наносекунд у секунди
    return elapsed_time / 1_000_000_000 / num_iters


if __name__ == "__main__":
    args = parser.parse_args()

    elapsed_time_vals = []
    for threads in NUM_THREADS:
        elapsed_time = measure_time(args.matrix_size, threads, args.iters)
        elapsed_time_vals.append(elapsed_time)
        print(f"Size: {args.matrix_size} | Threads: {threads} | Time: {elapsed_time:.3f}")

    fig, ax = plt.subplots()
    ax.set_title(f"Матриця {args.matrix_size}x{args.matrix_size}")
    ax.set_xlabel("Кількість потоків")
    ax.set_ylabel("Час виконання (секунди)")
    ax.bar(
        [str(threads) for threads in NUM_THREADS],
        elapsed_time_vals,
        width=0.9,
        edgecolor="white"
    )
    ax.set_ylim(bottom=min(elapsed_time_vals) / 2)
    plt.show()