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

import java.nio.charset.StandardCharsets;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

/**
 * @version 2018/01/29 8:31:46
 */
public class HTTPLogin {

    public static void main(String[] args) {
        String path = "https://lightning.bitflyer.jp/";
        HttpPost request = new HttpPost(path.startsWith("https://") ? path : path);

        try (CloseableHttpClient client = HttpClientBuilder.create().build(); //
                CloseableHttpResponse response = client.execute(request)) {

            int status = response.getStatusLine().getStatusCode();
            String value = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            if (status == HttpStatus.SC_OK) {
                System.out.println(value);
            } else {
                throw new Error("HTTP Status " + status + " " + value);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
