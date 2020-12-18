/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.execution;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import kiss.I;
import psychopath.File;

class NormalLog implements AutoCloseable {

    private final RandomAccessFile file;

    /**
     * 
     */
    NormalLog(File file) {
        this(file.asJavaPath());
    }

    /**
     * 
     */
    NormalLog(Path file) {
        try {
            this.file = new RandomAccessFile(file.toFile(), "rw");
        } catch (FileNotFoundException e) {
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
            if (file.length() == 0) {
                return -1;
            }

            // read value at head
            file.seek(0);
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
            if (file.length() == 0) {
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
            long length = file.length();

            if (length == 0) {
                return false;
            } else if (length <= 10) {
                return true;
            }

            // move to tail
            file.seek(length);

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
                file.setLength(file.getFilePointer());
                file.seek(file.length() - 2);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * Write the specified text at tail.
     * 
     * @param text
     * @throws IOException
     */
    void append(String text) throws IOException {
        file.seek(file.length());
        file.write(text.getBytes(StandardCharsets.ISO_8859_1));
    }

    /**
     * Move pointer to the head of the current line.
     * 
     * @param size
     * @return
     */
    private void moveToLineHead() throws IOException {
        byte[] bytes = readBack(128);
        for (int i = bytes.length - 2; 0 <= i; i--) {
            if (bytes[i] == 0x00D && bytes[i + 1] == 0x00A) {
                file.seek(file.getFilePointer() + i + 2);
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
        long end = file.getFilePointer();
        long start = Math.max(0, end - size);
        byte[] bytes = new byte[(int) (end - start)];
        file.seek(start);
        file.read(bytes);
        file.seek(start);
        return bytes;
    }

    /**
     * Read then next column data.
     * 
     * @return
     * @throws IOException
     */
    private String readColumn() throws IOException {
        // The longest date and time item can be read if it has 26 bytes.
        byte[] bytes = new byte[32];
        long pos = file.getFilePointer();
        int read = file.read(bytes);
        for (int i = 0; i < read; i++) {
            if (bytes[i] == 0x20) {
                file.seek(pos + i);
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
