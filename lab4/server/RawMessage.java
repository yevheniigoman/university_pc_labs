import java.nio.ByteBuffer;
import java.util.List;

record RawMessage(MessageTypes type, int length, byte[] value) {
    public static RawMessage success(long receiveTime) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(receiveTime);
        return new RawMessage(MessageTypes.SUCCESS, Long.BYTES, buffer.array());
    }

    public static RawMessage error(ErrorCodes code) {
        byte[] value = new byte[] {(byte) code.getCode()};
        return new RawMessage(MessageTypes.ERROR, value.length, value);
    }

    public static RawMessage status(StatusCodes code) {
        byte[] value = new byte[] {(byte) code.getCode()};
        return new RawMessage(MessageTypes.STATUS, value.length, value);
    }

    public static RawMessage sendResult(List<Integer> matrix, int matrixDim) {
        ByteBuffer buffer = ByteBuffer.allocate((matrix.size() + 1) * Integer.BYTES);
        buffer.putInt(matrixDim);
        for (int num : matrix) {
            buffer.putInt(num);
        }
        byte[] value = buffer.array();
        return new RawMessage(MessageTypes.SEND_RESULT, value.length, value);
    }
}