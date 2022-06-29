package com.zoho.catalyst.auth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

public class Oauth2HttpHandler implements HttpHandler {
    private static String code;
    private static String location;

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        Map<String, String> map = new HashMap<>();
        String query = httpExchange.getRequestURI().getQuery();
        for (String kv : query.split("&")) {
            String[] temp = kv.split("=");
            map.put(temp[0], URLDecoder.decode(temp[1], "utf-8"));
        }
        if(!map.containsKey("code") || !map.containsKey("location")) {
            String response = "Unable to process the request";
            httpExchange.sendResponseHeaders(400, response.length());
            try(OutputStream os = httpExchange.getResponseBody()) {
                os.write(response.getBytes());
            }
            return;
        }
        Oauth2HttpHandler.code = map.get("code");
        Oauth2HttpHandler.location = map.get("location");

        String response = "Processing completed successfully";
        httpExchange.sendResponseHeaders(200, response.length());
        try(OutputStream os = httpExchange.getResponseBody()) {
            os.write(response.getBytes());
        }
    }

    public static String getCode() {
        return Oauth2HttpHandler.code;
    }

    public static String getLocation() {
        return Oauth2HttpHandler.location;
    }
}
