/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import cointoss.Execution;
import cointoss.Side;
import cointoss.util.Num;
import filer.Filer;
import kiss.I;
import kiss.Signal;

/**
 * @version 2018/04/20 19:04:42
 */
public class Log {

    /** The buffer size (kb). */
    private static final int size = 16;

    /**
     * Read {@link Execution} log extremely fast.
     * 
     * @param file
     * @return
     */
    private Signal<Execution> read(Path file) {
        return new Signal<>((observer, disposer) -> {
            CharsetDecoder decoder = StandardCharsets.ISO_8859_1.newDecoder();
            ByteBuffer bytes = ByteBuffer.allocate(1024 * size);
            CharBuffer chars = CharBuffer.allocate(1024 * (size + 1));
            char[] array = chars.array();

            try (FileChannel channel = (FileChannel) Files.newByteChannel(file)) {
                Execution execution = new Execution();

                root: while (channel.read(bytes) != -1) {
                    int count = 0;

                    bytes.flip();
                    decoder.decode(bytes, chars, true);
                    chars.flip();
                    int last = 0;

                    for (int i = 0, end = chars.limit(); i < end; i++) {
                        char c = chars.get();

                        if (c == ' ' || c == '\r') {
                            String value = new String(array, last, i - last);
                            last = i + 1;

                            switch (count++) {
                            case 0:
                                execution.id = Long.parseLong(value);
                                break;

                            case 1:
                                execution.exec_date = LocalDateTime.parse(value).atZone(cointoss.util.Chrono.UTC);
                                break;

                            case 2:
                                execution.side = Side.parse(value);
                                break;

                            case 3:
                                execution.price = Num.of(value);
                                break;

                            case 4:
                                execution.size = execution.cumulativeSize = Num.of(value);
                                break;

                            case 5:
                                execution.buy_child_order_acceptance_id = value;
                                break;

                            case 6:
                                execution.sell_child_order_acceptance_id = value;
                                observer.accept(execution);

                                // skip next '\n'
                                i++;
                                last++;
                                chars.get();

                                if (chars.remaining() < 124) {
                                    chars.compact();
                                    bytes.clear();
                                    continue root;
                                }
                                count = 0;
                                execution = new Execution();
                            }
                        }
                    }
                    bytes.clear();
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
            return disposer;
        });
    }

    private Signal<Execution> read2(Path file) {
        return Filer.read(file).map(Execution::new);
    }

    public static void main(String[] args) {
        Log log = new Log();
        Path file = Paths.get("F:\\Development\\CoinToss\\.log\\bitflyer\\FX_BTC_JPY\\execution20180404.log");

        long start = System.currentTimeMillis();
        log.read(file).to(v -> {
        });
        long end = System.currentTimeMillis();
        System.out.println(end - start);
    }
}
