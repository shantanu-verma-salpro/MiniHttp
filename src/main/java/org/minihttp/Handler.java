package org.minihttp;

public interface Handler {
    HttpResponse handle(HttpRequest req, PathParameters param);
}