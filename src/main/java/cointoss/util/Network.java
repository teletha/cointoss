/*
 * Copyright (C) 2023 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;

import kiss.I;
import kiss.JSON;
import kiss.Signal;

public class Network {

    /**
     * Call REST API.
     */
    public static Signal<JSON> rest(HttpRequest.Builder request, APILimiter limiter, HttpClient... client) {
        return rest(request, limiter, 1, client);
    }

    /**
     * Call REST API.
     */
    public static Signal<JSON> rest(HttpRequest.Builder request, APILimiter limiter, int weight, HttpClient... client) {
        return new Signal<>((observer, disposer) -> {
            if (limiter != null) limiter.acquire(weight);

            return I.http(request.timeout(Duration.ofSeconds(15)), JSON.class, client).to(observer, disposer);
        });
    }

    /**
     * Call LINE notify API.
     * 
     * @param message A message to send
     * @param token Notify API token.
     */
    public static Signal<?> line(Object title, Object message, String token) {
        if (token != null) {
            Builder request = HttpRequest.newBuilder()
                    .uri(URI.create("https://notify-api.line.me/api/notify"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                    .POST(BodyPublishers.ofString("message=" + title + "\r\n" + message));

            return I.http(request, JSON.class);
        } else {
            return I.signal();
        }
    }
}