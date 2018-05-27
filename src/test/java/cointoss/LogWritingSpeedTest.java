/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import static java.nio.file.StandardOpenOption.*;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import antibug.CleanRoom;

/**
 * @version 2018/05/19 9:21:21
 */
public class LogWritingSpeedTest {

    static final int size = 1000000;

    static final int bufferSize = 5000;

    static final List<Execution> exes = MarketTestSupport.executionRandomly(size).toList();

    @RegisterExtension
    CleanRoom room = new CleanRoom();

    @Test
    void single() throws IOException {
        Path file = room.locateFile("single");
        Writer writer = Files.newBufferedWriter(file);

        long start = System.currentTimeMillis();

        for (int i = 0; i < exes.size(); i++) {
            writer.append(exes.get(i) + "\r\n");
            // writer.flush();
        }
        writer.close();
        long end = System.currentTimeMillis();
        System.out.println("SINGLE : " + (end - start) + "   " + Files.size(file));
    }

    @Test
    void multiline() throws IOException {
        Path file = room.locateFile("multiple");
        Writer writer = Files.newBufferedWriter(file);

        long start = System.currentTimeMillis();

        long now = 0;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < exes.size(); i++) {
            builder.append(exes.get(i)).append("\r\n");

            if (++now == bufferSize) {
                writer.append(builder.toString());
                writer.flush();
                now = 0;
                builder = new StringBuilder();
            }
        }
        writer.close();
        long end = System.currentTimeMillis();
        System.out.println("MULTI : " + (end - start) + "   " + Files.size(file));
    }

    @Test
    void asyncMulti() throws IOException {
        Path file = room.locateFile("async");
        AsynchronousFileChannel writer = AsynchronousFileChannel.open(file, WRITE);

        long start = System.currentTimeMillis();
        long position = 0;
        long now = 0;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < exes.size(); i++) {
            builder.append(exes.get(i)).append("\r\n");

            if (++now == bufferSize) {
                ByteBuffer wrap = ByteBuffer.wrap(builder.toString().getBytes());

                writer.write(wrap, position);
                position += builder.length();
                now = 0;
                builder = new StringBuilder();
            }
        }
        writer.close();
        long end = System.currentTimeMillis();
        System.out.println("AsyncMulti : " + (end - start) + "   " + Files.size(file));
    }

    @Test
    void bytebuffer() throws IOException {
        Path file = room.locateFile("bytebuffer");
        FileChannel writer = FileChannel.open(file, APPEND, CREATE);

        long start = System.currentTimeMillis();
        long now = 0;
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < exes.size(); i++) {
            builder.append(exes.get(i)).append("\r\n");

            if (++now == bufferSize) {
                CharBuffer cb = CharBuffer.wrap("aaa");
                ByteBuffer wrap = ByteBuffer.wrap(builder.toString().getBytes());
                writer.write(wrap);
                now = 0;
                builder = new StringBuilder();
            }
        }
        writer.close();
        long end = System.currentTimeMillis();
        System.out.println("Byte : " + (end - start) + "   " + Files.size(file));
    }
}
