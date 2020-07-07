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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.time.Duration;

import org.apache.logging.log4j.LogManager;

import kiss.I;
import kiss.JSON;
import kiss.Signal;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

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
     * Call REST API.
     */
    public final Signal<JSON> rest(Request request) {
        return rest(request, null);
    }

    /**
     * Call REST API.
     */
    public Signal<JSON> rest(Request request, APILimiter limiter) {
        return new Signal<>((observer, disposer) -> {
            if (limiter != null) {
                limiter.acquire();
            }

            try (Response response = client().newCall(request).execute(); ResponseBody body = response.body()) {
                String value = body.string();
                int code = response.code();

                if (code == 200) {
                    observer.accept(I.json(value));
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
                            observer.accept(json.as(type));
                        } else {
                            json.find(type, selector).forEach(observer);
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