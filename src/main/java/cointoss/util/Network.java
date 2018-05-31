/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.util.HashMap;
import java.util.Map;

import com.github.signalr4j.client.LogLevel;
import com.github.signalr4j.client.Logger;
import com.github.signalr4j.client.hubs.HubConnection;
import com.github.signalr4j.client.hubs.HubProxy;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import kiss.I;
import kiss.JSON;
import kiss.Signal;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * @version 2018/04/30 0:14:00
 */
public class Network {

    /** The singleton. */
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Call private API.
     */
    public <M> Signal<M> rest(Request request, String selector, Class<M> type) {
        return new Signal<>((observer, disposer) -> {
            try (Response response = client.newCall(request).execute()) {
                int code = response.code();
                String value = response.body().string();

                if (code == 200) {
                    if (type == null) {
                        observer.accept(null);
                    } else {
                        JSON json = I.json(value);

                        if (selector == null || selector.isEmpty()) {
                            observer.accept(json.to(type));
                        } else {
                            json.find(selector, type).to(observer);
                        }
                        observer.complete();
                    }
                } else {
                    observer.error(new Error("[" + request.url() + "] HTTP Status " + code + " " + value));
                }
            } catch (Throwable e) {
                observer.error(new Error("[" + request.url() + "] throws some error.", e));
            }
            return disposer;
        });
    }

    /**
     * Connect by websocket.
     * 
     * @param uri
     * @param channelName
     * @return
     */
    public Signal<JsonElement> jsonRPC(String uri, String channelName) {
        return new Signal<>((observer, disposer) -> {
            JsonParser parser = new JsonParser();
            Request request = new Request.Builder().url(uri).build();

            WebSocket websocket = client.newWebSocket(request, new WebSocketListener() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onOpen(WebSocket socket, Response response) {
                    JsonRPC invoke = new JsonRPC();
                    invoke.method = "subscribe";
                    invoke.params.put("channel", channelName);

                    socket.send(I.write(invoke));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onMessage(WebSocket socket, String text) {
                    JsonObject e = parser.parse(text).getAsJsonObject();
                    JsonObject params = e.getAsJsonObject("params");

                    if (params != null) {
                        observer.accept(params.get("message"));
                    }
                }
            });

            return disposer.add(() -> {
                websocket.cancel();
                client.dispatcher().executorService().shutdown();
                client.connectionPool().evictAll();
            });
        });
    }

    /**
     * @version 2018/04/26 21:09:09
     */
    @SuppressWarnings("unused")
    private static class JsonRPC {

        public long id;

        public String jsonrpc = "2.0";

        public String method;

        public Map<String, String> params = new HashMap();
    }

    /**
     * Connect by Signalr.
     * 
     * @param uri
     * @param query
     * @param channel
     * @return
     */
    public Signal<JsonElement> signalr(String uri, String query, String channel, String event) {
        return new Signal<>((observer, disposer) -> {
            // Connect to the server
            HubConnection connection = new HubConnection(uri, query, true, new NullLogger());
            connection.error(observer::error);
            HubProxy proxy = connection.createHubProxy(channel);
            proxy.subscribe(event).addReceivedHandler(array -> {
                for (JsonElement e : array) {
                    observer.accept(e);
                }
            });

            // Start the connection
            connection.start();

            return disposer.add(() -> {
                connection.disconnect();
            });
        });
    }

    /**
     * @version 2018/04/29 7:56:01
     */
    private static class NullLogger implements Logger {

        /**
         * {@inheritDoc}
         */
        @Override
        public void log(String message, LogLevel level) {
        }
    }
}
