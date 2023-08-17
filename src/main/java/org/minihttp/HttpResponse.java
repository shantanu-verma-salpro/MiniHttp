package org.minihttp;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    private final int statusCode;
    private final Map<String, String> headers;
    private final String body;

    private HttpResponse(Builder builder) {
        this.statusCode = builder.statusCode;
        this.headers = builder.headers;
        this.body = builder.body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, String> getHeaders() {
        return new HashMap<>(headers); // Return a copy of headers
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "HttpResponse{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", body='" + body + '\'' +
                '}';
    }
    public ByteBuffer toByteBuffer(ByteBuffer buffer) {
        byte[] statusLineBytes = ("HTTP/1.1 " + statusCode + "\r\n").getBytes(StandardCharsets.UTF_8);
        byte[] contentLengthHeaderBytes = ("Content-Length: " + body.length() + "\r\n").getBytes(StandardCharsets.UTF_8);
        buffer.clear();

        buffer.put(statusLineBytes);
        putHeadersToBuffer(buffer);
        buffer.put(contentLengthHeaderBytes);
        buffer.put((byte) '\r');
        buffer.put((byte) '\n');

        if (!body.isEmpty()) {
            byte[] bodyBytes = body.getBytes(StandardCharsets.UTF_8);
            buffer.put(bodyBytes);
        }

        buffer.flip();
        return buffer;
    }

    private void putHeadersToBuffer(ByteBuffer buffer) {
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            buffer.put(entry.getKey().getBytes(StandardCharsets.UTF_8));
            buffer.put((byte) ':');
            buffer.put((byte) ' ');
            buffer.put(entry.getValue().getBytes(StandardCharsets.UTF_8));
            buffer.put((byte) '\r');
            buffer.put((byte) '\n');
        }
    }

    public static class Builder {
        private int statusCode = 200; // Default status code is OK
        private Map<String, String> headers = new HashMap<>();
        private String body = "";

        public Builder statusCode(int statusCode) {
            this.statusCode = statusCode;
            return this;
        }

        public Builder header(String name, String value) {
            headers.put(name, value);
            return this;
        }

        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }
    }
}
