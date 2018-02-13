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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;

import filer.Filer;
import kiss.I;
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

    private final OkHttpClient client = new OkHttpClient();

    /**
     * 
     */
    private Websocket() {
        List<String> lines = Filer.read(".log/bitflyer/key.txt").toList();
        String accountId = lines.get(4);

        // negotiate
        try {
            I.signal(new URL("https://lightning.bitflyer.jp/signalr/negotiate?connectionData=[{name:\"bfexhub\"}]"))
                    .map(I::json)
                    .map(json -> json.to(Negotiate.class))
                    .to(n -> {
                        OkHttpClient client = new OkHttpClient.Builder().readTimeout(0, TimeUnit.MILLISECONDS).build();

                        StringBuilder uri = new StringBuilder();
                        uri.append("transport=webSockets");
                        uri.append("&groupsToken=TDbcbzXGIwdhJnI1U8X0W25S7VS7kKcfRqHb0EflWOu3IMoSHWIgZXzYi%2FIA0RXMmNkhyO5AXgyqUQmbonLsu0X%2FMr83gvpdHZoN%2FGZJbBvaYZyv%2F%2B7fk4Tl2ey1g5bqf%2BGg9W2DDKBRSDf9Jz2wmCaCYPs%2FwyO85V6s98M1aeu6L3R4ZBdJ2PEerLUDPuMBGEglE4L5YZnwKqfzpv28eq6iFz50g%2BzdKatXsF6cfC1tbUGD");
                        uri.append("&messageId=???");
                        uri.append("&clientProtocol=1.5");
                        uri.append("&account_id=" + accountId);
                        uri.append("&token=???");
                        uri.append("&products=FX_BTC_JPY%2Cheartbeat");
                        uri.append("&connectionToken=" + n.ConnectionToken);
                        uri.append("&connectionData=%5B%7B%22name%22%3A%22bfexhub%22%7D%5D");
                        uri.append("&tid=10");
                        System.out.println(n);
                        System.out.println(uri);
                        Request request = new Request.Builder().url("wss://lightning.bitflyer.jp/signalr/connect?" + uri.toString())
                                .build();
                        client.newWebSocket(request, new EchoWebSocketListener());
                        // Trigger shutdown of the dispatcher's executor so this process can
                        // exit cleanly.
                        client.dispatcher().executorService().shutdown();

                    });
        } catch (MalformedURLException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @version 2018/02/12 12:50:38
     */
    private static class Negotiate {

        public String ConnectionToken;

        public String ConnectionId;

        public float KeepAliveTimeout;

        public float DisconnectTimeout;

        public float ConnectionTimeout;

        public float TransportConnectTimeout;

        public boolean TryWebSockets;

        public String ProtocolVersion;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return "Negotiate [ConnectionToken=" + ConnectionToken + ", ConnectionId=" + ConnectionId + ", KeepAliveTimeout=" + KeepAliveTimeout + ", DisconnectTimeout=" + DisconnectTimeout + ", ConnectionTimeout=" + ConnectionTimeout + ", TryWebSockets=" + TryWebSockets + ", ProtocolVersion=" + ProtocolVersion + ", TransportConnectTimeout=" + TransportConnectTimeout + "]";
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Websocket websocket = new Websocket();
    }

    private static final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            System.out.println("OPEN");
            try {
                System.out.println(response.body().string());
            } catch (IOException e) {
                throw I.quiet(e);
            }
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
