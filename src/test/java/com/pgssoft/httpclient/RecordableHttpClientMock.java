/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package com.pgssoft.httpclient;

import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.Builder;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map.Entry;

import com.pgssoft.httpclient.internal.debug.Debugger;
import com.pgssoft.httpclient.internal.rule.Rule;

import kiss.I;

public class RecordableHttpClientMock {

    /**
     * Build enhanced {@link HttpClientMock}.
     * 
     * @return
     */
    public static HttpClientMock build() {
        return new HttpClientMock("", new RecodingDebugger());
    }

    /**
     * 
     */
    private static class RecodingDebugger extends Debugger {

        /**
         * {@inheritDoc}
         */
        @Override
        public void debug(List<Rule> rules, HttpRequest request) {
            super.debug(rules, request);

            if (rules.isEmpty()) {
                Builder builder = HttpRequest.newBuilder(request.uri());
                builder.expectContinue(request.expectContinue());
                builder.timeout(request.timeout().orElse(Duration.of(10, ChronoUnit.SECONDS)));
                builder.version(request.version().orElse(Version.HTTP_2));
                for (Entry<String, List<String>> entry : request.headers().map().entrySet()) {
                    String key = entry.getKey();
                    if (key.equalsIgnoreCase("Set-Cookie")) {
                        for (String value : entry.getValue()) {
                            builder.header(key, value);
                        }
                    } else {
                        builder.header(entry.getKey(), String.join(",", entry.getValue()));
                    }
                }
                switch (request.method()) {
                case "POST":
                    builder.POST(request.bodyPublisher().orElseThrow());
                    break;

                case "PUT":
                    builder.PUT(request.bodyPublisher().orElseThrow());
                    break;

                case "DELETE":
                    builder.DELETE();
                    break;

                default:
                    builder.GET();
                    break;
                }

                I.http(builder, String.class).waitForTerminate().to(json -> {
                    System.out.println(format(json, 2));
                }, e -> {
                    System.out.println(e);
                });
            }
        }

        private String format(String json, int indentWidth) {
            char[] chars = json.toCharArray();
            String newline = System.lineSeparator();
            StringBuilder builder = new StringBuilder();

            boolean beginQuotes = false;

            for (int i = 0, indent = 0; i < chars.length; i++) {
                char c = chars[i];

                if (c == '\"') {
                    builder.append(c);
                    beginQuotes = !beginQuotes;
                    continue;
                }

                if (!beginQuotes) {
                    switch (c) {
                    case '{':
                    case '[':
                        builder.append(c).append(newline).append(String.format("%" + (indent += indentWidth) + "s", ""));
                        continue;
                    case '}':
                    case ']':
                        builder.append(newline).append((indent -= indentWidth) > 0 ? String.format("%" + indent + "s", "") : "").append(c);
                        continue;
                    case ':':
                        builder.append(c).append(" ");
                        continue;
                    case ',':
                        builder.append(c).append(newline).append(indent > 0 ? String.format("%" + indent + "s", "") : "");
                        continue;
                    default:
                        if (Character.isWhitespace(c)) continue;
                    }
                }

                builder.append(c).append(c == '\\' ? "" + chars[++i] : "");
            }
            return builder.toString();
        }
    }
}