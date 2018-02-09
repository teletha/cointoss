/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

/**
 * @version 2018/01/28 23:46:13
 */
public class Websocket {

    /**
     * @param args
     */
    public static void main(String[] args) {
        // PING PONG
        // GET XMLHTTPequest
        // https://lightning.bitflyer.jp/signalr/ping?account_id=ID&token=TOKEN&products=FX_BTC_JPY,heartbeat&_=1518066411185

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("wss").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);

        client.dispatcher().executorService().shutdown();
    }

    private static final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            System.out.println("OPEN");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            System.out.println(text);
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes) {
            System.out.println(bytes.hex());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(NORMAL_CLOSURE_STATUS, null);
            System.out.println("Close " + reason);
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            System.out.println("Error " + t.getMessage());
        }
    }
}
