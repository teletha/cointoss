/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.feather;

import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.SPARSE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

import kiss.I;
import psychopath.File;

class DiskStorage<T> {

    /** The header size. */
    private static final int HEADER_SIZE = 8 + 8;

    /** The actual channel. */
    private final FileChannel channel;

    /** The process lock. */
    private final FileLock lockForProcess;

    /** The read-write lock. */
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    /** The data definition. */
    private final DataType<T> codec;

    /** The time that one element has. */
    private final long duration;

    /** The start time of all records. */
    private long startTime;

    /** The end time of all records. */
    private long endTime;

    /**
     * Create disk storage.
     * 
     * @param databaseFile The actual storage file.
     * @param codec The data definition.
     * @param duration The time that one element has.
     */
    DiskStorage(File databaseFile, DataType<T> codec, long duration) {
        try {
            this.channel = databaseFile.isPresent() ? databaseFile.newFileChannel(READ, WRITE)
                    : databaseFile.newFileChannel(CREATE_NEW, SPARSE, READ, WRITE);
            this.lockForProcess = channel.tryLock();
            this.codec = codec;
            this.duration = duration;

            if (HEADER_SIZE < channel.size()) {
                readHeader();
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Parse header.
     */
    private void readHeader() {
        try {
            // read
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
            channel.read(buffer, 0);
            buffer.flip();

            // decode
            startTime = buffer.getLong();
            endTime = buffer.getLong();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Update header.
     * 
     * @throws IOException
     */
    private void writeHeader() {
        try {
            // encode
            ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
            buffer.putLong(startTime);
            buffer.putLong(endTime);

            // write
            buffer.flip();
            channel.write(buffer, 0);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Read data from disk cache.
     * 
     * @param truncatedTime
     */
    int read(long truncatedTime, T[] items) {
        ReadLock readLock = lock.readLock();

        try {
            readLock.lock();

            ByteBuffer buffer = ByteBuffer.allocate(codec.size() * items.length);
            int size = channel.read(buffer, HEADER_SIZE + truncatedTime / duration * codec.size());

            if (size != -1) {
                buffer.flip();

                int readableItemSize = size / codec.size();
                for (int i = 0; i < readableItemSize; i++) {
                    items[i] = codec.read(buffer);
                }
                return readableItemSize;
            }
            return 0;
        } catch (IOException e) {
            throw I.quiet(e);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Write data to disk cache.
     * 
     * @param truncatedTime
     * @param segment
     */
    void write(long truncatedTime, T[] items) {
        if (lockForProcess == null) {
            return;
        }

        WriteLock writeLock = lock.writeLock();
        writeLock.lock();

        try {
            long startPosition = HEADER_SIZE + truncatedTime / duration * codec.size();
            ByteBuffer buffer = ByteBuffer.allocate(codec.size() * items.length);

            for (int i = 0; i < items.length; i++) {
                T item = items[i];
                if (item == null) {
                    if (buffer.position() != 0) {
                        buffer.flip();
                        channel.write(buffer, startPosition);
                        buffer.clear();
                    }
                    startPosition = HEADER_SIZE + (truncatedTime / duration + i + 1) * codec.size();
                } else {
                    codec.write(item, buffer);
                }
            }

            if (buffer.position() != 0) {
                buffer.flip();
                channel.write(buffer, startPosition);
            }
        } catch (IOException e) {
            throw I.quiet(e);
        } finally {
            writeLock.unlock();
        }
    }
}