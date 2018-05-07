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
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.channels.CompletionHandler;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;

import kiss.I;

/**
 * @version 2018/05/07 9:51:11
 */
public class LogWriter implements CompletionHandler<Integer, ByteBuffer> {

    /** The log encoding. */
    private static final Charset ISO = StandardCharsets.ISO_8859_1;

    private final AsynchronousFileChannel channel;

    private final ConcurrentLinkedQueue<String> messages = new ConcurrentLinkedQueue();

    private long size;

    private ByteBuffer buffer = ByteBuffer.allocate(1024 * 8);

    /**
     * @param file
     */
    private LogWriter(Path file) {
        try {
            this.channel = AsynchronousFileChannel.open(file, APPEND, CREATE);
            this.size = channel.size();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void completed(Integer result, ByteBuffer buffer) {
        if (messages.isEmpty() == false) {
            size += result;

            buffer.rewind();
            buffer.put(messages.poll().getBytes(ISO));
            buffer.flip();

            channel.write(buffer, size, buffer, this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
    }

    public static void main(String[] args) {

    }
}
