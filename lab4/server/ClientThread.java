import java.net.Socket;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Future;

public class ClientThread implements Runnable {
    private static final int LENGTH_FIELD_BYTES = 4;
    private static final int MATRIX_DIM_FIELD_BYTES = 4;

    private final ThreadPoolExecutor threadPool;
    private List<Future<Long>> futures;
    private final Socket socket;
    private List<Integer> matrix;
    private int matrixDim;
    private int numThreads;

    public ClientThread(Socket socket, ThreadPoolExecutor threadPool) {
        this.socket = socket;
        this.threadPool = threadPool;
    }

    @Override
    public void run() {
        try (var inputStream = new BufferedInputStream(socket.getInputStream());
             var outputStream = new BufferedOutputStream(socket.getOutputStream())) {
            while (true) {
                RawMessage request;
                try {
                    request = readMessage(inputStream);
                } catch (IllegalArgumentException _) {
                    RawMessage response = handleUnrecognizedRequest();
                    sendMessage(outputStream, response);
                    continue;
                }

                Instant now = Instant.now();
                long receiveTime = (now.getEpochSecond() * 1_000_000_000L) + now.getNano();

                RawMessage response = switch (request.type()) {
                    case MessageTypes.SETUP -> handleSetupRequest(request, receiveTime);
                    case MessageTypes.PROCESS -> handleProcessRequest();
                    case MessageTypes.GET_RESULT -> handleGetResultRequest();
                    default -> handleUnrecognizedRequest();
                };
                sendMessage(outputStream, response);
            }
        } catch (IOException e) {

        }
    }

    private static RawMessage readMessage(BufferedInputStream inputStream)
            throws IOException, IllegalArgumentException {
        MessageTypes type = MessageTypes.fromInt(inputStream.read());
        byte[] lengthBytes = inputStream.readNBytes(LENGTH_FIELD_BYTES);
        int length = ByteBuffer.wrap(lengthBytes).getInt();
        byte[] valueBytes = inputStream.readNBytes(length);
        return new RawMessage(type, length, valueBytes);
    }

    private static void sendMessage(BufferedOutputStream outputStream, RawMessage msg)
            throws IOException {
        byte[] lengthBytes = ByteBuffer.allocate(Integer.BYTES).putInt(msg.length()).array();
        outputStream.write(msg.type().getCode());
        outputStream.write(lengthBytes);
        outputStream.write(msg.value());
        outputStream.flush();
    }

    private RawMessage handleSetupRequest(RawMessage msg, long receiveTime) throws IOException {
        ByteBuffer valueBuffer = ByteBuffer.wrap(msg.value());
        numThreads = Byte.toUnsignedInt(valueBuffer.get(0));
        if (numThreads == 0 || numThreads > threadPool.getCorePoolSize()) {
            return RawMessage.error(ErrorCodes.INVALID_NUM_THREADS);
        }
        matrixDim = valueBuffer.slice(1, 1 + MATRIX_DIM_FIELD_BYTES).getInt();
        if (matrixDim <= 0 || matrixDim > 1000) {
            return RawMessage.error(ErrorCodes.INVALID_MATRIX_DIM);
        }

        matrix = new ArrayList<>();
        for (int i = 5; i < msg.length(); i += Integer.BYTES) {
            int number = valueBuffer.slice(i, Integer.BYTES).getInt();
            matrix.add(number);
        }

        futures = new ArrayList<>(numThreads);
        return RawMessage.success(receiveTime);
    }

    private RawMessage handleProcessRequest() {
        if (matrix == null) {
            return RawMessage.error(ErrorCodes.SETUP_REQUIRED);
        }

        System.out.println("Processing started...");
        if (numThreads == 1) {
            Future<Long> future = threadPool.submit(() -> {
                long startTime = System.nanoTime();
                Matrices.fillSequentially(matrix, matrixDim);
                return System.nanoTime() - startTime;
            });
            futures.add(future);
        } else {
            numThreads = Math.min(numThreads, matrixDim);

            // розподілення стовпців матриці між потоками
            int colsPerThread = matrixDim / numThreads;
            for (int i = 0; i < numThreads; i++) {
                int fromColumn = i * colsPerThread;
                int toColumn = (i == numThreads - 1) ? matrixDim : (i + 1) * colsPerThread;
                Future<Long> future = threadPool.submit(() -> {
                    long startTime = System.nanoTime();
                    Matrices.fillMatrixColumns(matrix, matrixDim, fromColumn, toColumn);
                    return System.nanoTime() - startTime;
                });
                futures.add(future);
            }
        }
        return RawMessage.status(StatusCodes.IN_PROGRESS);
    }

    private RawMessage handleGetResultRequest() {
        if (futures == null) {
            return RawMessage.error(ErrorCodes.SETUP_REQUIRED);
        }
        if (futures.isEmpty()) {
            return RawMessage.status(StatusCodes.NOT_STARTED);
        }

        boolean finished = futures.stream()
                .allMatch(future -> future.state() == Future.State.SUCCESS);
        if (finished) {
            Optional<Long> elapsedTime = futures.stream()
                    .map(Future::resultNow)
                    .max(Long::compare);
            System.out.println("Max time: " + elapsedTime.get() / 1000 + " ms");
            return RawMessage.sendResult(matrix, matrixDim);
        }
        boolean failed = futures.stream()
                .anyMatch(future -> future.state() == Future.State.FAILED || future.state() == Future.State.CANCELLED);
        if (failed) {
            return RawMessage.status(StatusCodes.FAILED);
        }

        return RawMessage.status(StatusCodes.IN_PROGRESS);
    }

    private RawMessage handleUnrecognizedRequest() {
        return RawMessage.error(ErrorCodes.INVALID_TYPE);
    }
}
