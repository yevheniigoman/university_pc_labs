import argparse
import subprocess
from typing import NamedTuple
import matplotlib.pyplot as plt


parser = argparse.ArgumentParser(description="Program for measuring time of matrix filling.")
parser.add_argument("size", type=int, help="Number of elements in array")

class TimeResult(NamedTuple):
    blocking_time: int
    atomic_time: int

def measure_time(size: int) -> TimeResult:
    NUM_ITERATIONS = 5
    blocking_time = 0.0
    atomic_time = 0.0
    for _ in range(NUM_ITERATIONS):
        blocking_process = subprocess.run(
            f"java ../lab2/src/BlockingParallel.java {size}",
            stdout=subprocess.PIPE,
            text=True
        )
        blocking_time += float(blocking_process.stdout)

        atomic_process = subprocess.run(
            f"java ../lab2/src/AtomicParallel.java {size}",
            stdout=subprocess.PIPE,
            text=True
        )
        atomic_time += float(atomic_process.stdout)
    blocking_time = blocking_time / 1_000_000 / NUM_ITERATIONS
    atomic_time = atomic_time / 1_000_000 / NUM_ITERATIONS
    return TimeResult(blocking_time, atomic_time)

if __name__ == "__main__":
    args = parser.parse_args()

    blocking_time, atomic_time = measure_time(args.size)
    print(f"Blocking time: {blocking_time} ms")
    print(f"Atomic time: {atomic_time} ms")

    fig, ax = plt.subplots()
    ax.set_title(f"Розмір масиву {args.size}")
    ax.set_ylabel("Час виконання (мс)")
    ax.bar(
        ["synchronized", "AtomicInteger"],
        [blocking_time, atomic_time],
        width=0.9,
        edgecolor="white"
    )
    ax.set_ylim(bottom=min(blocking_time, atomic_time) / 2)
    plt.show()