/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.execution;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

import kiss.I;
import psychopath.File;

class NormalLog implements AutoCloseable {

    private final SeekableByteChannel file;

    /**
     * 
     */
    NormalLog(File file) {
        try {
            this.file = Files.newByteChannel(file.create()
                    .asJavaPath(), StandardOpenOption.CREATE, StandardOpenOption.READ, StandardOpenOption.WRITE);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() throws Exception {
        file.close();
    }

    /**
     * Retrieve ID of the first item.
     * 
     * @return
     */
    long firstID() {
        // normalize
        repair();

        try {
            // no data
            if (file.size() == 0) {
                return -1;
            }

            // read value at head
            file.position(0);
            return Long.parseLong(readColumn());
        } catch (NumberFormatException e) {
            return -1;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Retrieve ID of the last item.
     * 
     * @return
     */
    long lastID() {
        // normalize
        repair();

        try {
            // no data
            if (file.size() == 0) {
                return -1;
            }

            // read value at tail
            moveToLineHead();
            return Long.parseLong(readColumn());
        } catch (NumberFormatException e) {
            return -1;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Check to see if this file is corrupt.
     * 
     * @return A result.
     */
    boolean isCorrupted() {
        try {
            long length = file.size();

            if (length == 0) {
                return false;
            } else if (length <= 10) {
                return true;
            }

            // move to tail
            file.position(length);

            // tail must be '\r\n'
            byte[] bytes = readBack(2);
            return bytes[0] != 0x00D || bytes[1] != 0x00A;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * If this file is corrupted, repair it by removing only the lines that cause it.
     */
    void repair() {
        if (isCorrupted()) {
            try {
                moveToLineHead();
                file.truncate(file.position());
                file.position(file.size() - 2);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * Write the specified text at tail.
     * 
     * @throws IOException
     */
    void append(String text) throws IOException {
        file.position(file.size());
        file.write(ByteBuffer.wrap(text.getBytes(StandardCharsets.ISO_8859_1)));
    }

    /**
     * Move pointer to the head of the current line.
     * 
     * @param size
     */
    private void moveToLineHead() throws IOException {
        byte[] bytes = readBack(128);
        for (int i = bytes.length - 2; 0 <= i; i--) {
            if (bytes[i] == 0x00D && bytes[i + 1] == 0x00A) {
                file.position(file.position() + i + 2);
                return;
            }
        }
    }

    /**
     * Read data and step position back.
     * 
     * @param size
     * @return
     */
    private byte[] readBack(int size) throws IOException {
        long end = file.position();
        long start = Math.max(0, end - size);
        ByteBuffer bytes = ByteBuffer.allocate((int) (end - start));
        file.position(start);
        file.read(bytes);
        file.position(start);
        return bytes.array();
    }

    /**
     * Read then next column data.
     * 
     * @return
     * @throws IOException
     */
    private String readColumn() throws IOException {
        // The longest date and time item can be read if it has 26 bytes.
        ByteBuffer buffer = ByteBuffer.allocate(32);
        long pos = file.position();
        int read = file.read(buffer);
        byte[] bytes = buffer.array();
        for (int i = 0; i < read; i++) {
            if (bytes[i] == 0x20) {
                file.position(pos + i);
                return new String(bytes, 0, i);
            }
        }
        return "";
    }

    /**
     * Utility to read the last ID simply.
     * 
     * @param file
     * @return
     */
    static long readLastID(File file) {
        try (NormalLog reader = new NormalLog(file)) {
            return reader.lastID();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }
}