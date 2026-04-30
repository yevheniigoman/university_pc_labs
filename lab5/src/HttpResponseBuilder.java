import java.util.HashMap;
import java.util.Map;

public class HttpResponseBuilder {
    private HttpStatus status;
    private final Map<String, String> headers;
    private String body;

    public HttpResponseBuilder() {
        headers = new HashMap<>();
    }

    public HttpResponseBuilder status(HttpStatus status) {
        this.status = status;
        return this;
    }

    public HttpResponseBuilder header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public HttpResponseBuilder html(String body) {
        headers.put("Content-Type", "text/html");
        headers.put("Content-Length", Integer.toString(body.length()));
        this.body = body;
        return this;
    }

    public HttpResponse build() {
        return new HttpResponse(status, headers, body);
    }
}
