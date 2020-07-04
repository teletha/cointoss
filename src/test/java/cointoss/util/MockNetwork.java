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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

import com.google.gson.JsonElement;

import kiss.Signal;
import kiss.Signaling;
import okhttp3.Request;

/**
 * @version 2018/04/30 10:38:28
 */
public class MockNetwork extends Network {

    /** The mocked response manager. */
    private final Map<String, MockResponse> responses = new HashMap();

    /** The mocked websocket manager. */
    private final Map<String, MockSocket> websockets = new HashMap();

    /**
     * {@inheritDoc}
     */
    @Override
    public final <M> Signal<M> rest(Request request, APILimiter limiter, Class<M> type, String[] selector) {
        String path = request.url().encodedPath();
        MockResponse mock = responses.get(path);

        if (mock == null) {
            throw new AssertionError("[" + path + "] requires valid response.");
        } else {
            return new Signal<>((observer, disposer) -> {
                observer.accept((M) mock.response());
                observer.complete();
                return disposer;
            });
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final Signal<JsonElement> jsonRPC(String uri, String channelName) {
        return websockets.computeIfAbsent(uri, key -> new MockSocket()).signaling.expose;
    }

    /**
     * Describe mockable response for the specified request.
     * 
     * @param path A request path to mock.
     * @return A mock description..
     */
    public MockResponse request(String path) {
        return responses.computeIfAbsent(path, key -> new MockResponse());
    }

    /**
     * Describe mockable websocket for the specified request.
     * 
     * @param uri
     * @return
     */
    public MockSocket connect(String uri) {
        return websockets.computeIfAbsent(uri, key -> new MockSocket());
    }

    /**
     * @version 2018/04/30 13:44:48
     */
    public static class MockResponse {

        /** The dummy responses. */
        private final Queue response = new LinkedList();

        /**
         * Describe the reseponse value.
         * 
         * @param value
         */
        public void willResponse(Object... values) {
            Objects.requireNonNull(values);

            for (Object value : values) {
                response.add(value);
            }
        }

        /**
         * Return your response.
         * 
         * @return
         */
        private Object response() {
            return response.poll();
        }
    }

    /**
     * @version 2018/04/30 14:15:25
     */
    public static class MockSocket {

        private final Signaling<JsonElement> signaling = new Signaling();

        /**
         * Describe the reseponse value.
         * 
         * @param value
         */
        public void willResponse(JsonElement data) {
            signaling.accept(data);
        }
    }
}