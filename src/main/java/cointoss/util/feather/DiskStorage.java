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

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.SPARSE;
import static java.nio.file.StandardOpenOption.WRITE;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.locks.StampedLock;

import kiss.I;
import psychopath.File;

class DiskStorage<T> {

    /** The header size. */
    private static final int HEADER_SIZE = 0 //
            + 8 // start time
            + 8 // end time
            + 112; // reserved space, use in future

    /** The item prefix. */
    private static final byte ITEM_UNDEFINED = 0;

    /** The item prefix. */
    private static final byte ITEM_DEFINED = 1;

    /** The actual channel. */
    private final FileChannel channel;

    /** The process lock. */
    private final FileLock lockForProcess;

    /** The read-write lock. */
    private final StampedLock lock = new StampedLock();

    /** The data definition. */
    private final DataType<T> codec;

    /** The total byte size for each items. */
    private final int itemWidth;

    /** The time that one element has. */
    private final long duration;

    /** The flag. */
    private boolean headerModified;

    /** The start time of all records. (included) */
    private long startTime;

    /** The end time of all records. (excluded) */
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
            this.lockForProcess = databaseFile.extension("lock").newFileChannel(CREATE, READ, WRITE).tryLock();
            this.codec = codec;
            this.itemWidth = codec.size() + 1;
            this.duration = duration;

            if (HEADER_SIZE < channel.size()) {
                readHeader();
            } else {
                startTime = Long.MAX_VALUE;
                endTime = 0;
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
        if (lockForProcess == null) {
            // The current process does not have write permission because it is being used by
            // another process.
            return;
        }

        if (headerModified) {
            try {
                channel.force(true);
                // encode
                ByteBuffer buffer = ByteBuffer.allocate(HEADER_SIZE);
                buffer.putLong(startTime);
                buffer.putLong(endTime);

                // write
                buffer.flip();
                channel.write(buffer, 0);

                // update stataus
                headerModified = false;
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * Update time information in header.
     * 
     * @param startTime A new starting time.
     * @param endTime A new ending time.
     */
    private void updateTime(long startTime, long endTime) {
        if (startTime < this.startTime) {
            this.startTime = startTime;
            headerModified = true;
        }

        if (this.endTime < endTime) {
            this.endTime = endTime;
            headerModified = true;
        }
    }

    /**
     * Read data from disk cache.
     * 
     * @param truncatedTime
     */
    int read(long truncatedTime, T[] items) {
        long stamp = lock.readLock();

        try {
            ByteBuffer buffer = ByteBuffer.allocate(itemWidth * items.length);
            int size = channel.read(buffer, HEADER_SIZE + truncatedTime / duration * itemWidth);
            if (size == -1) {
                return 0;
            }
            buffer.flip();

            int readableItemSize = size / itemWidth;
            int skip = 0;
            for (int i = 0; i < readableItemSize; i++) {
                if (buffer.get() == ITEM_UNDEFINED) {
                    buffer.position(buffer.position() + itemWidth - 1);
                    skip++;
                } else {
                    items[i] = codec.read(buffer);
                }
            }
            return readableItemSize - skip;
        } catch (IOException e) {
            throw I.quiet(e);
        } finally {
            lock.unlockRead(stamp);
        }
    }

    /**
     * Write data to disk cache.
     * 
     * @param truncatedTime
     * @param segment
     */
    void write(long truncatedTime, T... items) {
        if (lockForProcess == null) {
            // The current process does not have write permission because it is being used by
            // another process.
            return;
        }

        long stamp = lock.writeLock();

        try {
            long startPosition = HEADER_SIZE + truncatedTime / duration * itemWidth;
            ByteBuffer buffer = ByteBuffer.allocate(itemWidth * items.length);

            for (int i = 0; i < items.length; i++) {
                T item = items[i];
                if (item == null) {
                    if (buffer.position() != 0) {
                        buffer.flip();
                        channel.write(buffer, startPosition);
                        buffer.clear();
                    }
                    startPosition = HEADER_SIZE + (truncatedTime / duration + i + 1) * itemWidth;
                } else {
                    buffer.put(ITEM_DEFINED);
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
            updateTime(truncatedTime, truncatedTime + items.length * duration);
            // writeHeader();

            lock.unlockWrite(stamp);
        }
    }

    /**
     * Get the starting time. (included)
     * 
     * @return
     */
    final long startTime() {
        return startTime;
    }

    /**
     * Get the ending time. (excluded)
     * 
     * @return
     */
    final long endTime() {
        return endTime;
    }
}