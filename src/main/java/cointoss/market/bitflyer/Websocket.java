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
        // POST https://lightning.bitflyer.jp/api/trade/sendorder
        //
        /*
         * <pre> Host: lightning.bitflyer.jp User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64;
         * rv:58.0) Gecko/20100101 Firefox/58.0 Accept: Accept-Language: ja,en;q=0.7,en-US;q=0.3
         * Accept-Encoding: gzip, deflate, br Referer: https://lightning.bitflyer.jp/trade
         * content-type: application/json; charset=utf-8 x-requested-with: XMLHttpRequest origin:
         * https://lightning.bitflyer.jp Content-Length: 222 Cookie:
         * __cfduid=d07298140eae875de205cfd1a36b63a541514707369; region=JP;
         * device_token=d0433b25-15f9-432c-babd-c524ea00b99b; language=ja; important_notice_1=true;
         * ai_user=O5Vfe|2017-12-31T10:36:22.868Z; _ga=GA1.2.1826611599.1514716583;
         * _gid=GA1.2.321104742.1514716583; _ga=GA1.3.1826611599.1514716583;
         * _gid=GA1.3.321104742.1514716583; _tdim=b1f7f046-ba37-4a38-8e42-214b01178f29;
         * visid_incap_471748=al93NpnETbqbDg9k3U+v6irFSFoAAAAAQUIPAAAAAAALwT3WslQYcEj7Ueem+2wv;
         * visid_incap_259540=LTss5z8bQAa5g38bySciDXHGSFoAAAAAQUIPAAAAAABh/SllfvzyDi/ZZrz1LBoc;
         * important_notice_2=true; __RequestVerificationToken=
         * VpcDOsyEQ8Hdvim9mMbVxft7S69UwduOAtXq8qB2LT1_cNcDYLJT5kKr0YBR3FOVnCFNZsLJRnA-
         * fnJF5IBLLpF6pn81; ASP.NET_SessionId=3ibazwffp1awiysmo1jzxueb;
         * history_id=e46bf2df-a7c8-4aa7-82d6-69afc0a4a90c;
         * api_session=7GvdSFqglnvfS6OzhPD1X%2fqo01%2bUeh6FcHDWZnXcQuk%3d;
         * nlbi_259540=pBkZSKrTpDTcDQsueCemewAAAAC+tSc5P2gRF2YTUcdDDE+s;
         * incap_ses_432_259540=FZfIO2W9pVzgo3B+v8b+BZ9KbVoAAAAAjqZT4N1HkjpmxwPHWReQPw==;
         * ai_session=/HRvR|1517098441369|1517150669497.28;
         * incap_ses_432_471748=1YqKcXSRDUt3dnB+v8b+BUVKbVoAAAAAiCK8nBLbzkaTKAOaCgLrMA==;
         * nlbi_471748=F5+teWbas2u3khU4kD9wKQAAAAA5KWsFGnL/w2SJWcp+lnP6 Connection: keep-alive
         * </pre>
         */
        // REQUEST
        // account_id 固定
        // lang ja
        // minuteToExpire 43200
        // ord_type LIMIT
        // order_ref_id JRF20180128-235909-006232
        // price 1178559
        // product_code FX_BTC_JPY
        // side BUY
        // size 0.01
        // time_in_force GTC
        // RESPONSE
        // {"status":0,"error_message":null,"data":{"order_ref_id":"JRF20180128-234437-373993"}}

        // generateOrderReferenceId=function(){
        // return["JRF",moment().format("YYYYMMDD-HHmmss"),"-",("000000"+(Math.random()*1e6|0)).slice(-6)].join("")
        // }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://echo.websocket.org").build();
        EchoWebSocketListener listener = new EchoWebSocketListener();
        WebSocket ws = client.newWebSocket(request, listener);

        client.dispatcher().executorService().shutdown();
    }

    private static final class EchoWebSocketListener extends WebSocketListener {
        private static final int NORMAL_CLOSURE_STATUS = 1000;

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            webSocket.send("Hello, it's SSaurel !");
            webSocket.send("What's up ?");
            webSocket.send(ByteString.decodeHex("deadbeef"));
            webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !");
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
