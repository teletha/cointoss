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

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;

import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class Network {

    /**
     * Call REST API.
     */
    public final Signal<JSON> rest(HttpRequest.Builder request) {
        return rest(request, null);
    }

    /**
     * Call REST API.
     */
    public final Signal<JSON> rest(HttpRequest.Builder request, APILimiter limiter) {
        return new Signal<>((observer, disposer) -> {
            if (limiter != null) limiter.acquire();

            return I.http(request, JSON.class).to(observer, disposer);
        });
    }

    /**
     * Call REST API.
     */
    public final <M> Signal<M> rest(HttpRequest.Builder request, Class<M> type, String... selector) {
        return rest(request, null, type, selector);
    }

    /**
     * Call REST API.
     */
    public <M> Signal<M> rest(HttpRequest.Builder request, APILimiter limiter, Class<M> type, String... selector) {
        return new Signal<>((observer, disposer) -> {
            if (limiter != null) limiter.acquire();

            return I.http(request, JSON.class).flatIterable(json -> json.find(type, selector)).to(observer, disposer);
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
            Builder request = HttpRequest.newBuilder()
                    .uri(URI.create("https://notify-api.line.me/api/notify"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                    .POST(BodyPublishers.ofString("message=" + title + "\r\n" + message));

            return rest(request, JSON.class);
        } else {
            return I.signal();
        }
    }

    /**
     * Create {@link HttpRequest.Builder}.
     * 
     * @param uri
     */
    public static HttpRequest.Builder request(String... uri) {
        return HttpRequest.newBuilder(URI.create(String.join("", uri)));
    }
}