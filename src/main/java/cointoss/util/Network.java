/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;

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

public class Network {

    /** The logging system. */
    private static final org.apache.logging.log4j.Logger log = LogManager.getLogger(Network.class);

    /** The timeout duration. */
    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /** The proxy server. */
    private static Proxy proxy;

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
                    .connectTimeout(TIMEOUT)
                    .readTimeout(TIMEOUT)
                    .writeTimeout(TIMEOUT)
                    .callTimeout(TIMEOUT)
                    .pingInterval(Duration.ofMinutes(3))
                    .proxy(proxy)
                    .build();

        }
        return client;
    }

    /**
     * Use proxy server.
     * 
     * @param uri
     * @param port
     */
    public static void proxy(String uri, int port) {
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(uri, port));
        log.info("Use proxy " + uri + ":" + port);
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
    public final Signal<JsonElement> rest(Request request) {
        return rest(request, null);
    }

    /**
     * Call REST API.
     */
    public Signal<JsonElement> rest(Request request, APILimiter limiter) {
        return new Signal<>((observer, disposer) -> {
            if (limiter != null) {
                limiter.acquire();
            }

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
                observer.error(new Error("[" + request.url() + "] throws error : " + e.getMessage(), e));
            }
            return disposer;
        });
    }

    /**
     * Call REST API.
     */
    public final <M> Signal<M> rest(Request request, Class<M> type, String... selector) {
        return rest(request, null, type, selector);
    }

    /**
     * Call REST API.
     */
    public <M> Signal<M> rest(Request request, APILimiter limiter, Class<M> type, String... selector) {
        return new Signal<>((observer, disposer) -> {
            if (limiter != null) {
                limiter.acquire();
            }

            try (Response response = client().newCall(request).execute(); ResponseBody body = response.body()) {
                String value = body.string();
                int code = response.code();

                if (code == 200) {
                    if (type == null) {
                        observer.accept(null);
                    } else {
                        JSON json = I.json(value);

                        if (selector == null || selector.length == 0) {
                            observer.accept(json.to(type));
                        } else {
                            json.find(selector).map(v -> v.to(type)).to(observer);
                        }
                    }
                    observer.complete();
                } else {
                    observer.error(new Error("[" + request.url() + "] HTTP Status " + code + " " + value));
                }
            } catch (Throwable e) {
                observer.error(new Error("[" + request.url() + "] throws error : " + e.getMessage(), e));
            }
            return disposer;
        });
    }

    /**
     * Connect by websocket.
     * 
     * @param uri
     * @param jsonCommnad
     * @return
     */
    public Signal<JsonElement> websocket(String uri, Object jsonCommnad) {
        return new Signal<JsonElement>((observer, disposer) -> {
            JsonParser parser = new JsonParser();
            Request request = new Request.Builder().url(uri).build();

            WebSocket websocket = client().newWebSocket(request, new WebSocketListener() {

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onOpen(WebSocket socket, Response response) {
                    socket.send(I.write(jsonCommnad));
                }

                /**
                 * {@inheritDoc}
                 */
                @Override
                public void onMessage(WebSocket socket, String text) {
                    observer.accept(parser.parse(text));
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
                public void onClosed(WebSocket webSocket, int code, String reason) {
                    System.out.println("Closed " + code + "   " + reason);
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
     * JsonRPC model.
     */
    @SuppressWarnings("unused")
    private static class JsonRPC {

        public long id = 123;

        public String jsonrpc = "2.0";

        public String method;

        public Map<String, String> params = new HashMap();
    }

    /**
     * Call LINE notify API.
     * 
     * @param message A message to send
     * @param token Notify API token.
     */
    public Signal<?> line(Object title, Object message, String token) {
        if (token != null) {
            Request request = new Request.Builder().url("https://notify-api.line.me/api/notify")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(RequestBody.create(MediaType
                            .parse("application/x-www-form-urlencoded; charset=utf-8"), "message=" + title + "\r\n" + message))
                    .build();

            return rest(request, null, new String[0]);
        } else {
            return I.signal();
        }
    }
}