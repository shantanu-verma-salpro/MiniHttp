package org.minihttp;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;

public class HttpParser {

    public HttpParser() {

    }

    HttpRequest parse_(ByteBuffer b) throws CharacterCodingException {
        CharBuffer buffer = StandardCharsets.UTF_8.newDecoder().decode(b);
        HttpRequest.Builder req = new HttpRequest.Builder();
        int idx = 0;
        char c = buffer.get(idx);
        StringBuilder acc = new StringBuilder();
        if (c == 'G') {
            req.method(HttpMethod.GET);
            idx += 4;
        } else if (c == 'P') {
            ++idx;
            c = buffer.get(idx);
            if (c == 'O') {
                req.method(HttpMethod.POST);
                idx += 4;
            } else if (c == 'U') {
                req.method(HttpMethod.PUT);
                idx += 3;
            } else if (c == 'A') {
                req.method(HttpMethod.PATCH);
                idx += 5;
            }
        } else if (c == 'D') {
            req.method(HttpMethod.DELETE);
            idx += 7;
        } else if (c == 'H') {
            req.method(HttpMethod.HEAD);
            idx += 5;
        } else if (c == 'O') {
            req.method(HttpMethod.OPTIONS);
            idx += 8;
        } else return null;

        c = buffer.get(idx);

        do {
            acc.append(c);
            c = buffer.get(++idx);
        } while (c != ' ');
        req.url(acc.toString());
        acc.setLength(0);
        idx += 6;
        if (buffer.get(idx) != '1') return null;
        idx += 5;
        String header_name;
        c = buffer.get(idx);
        do {
            do {
                acc.append(c);
                c = buffer.get(++idx);
            } while (c != ':');
            header_name = acc.toString();
            acc.setLength(0);
            do {
                acc.append(c);
                c = buffer.get(++idx);
            } while (c != '\r');
            req.header(header_name, acc.toString());
            acc.setLength(0);
            idx += 2;
            c = buffer.get(idx);
        } while (c != '\r');
        idx += 2;
        buffer.position(idx);
        req.body(buffer.slice(idx, buffer.remaining()).toString());
        return req.build();
    }

}