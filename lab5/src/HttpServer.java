import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.util.concurrent.Executors;

public class HttpServer {
    private final int nThreads;
    private final int port;

    public HttpServer(int port, int nThreads) {
        this.port = port;
        this.nThreads = nThreads;
    }

    public void start() {
        try(var serverSocket = new ServerSocket(port);
            var threadPool = Executors.newFixedThreadPool(nThreads)) {
            System.out.println("Server is running on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                threadPool.execute(new ClientHandler(socket));
            }
        } catch (IOException _) {

        }
    }

    static void main(String[] args) {
        var httpServer = new HttpServer(8000, 12);
        httpServer.start();
    }
}
