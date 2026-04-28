import math
import random
from time import sleep
from typing import Sequence
from client import MatrixClient, MessageTypes


def print_matrix(matrix: Sequence[int], matrix_dim: int) -> None:
    for row in range(matrix_dim):
        for col in range(matrix_dim):
            print(matrix[col * matrix_dim + row], end=" ")
        print()


if __name__ == "__main__":
    client = MatrixClient("localhost", 8000)
    matrix = [random.randint(1, 100) for _ in range(1000 ** 2)]
    # print_matrix(matrix, math.isqrt(len(matrix)))

    response, elapsed_time = client.setup(matrix, 4)
    if response.msg_type == MessageTypes.SUCCESS:
        print(f"Elapsed time: {elapsed_time / 1_000_000} ms")

    response = client.process()
    print(response)

    while (msg := client.get_result()).msg_type != MessageTypes.SEND_RESULT:
        print(msg)
        print()
        sleep(0.1)
    # print(f"Response: {msg.msg_type.name}; {msg.length}")
    # print_matrix(msg["matrix"], msg["dim"])