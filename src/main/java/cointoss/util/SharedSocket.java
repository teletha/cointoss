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

import java.net.http.WebSocket;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import kiss.Disposable;
import kiss.I;
import kiss.JSON;
import kiss.Signal;
import kiss.WiseFunction;

public abstract class SharedSocket {

    /** The communication route. */
    protected final Signal<JSON> expose;

    /** The cached connection. */
    private WebSocket ws;

    /** Temporary buffer for commands called before the connection was established. */
    private Deque queued = new ArrayDeque();

    /**
     * Build websocket client.
     * 
     * @param uri
     */
    protected SharedSocket(String uri, WiseFunction<String, JSON> mapper) {
        this.expose = I.http(uri, ws -> {
            synchronized (this) {
                this.ws = ws;
                for (Object command : queued) {
                    invoke(command);
                }
                queued = null;
            }
        }).map(mapper).share();
    }

    /**
     * Execute command on this connection.
     * 
     * @param command A commnad entity (i.e. bena-like object).
     * @return A shared connection.
     */
    protected final synchronized Signal<JSON> invoke(Object command) {
        if (ws == null) {
            queued.add(command);
        } else {
            ws.sendText(I.write(command), true);
        }
        return expose;
    }

    /**
     * Close this connection.
     */
    public final void close() {
        if (ws != null) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "");
        }
    }

    /**
     * JsonRPC client.
     */
    public static class JsonRPC extends SharedSocket {

        /**
         * Build JsonRPC client.
         * 
         * @param uri
         */
        public JsonRPC(String uri) {
            super(uri, text -> I.json(text).get("params"));
        }

        /**
         * Subscribe channel.
         * 
         * @param channel
         * @return
         */
        public Signal<JSON> subscribe(String channel) {
            return invoke(new Command("subscribe", channel)).effectOnDispose(() -> invoke(new Command("unsubscribe", channel)))
                    .map(json -> {
                        if (json != null && json.has("channel", channel)) {
                            return json.get("message");
                        } else {
                            return null;
                        }
                    })
                    .skipNull();
        }

        /**
         * Entity for command.
         */
        private static class Command {

            public long id = 123;

            public String jsonrpc = "2.0";

            public String method;

            public Map<String, String> params = new HashMap();

            /**
             * Hide constructor.
             * 
             * @param method
             * @param channel
             */
            private Command(String method, String channel) {
                this.method = method;
                this.params.put("channel", channel);
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        JsonRPC ws = new JsonRPC("wss://ws.lightstream.bitflyer.com/json-rpc");
        Disposable disposable = ws.subscribe("lightning_executions_FX_BTC_JPY").flatIterable(json -> json.find("*")).to(v -> {
            System.out.println(v.as(String.class));
        }, e -> {
            e.printStackTrace();
        });

        Thread.sleep(1000 * 2);
        Disposable disposable2 = ws.subscribe("lightning_board_FX_BTC_JPY").to(v -> {
            System.out.println(v.as(String.class));
        });

        Thread.sleep(1000 * 10);
        disposable.dispose();
        System.out.println("Dispose1");
        Thread.sleep(1000 * 10);
        disposable2.dispose();
        System.out.println("Dispose2");

        Thread.sleep(1000 * 10);
    }
}
