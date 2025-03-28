/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util.feather;

/**
 * The primitive long specialized LRU queue.
 */
class LRU implements EvictionPolicy {

    private int index;

    private long[] values;

    private int[] nexts;

    private int[] prevs;

    /** The index of eldest value in values. */
    private int eldest;

    /** The index of latest value in values. */
    private int latest;

    LRU(int size) {
        index = size - 1;
        values = new long[size];
        nexts = new int[size];
        prevs = new int[size];
        eldest = -1;
        latest = -1;
    }

    long latest() {
        return values[latest];
    }

    long eldest() {
        return values[eldest];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long access(long item) {
        // first assign
        if (eldest == -1) {
            eldest = latest = index;
            prevs[index] = -1;
            nexts[index] = -1;
            values[index--] = item;
            return -1;
        }

        // imcompleted assign
        if (index != -1) {
            values[index] = item;
            updateLatest(index--);
            return -1;
        }

        // ignore latest
        if (values[latest] == item) {
            return -1;
        }

        // ignore existing

        // completed assign
        for (int i = 0; i < values.length; i++) {
            if (values[i] == item) {
                delete(i);
                updateLatest(i);
                return -1;
            }
        }

        // delete eldest
        int e = eldest;
        long v = values[e];
        delete(e);

        // replace by latest
        values[e] = item;
        updateLatest(e);

        return v;
    }

    /**
     * @param newLatest The index of new latest value in values.
     */
    private void updateLatest(int newLatest) {
        nexts[latest] = newLatest;

        prevs[newLatest] = latest;
        nexts[newLatest] = -1;
        latest = newLatest;
    }

    private void delete(int target) {
        int prev = prevs[target];
        int next = nexts[target];

        if (prev != -1) {
            nexts[prev] = next;
        }

        if (next != -1) {
            prevs[next] = prev;
        }

        if (eldest == target) {
            eldest = next;
        }
    }
}