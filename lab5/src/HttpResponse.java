import java.util.Map;

public record HttpResponse(HttpStatus status, Map<String, String> headers, String body) {
    public static HttpResponseBuilder builder() {
        return new HttpResponseBuilder();
    }

    @Override
    public String toString() {
        var msg = new StringBuilder();
        msg.append("HTTP/1.1 ");
        msg.append(status.getCode());
        msg.append(' ');
        msg.append(status.getName());

        if (!headers.isEmpty()) {
            msg.append('\n');
            for (var entry : headers.entrySet()) {
                msg.append(entry.getKey());
                msg.append(": ");
                msg.append(entry.getValue());
                msg.append('\n');
            }
        }

        if (body != null && !body.isBlank()) {
            msg.append('\n');
            msg.append(body);
        }
        return msg.toString();
    }
}
