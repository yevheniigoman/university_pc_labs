import java.net.Socket;
import java.io.Writer;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStreamWriter;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final String aboutPage;
    private long startTime;

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        aboutPage = Files.readString(Path.of("..", "resources", "about.html"));
    }

    @Override
    public void run() {
        startTime = System.nanoTime();

        BufferedReader reader;
        BufferedWriter writer;
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            return;
        }

        HttpRequest request;
        HttpResponse response;
        try(reader; writer) {
            String requestLine = reader.readLine();
            if (requestLine == null || requestLine.isBlank()) {
                response = HttpResponse.builder().status(HttpStatus.BAD_REQUEST).build();
                sendResponse(writer, response);
                return;
            }

            String[] details = requestLine.split(" ");
            if (details.length < 3) {
                response = HttpResponse.builder().status(HttpStatus.BAD_REQUEST).build();
                sendResponse(writer, response);
                return;
            }

            HttpMethod method;
            if (details[0].equals("GET")) {
                method = HttpMethod.GET;
            } else {
                System.out.println("Method: " + details[0]);
                response = HttpResponse.builder()
                        .status(HttpStatus.METHOD_NOT_ALLOWED)
                        .header("Allow", "GET")
                        .build();
                sendResponse(writer, response);
                return;
            }

            request = new HttpRequest(method, details[1]);

            response = switch (request.path()) {
                case "/" -> index();
                case "/about" -> about();
                default -> notFound();
            };
            sendResponse(writer, response);
        } catch (IOException _) {
            response = HttpResponse.builder().status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            sendResponse(writer, response);
        }
    }

    private void sendResponse(Writer writer, HttpResponse response) {
        try {
            writer.write(response.toString());
            writer.flush();
            long endTime = System.nanoTime();
            long elapsedTime = endTime - startTime;
            System.out.println("Response time: " +  elapsedTime / 1_000 + " microseconds");
        } catch (IOException _) {}
    }

    private HttpResponse index() throws IOException {
        String htmlString = Files.readString(Path.of("..", "resources", "index.html"));
        return HttpResponse.builder().status(HttpStatus.OK).html(htmlString).build();
    }

    private HttpResponse about() throws IOException {
        return HttpResponse.builder().status(HttpStatus.OK).html(aboutPage).build();
    }

    private HttpResponse notFound() throws IOException {
        String htmlString = Files.readString(Path.of("..", "resources", "404.html"));
        return HttpResponse.builder().status(HttpStatus.NOT_FOUND).html(htmlString).build();
    }
}
