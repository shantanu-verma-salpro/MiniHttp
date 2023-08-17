package org.minihttp;

import com.sun.net.httpserver.HttpHandler;

public class Dispatcher {
    private final Router r;
    public Dispatcher(){
        r = new Router();
    }
    void add(HttpMethod method,String url,Handler h){
        r.add(method,url,h);
    }
    Pair<PathParameters, Handler> find(HttpMethod method, String url){
        return r.find(method,url);
    }
}
