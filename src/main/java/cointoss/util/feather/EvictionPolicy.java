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

public interface EvictionPolicy {

    /**
     * Calculates the most unnecessary time that should be evicted based on the history of changes
     * made by accessing the specified time.
     * 
     * @param time The current accessing time.
     * @return The most unnecessary time which should be evicted, or -1 if there is no evictable
     *         time.
     */
    long access(long time);

    /**
     * Provide the {@link EvictionPolicy} which never evict any value.
     * 
     * @return
     */
    static EvictionPolicy never() {
        return time -> -1;
    }

    /**
     * Provides an {@link EvictionPolicy} based on the LRU algorithm with a specified size. However,
     * the most recent time will never be evicted as it is expected to be accessed frequently.
     * 
     * @param size A cache size.
     * @return A new {@link EvictionPolicy}.
     */
    static EvictionPolicy byLRU(int size) {
        return new EvictionPolicy() {

            /** The latest time. */
            private long latest;

            /** The actual policy. */
            private LRU policy = new LRU(size);

            @Override
            public long access(long time) {
                if (latest < time) {
                    latest = time;
                }

                long result = policy.access(time);
                return result == time ? -1 : result;
            }
        };
    }
}