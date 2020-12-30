/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.db;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.junit.jupiter.api.Test;

class FileTest {

    @Test
    void testName() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(8).putDouble(-1.210);

        // int -> bytes
        byte[] bytes = buffer.array();
        // bytes -> int
        double num = ByteBuffer.wrap(bytes).getDouble();

        System.out.print("bytes: ");
        for (byte b : bytes) {
            System.out.printf("%x ", b);
        }
        System.out.println();

        System.out.print("int: ");
        System.out.println(num);

        FileChannel channel = FileChannel.open(Path.of("test.txt"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        buffer.flip();
        channel.write(buffer);
        channel.close();
    }
}
