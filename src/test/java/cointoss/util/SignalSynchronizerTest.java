/*
 * Copyright (C) 2025 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.util;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import cointoss.util.feather.Timelinable;
import kiss.I;
import kiss.Signal;

public class SignalSynchronizerTest {

    @Test
    void sync() {
        Signal<Timelinable> A = signal('a');
        Signal<Timelinable> B = signal('b');
        SignalSynchronizer synchronizer = new SignalSynchronizer();

        List<String> list = new ArrayList();
        A.plug(synchronizer.sync()).map(Timelinable::toString).toCollection(list);
        B.plug(synchronizer.sync()).map(Timelinable::toString).toCollection(list);

        Assertions.assertIterableEquals(list, List.of("a", "b"));
    }

    @Test
    void syncs() {
        Signal<Timelinable> A = signal('a', 'c');
        Signal<Timelinable> B = signal('b', 'd');
        A.combineLatest(B).to();
        SignalSynchronizer synchronizer = new SignalSynchronizer();

        List<String> list = new ArrayList();
        A.plug(synchronizer.sync()).map(Timelinable::toString).toCollection(list);
        B.plug(synchronizer.sync()).map(Timelinable::toString).toCollection(list);

        Assertions.assertIterableEquals(List.of("a", "b", "c", "d"), list);
    }

    @Test
    void syncs2() {
        Signal<Timelinable> A = signal('a', 'c', 'g', 'j');
        Signal<Timelinable> B = signal('b', 'd', 'e', 'f', 'h', 'i');
        A.combineLatest(B).to();
        SignalSynchronizer synchronizer = new SignalSynchronizer();

        List<String> list = new ArrayList();
        A.plug(synchronizer.sync()).map(Timelinable::toString).toCollection(list);
        B.plug(synchronizer.sync()).map(Timelinable::toString).toCollection(list);

        Assertions.assertIterableEquals(List.of("a", "b", "c", "d", "e", "f", "g", "h", "i", "j"), list);
    }

    private Signal<Timelinable> signal(Character... values) {
        return I.signal(values).map(x -> new TimedValue(x));
    }

    private static class TimedValue implements Timelinable {

        private final char value;

        /**
         * @param value
         */
        private TimedValue(char value) {
            this.value = value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long mills() {
            return value;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }
}