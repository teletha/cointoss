/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
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
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.Buffer;
import trademate.setting.Notificator;

public class Network {

    /** The timeout duration. */
    private static final long TIMEOUT = 5;

    /** The singleton. */
    private static OkHttpClient client;

    /**
     * Retrieve the client.
     * 
     * @return The singleton.
     */
    private static synchronized OkHttpClient client() {
        if (client == null) {
            client = new OkHttpClient.Builder() //
                    .connectTimeout(TIMEOUT, SECONDS)
                    .readTimeout(TIMEOUT, SECONDS)
                    .writeTimeout(TIMEOUT, SECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
        }
        return client;
    }

    /**
     * Terminate all network resources forcibly.
     */
    public static synchronized void terminate() {
        if (client != null) {
            client.dispatcher().executorService().shutdownNow();
            client.connectionPool().evictAll();
            client = null;
        }
    }

    /**
     * For debug.
     * 
     * @param request
     * @return
     */
    @SuppressWarnings("unused")
    private static String bodyToString(final Request request) {
        try {
            RequestBody body = request.body();

            if (body == null) {
                return "";
            }
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    /**
     * Call REST API.
     */
    public Signal<JsonElement> rest(Request request) {
        return new Signal<>((observer, disposer) -> {
            // System.out.println(request + " " + bodyToString(request));
            try (Response response = client().newCall(request).execute(); ResponseBody body = response.body()) {
                String value = body.string();
                int code = response.code();

                if (code == 200) {
                    observer.accept(new JsonParser().parse(value));
                    observer.complete();
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
     * Call REST API.
     */
    public <M> Signal<M> rest(Request request, String selector, Class<M> type) {
        return new Signal<>((observer, disposer) -> {
            // System.out.println(request + " " + bodyToString(request));
            try (Response response = client().newCall(request).execute(); ResponseBody body = response.body()) {
                String value = body.string();
                int code = response.code();

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
                    }
                    observer.complete();
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
        return new Signal<JsonElement>((observer, disposer) -> {
            JsonParser parser = new JsonParser();
            Request request = new Request.Builder().url(uri).build();

            WebSocket websocket = client().newWebSocket(request, new WebSocketListener() {

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

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onClosing(WebSocket socket, int code, String reason) {
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onClosed(WebSocket socket, int code, String reason) {
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onFailure(WebSocket socket, Throwable error, Response response) {
                    observer.error(error);
                }
            });

            return disposer.add(() -> {
                websocket.close(1000, null);
            });
        });
    }

    /**
     * @version 2018/04/26 21:09:09
     */
    @SuppressWarnings("unused")
    private static class JsonRPC {

        public long id = 123;

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

    /**
     * Call LINE notify API.
     * 
     * @param message A message to send
     */
    public Signal<?> line(CharSequence message) {
        Notificator notificator = I.make(Notificator.class);

        if (notificator.lineAccessToken.isPresent()) {
            Request request = new Request.Builder().url("https://notify-api.line.me/api/notify")
                    .addHeader("Authorization", "Bearer " + notificator.lineAccessToken)
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=utf-8"), "message=\r\n" + message))
                    .build();

            return rest(request, "", null);
        } else {
            return I.signal();
        }
    }
}
