package org.minihttp;

import com.sun.net.httpserver.HttpHandler;

import java.util.*;

class Pair<K, V> extends AbstractMap.SimpleImmutableEntry<K, V> {

    public Pair(K key, V value) {
        super(key, value);
    }

    public Pair(Map.Entry<? extends K, ? extends V> entry) {
        super(entry);
    }
}
class URLTrieNode {
    final Map<Pair<String, HttpMethod>, URLTrieNode> children;
    Handler handler;
    String pathParam;
    boolean wildcard;


    public URLTrieNode(Handler handler) {
        children = new HashMap<>();
        this.handler = handler;
        pathParam = null;
        this.wildcard = false;
    }

    public boolean isWildcard() {
        return this.wildcard;
    }

    public void setIsWildcard(boolean x) {
        this.wildcard = x;
    }

    public String getPathParam() {
        return this.pathParam;
    }

    public void setPathParam(String x) {
        this.pathParam = x;
    }

    public Map<Pair<String, HttpMethod>, URLTrieNode> getChildren() {
        return children;
    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler h) {
        this.handler = h;
    }

}
class PathParameters extends HashMap<String, String> {

}
public class Router {
    private final URLTrieNode node;

    public Router() {
        node = new URLTrieNode(null);
    }
    public void add(HttpMethod r,String _u, Handler h){
        List<String> parts = getStrings(_u);
        URLTrieNode temp = node;
        for (String part : parts) {
            URLTrieNode n = temp.getChildren().get(new Pair<>(part, r));
            if (n == null) {
                n = new URLTrieNode(h);
                if (part.charAt(0)==':') {
                    temp.setPathParam(part.substring(1));
                } else if (part.charAt(0) == '*') {
                    temp.setIsWildcard(true);
                }
                temp.getChildren().put(new Pair<>(part, r), n);
            }
            temp = n;
        }
    }

    public Pair<PathParameters, Handler> find( HttpMethod r,String u) {
        PathParameters mp = new PathParameters();
        List<String> parts = getStrings(u);
        URLTrieNode temp = node;
        URLTrieNode wildcardNode = null;
        boolean wilcardOccured = false;
        for (String part : parts) {
            URLTrieNode n = temp.getChildren().get(new Pair<>(part, r));
            if (n == null) {
                if (wilcardOccured) n = temp;
                else if (temp.isWildcard()) {
                    n = temp.getChildren().get(new Pair<>("*", r));
                    wilcardOccured = true;
                } else if (temp.getPathParam() != null) {
                    n = temp.getChildren().get(new Pair<>(temp.getPathParam(), r));
                    mp.put(temp.getPathParam(), part);
                }
            } else wilcardOccured = false;
            temp = n;
            if (temp == null) return null;
        }
        return new Pair<>(mp, temp.getHandler());
    }

    private static List<String> getStrings(String u) {
        StringBuilder path = new StringBuilder();
        List<String> parts = new LinkedList<>();
        int urlLength = u.length();

        for (int i = 0; i < urlLength && u.charAt(i) != '?'; ++i) {
            char c = u.charAt(i);
            if (c == '/') {
                if (path.length() > 0) {
                    parts.add(path.toString());
                    path.setLength(0);
                }
            } else {
                path.append(c);
            }
        }
        if (path.length() > 0) {
            parts.add(path.toString());
        }
        return parts;
    }
}