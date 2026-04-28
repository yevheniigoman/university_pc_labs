import math
import time
import socket
from enum import IntEnum
from dataclasses import dataclass
from typing import Sequence, Self, Any


class MessageTypes(IntEnum):
    SETUP = 1
    SUCCESS = 2,
    ERROR = 3,
    PROCESS = 4,
    STATUS = 5,
    GET_RESULT = 6,
    SEND_RESULT = 7


class StatusCodes(IntEnum):
    IN_PROGRESS = 1,
    FAILED = 2,
    NOT_STARTED = 3


class ErrorCodes(IntEnum):
    INVALID_TYPE = 0,
    INVALID_NUM_THREADS = 1,
    INVALID_MATRIX_DIM = 2
    SETUP_REQUIRED = 3


@dataclass
class RawMessage:
    msg_type: MessageTypes
    length: int
    value: bytes

    @classmethod
    def setup(cls, matrix: Sequence[int], num_threads: int) -> Self:
        matrix_dim = cls.__calc_matrix_dim(matrix)

        value = bytearray()
        value.append(num_threads)
        value.extend(matrix_dim.to_bytes(4, "big"))
        for num in matrix:
            value.extend(num.to_bytes(4, "big"))

        length = len(value)
        return RawMessage(MessageTypes.SETUP, length, value)

    @classmethod
    def process(cls) -> Self:
        return RawMessage(MessageTypes.PROCESS, 0, bytes())

    @classmethod
    def get_result(cls) -> Self:
        return RawMessage(MessageTypes.GET_RESULT, 0, bytes())

    @staticmethod
    def __calc_matrix_dim(matrix: Sequence[int]) -> int:
        dim = math.isqrt(len(matrix))
        if dim * dim != len(matrix):
            raise AttributeError("Matrix should be square.")
        return dim


@dataclass
class ParsedMessage:
    msg_type: MessageTypes
    length: int
    value: dict[str, Any]

    def __getitem__(self, field: str) -> Any:
        return self.value[field]


class MatrixClient:
    __TYPE_FIELD_BYTES = 1
    __LEN_FIELD_BYTES = 4
    __MAX_NUM_THREADS = 12

    def __init__(self, host: str, port: int) -> None:
        self.__socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        self.__socket.connect((host, port))

    def __del__(self) -> None:
        self.__socket.close()

    def __send(self, msg: RawMessage) -> float:
        message_bytes = bytearray()
        message_bytes.append(msg.msg_type)
        message_bytes.extend(msg.length.to_bytes(4, "big"))
        message_bytes.extend(msg.value)

        send_time = time.time_ns()
        self.__socket.sendall(message_bytes)
        return send_time

    def __receive(self) -> ParsedMessage:
        type_len_bytes = self.__socket.recv(self.__TYPE_FIELD_BYTES + self.__LEN_FIELD_BYTES)
        res_type = MessageTypes(int.from_bytes(type_len_bytes[0:1], "big", signed=False))
        res_len = int.from_bytes(type_len_bytes[1:5], "big", signed=False)
        if res_len == 0:
            return ParsedMessage(res_type, res_len, {})

        value_bytes = self.__socket.recv(res_len)
        raw_msg = RawMessage(res_type, res_len, value_bytes)
        return self.__parse_raw_message(raw_msg)

    @staticmethod
    def __parse_raw_message(msg: RawMessage) -> ParsedMessage:
        msg_value = {}
        if msg.msg_type == MessageTypes.SUCCESS:
            receive_time = int.from_bytes(msg.value, "big", signed=True)
            msg_value["receive_time"] = receive_time
        if msg.msg_type == MessageTypes.ERROR:
            error = ErrorCodes(int.from_bytes(msg.value, "big", signed=False))
            msg_value["code"] = error
        elif msg.msg_type == MessageTypes.STATUS:
            status = StatusCodes(int.from_bytes(msg.value, "big", signed=False))
            msg_value["code"] = status
        elif msg.msg_type == MessageTypes.SEND_RESULT:
            matrix_dim = int.from_bytes(msg.value[0:4], "big", signed=False)
            matrix = []
            for i in range(4, len(msg.value), 4):
                matrix.append(int.from_bytes(msg.value[i:i+4], "big", signed=False))
            msg_value["dim"] = matrix_dim
            msg_value["matrix"] = matrix
        return ParsedMessage(msg.msg_type, msg.length, msg_value)


    def setup(self, matrix: Sequence[int], num_threads: int = 1) -> tuple[ParsedMessage, int]:
        if num_threads <= 0 or num_threads > self.__MAX_NUM_THREADS:
            raise AttributeError("Number of threads must be in range [0, 255].")
        if len(matrix) > 1_000_000:
            raise AttributeError("Matrix can not be greater than 1000x1000 elements.")

        msg = RawMessage.setup(matrix, num_threads)
        start_time = self.__send(msg)
        parsed_msg = self.__receive()
        elapsed_time = parsed_msg['receive_time'] - start_time
        return parsed_msg, elapsed_time

    def process(self) -> ParsedMessage:
        self.__send(RawMessage.process())
        return self.__receive()

    def get_result(self) -> ParsedMessage:
        self.__send(RawMessage.get_result())
        return self.__receive()
