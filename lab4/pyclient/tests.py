import random
import pytest
from time import sleep
from client import MatrixClient, MessageTypes, StatusCodes, ErrorCodes


def test_setup_process_result_success():
    initial_matrix = [random.randint(1, 100) for _ in range(4 ** 2)]
    num_threads = 4

    client = MatrixClient("localhost", 8000)
    response, time_elapsed = client.setup(initial_matrix, num_threads)
    assert response.msg_type == MessageTypes.SUCCESS

    response = client.process()
    assert response.msg_type == MessageTypes.STATUS
    assert response["code"] == StatusCodes.IN_PROGRESS

    while (msg := client.get_result()).msg_type != MessageTypes.SEND_RESULT:
        sleep(0.1)


def test_process_without_setup():
    client = MatrixClient("localhost", 8000)
    response = client.process()
    assert response.msg_type == MessageTypes.ERROR
    assert response["code"] == ErrorCodes.SETUP_REQUIRED


def test_result_without_setup_process():
    client = MatrixClient("localhost", 8000)
    response = client.get_result()
    assert response.msg_type == MessageTypes.ERROR
    assert response["code"] == ErrorCodes.SETUP_REQUIRED


def test_setup_result_without_process():
    initial_matrix = [random.randint(1, 100) for _ in range(4 ** 2)]
    num_threads = 4

    client = MatrixClient("localhost", 8000)
    client.setup(initial_matrix, num_threads)

    response = client.get_result()
    assert response.msg_type == MessageTypes.STATUS
    assert response["code"] == StatusCodes.NOT_STARTED