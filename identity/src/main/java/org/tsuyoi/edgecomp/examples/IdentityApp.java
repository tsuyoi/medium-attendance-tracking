package org.tsuyoi.edgecomp.examples;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.tsuyoi.edgecomp.examples.identity.LookupRequest;
import org.tsuyoi.edgecomp.examples.identity.LookupResult;
import org.tsuyoi.edgecomp.examples.identity.LookupService;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;

public class IdentityApp {
    private static LookupService lookupService;

    public static void main( String[] args ) throws IOException {
        lookupService = new LookupServiceImpl(
                "fake-user-name", "fake-first-name", "fake-last-name");
        HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
        HttpContext context = server.createContext("/");
        context.setHandler(IdentityApp::handleRequest);
        server.start();
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        int resCode = 404;
        String response = "Content not found";
        if (requestURI.getPath().equals("/lookup")) {
            if (requestURI.getQuery() != null) {
                String[] query = requestURI.getQuery().split("&");
                if (query.length > 0) {
                    String id = null;
                    for (int i = 0; i < query.length; i++) {
                        String[] paramParts = query[i].split("=");
                        if (paramParts.length > 1 && paramParts[0].equals("id"))
                            id = paramParts[1];
                    }
                    if (id != null) {
                        LookupRequest request = new LookupRequest(id);
                        System.out.println(request);
                        LookupResult result = lookupService.lookup(request);
                        response = result.toString();
                        resCode = 200;
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                    } else {
                        resCode = 400;
                        response = "You must supply an 'id' query parameter";
                    }
                } else {
                    resCode = 400;
                    response = "You must supply an 'id' query parameter";
                }
            } else {
                resCode = 400;
                response = "You must supply an 'id' query parameter";
            }
        }
        exchange.sendResponseHeaders(resCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        exchange.close();
    }
}
