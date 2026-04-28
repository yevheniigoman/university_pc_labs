import java.net.ServerSocket;
import java.net.Socket;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class MatrixServer {
    private static final int MAX_NUM_CLIENTS = 5;
    private static final int MAX_NUM_THREADS = 12;
    private final int port;

    public MatrixServer(int port) {
        this.port = port;
    }

    public void start() {
        try (var serverSocket = new ServerSocket(port);
             var clientsThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_NUM_CLIENTS);
             var tasksThreadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_NUM_THREADS)) {
            System.out.println("Server is listening on port " + port + "...");
            while (true) {
                Socket socket = serverSocket.accept();
                clientsThreadPool.execute(new ClientThread(socket, tasksThreadPool));
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    static void main(String[] args) {
        var server = new MatrixServer(8000);
        server.start();
    }
}
