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
import java.nio.file.Path;

import kiss.I;
import psychopath.File;

class NormalLogFile implements AutoCloseable {

    private final RandomAccessFile file;

    /**
     * 
     */
    NormalLogFile(File file) {
        this(file.asJavaPath());
    }

    /**
     * 
     */
    NormalLogFile(Path file) {
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
     * Check to see if this file is corrupt.
     * 
     * @return A result.
     */
    boolean isCorrupted() {
        try {
            long length = file.length();

            if (length <= 2) {
                return true;
            }

            file.seek(length);
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
                backToLineStart();
                file.setLength(file.getFilePointer());
                file.seek(file.length() - 2);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    long readLastId() {
        repair();
        moveToEnd();
        String line = readLastLine();
    }

    private String readLastLine() {
        try {
            repair();

            byte[] bytes = readBack(128);
            for (int i = bytes.length - 2; 0 <= i; i--) {
                if (bytes[i] == 0x00D && bytes[i + 1] == 0x00A) {
                    file.seek(file.getFilePointer() + i + 2);
                    return;
                }
            }

        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Read data and step position back.
     * 
     * @param size
     * @return
     */
    private void backToLineStart() throws IOException {
        byte[] bytes = readBack(128);
        for (int i = bytes.length - 2; 0 <= i; i--) {
            if (bytes[i] == 0x00D && bytes[i + 1] == 0x00A) {
                file.seek(file.getFilePointer() + i + 2);
                return;
            }
        }
    }

    /**
     * Move the pointer to end of file.
     */
    private void moveToEnd() {
        try {
            file.seek(file.length());
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Read data and step position back.
     * 
     * @param size
     * @return
     */
    private byte[] readBack(int size) throws IOException {
        byte[] bytes = new byte[size];
        long pos = Math.max(0, file.getFilePointer() - size);
        file.seek(pos);
        file.read(bytes);
        file.seek(pos);
        return bytes;
    }
}
