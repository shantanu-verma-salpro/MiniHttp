package org.minihttp;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



public class HttpRequest {
    private final String url;
    private final HttpMethod method;
    private final Map<String, String> headers;
    private final String body;
    private Map<String,String> query;
    private String path;

    private HttpRequest(Builder builder) {
        this.query = builder.query;
        this.path = builder.path;
        this.url = builder.url;
        this.method = builder.method;
        this.headers = builder.headers; // No need to create a new HashMap
        this.body = builder.body;       // No need to copy the body
    }



    public String getUrl() {
        return url;
    }
    public String getPath() {
        return path;
    }
    Map<String ,String> queryParams(){
        return Collections.unmodifiableMap(query);
    }
    public HttpMethod getMethod() {
        return method;
    }

    public Map<String, String> getHeaders() {
        return Collections.unmodifiableMap(headers);
    }

    public String getBody() {
        return body; // No need to copy the body
    }

    public static class Builder {
        private String url = "";
        private HttpMethod method = HttpMethod.GET;
        private Map<String, String> headers = null; // Lazy initialization
        private String body = "";
        private String path = "";
        private Map<String,String> query = null;

        public Builder() {

        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public Builder method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Builder header(String name, String value) {
            if (headers == null) {
                headers = new HashMap<>(); // Initialize headers lazily
            }
            headers.put(name, value);
            return this;
        }
        public Builder query(String name,String value){
            if (query == null) {
                query = new HashMap<>(); // Initialize headers lazily
            }
            query.put(name, value);
            return this;
        }
        public Builder body(String body) {
            this.body = body;
            return this;
        }

        public HttpRequest build() {
            int idx = 0;
            StringBuilder s = new StringBuilder();
            while(idx < url.length() && url.charAt(idx)!='?') s.append(url.charAt(idx));
            path = s.toString();
            s.setLength(0);
            ++idx;
            while(idx < url.length()){

            }
            return new HttpRequest(this);
        }
    }
}

