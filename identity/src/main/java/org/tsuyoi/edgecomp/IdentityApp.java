package org.tsuyoi.edgecomp;

import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.tsuyoi.edgecomp.identity.IdentityService;
import org.tsuyoi.edgecomp.identity.LookupRequest;
import org.tsuyoi.edgecomp.identity.LookupResult;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Paths;

public class IdentityApp {
    private static IdentityService identityService;

    public static void main( String... args ) throws IOException {
        System.out.println("Creating IdentityService");
        if (args.length > 0) {
            try {
                identityService = new FileBasedIdentityService(Paths.get(args[0]));
            } catch (IOException e) {
                System.err.println("Failed to created FileBasedIdentityServer");
                e.printStackTrace();
                System.exit(1);
            }
        } else
            identityService = new FakeIdentityService(
                "fake-user-name", "fake-email", "fake-first-name", "fake-last-name");
        System.out.println("Creating http server");
        HttpServer server = HttpServer.create(new InetSocketAddress(8500), 0);
        HttpContext context = server.createContext("/");
        context.setAuthenticator(new BasicAuthenticator("get") {
            @Override
            public boolean checkCredentials(String user, String pass) {
                return user.equals("user") && pass.equals("password");
            }
        });
        context.setHandler(IdentityApp::handleRequest);
        System.out.println("Starting http server, use ctrl+C to stop.");
        server.start();
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        int resCode = 404;
        String response = "Content not found";
        if (requestURI.getPath().equals("/lookup")) {
            if (requestURI.getQuery() != null) {
                String[] params = requestURI.getQuery().split("&");
                if (params.length > 0) {
                    String id = null;
                    for (String param : params) {
                        String[] paramParts = param.split("=");
                        if (paramParts.length > 1 && paramParts[0].equals("id"))
                            id = paramParts[1];
                    }
                    if (id != null) {
                        LookupRequest request = new LookupRequest(id);
                        System.out.println("Request: " + request);
                        LookupResult result = identityService.lookup(request);
                        System.out.println("Response: " + result);
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
        System.out.println("ResponseCode: " + resCode);
        System.out.println("Response: " + response);
        exchange.sendResponseHeaders(resCode, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
        exchange.close();
    }
}
