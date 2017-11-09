/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import org.junit.Test;

/**
 * @version 2017/09/10 9:27:14
 */
public class BufferTest {

    @Test
    public void appendSingle() throws Exception {
        Buffer<Integer> buffer = new Buffer<>(2, 2, "");
        buffer.append(1);
    }

    @Test
    public void append() throws Exception {
        Buffer<Integer> buffer = new Buffer<>(2, 2, "");
        buffer.append(0, 1, 2, 3);
        assert buffer.end() == 3;
        assert buffer.startIndex() == 0;
        assert buffer.endIndex() == 3;
        assert buffer.firstIndex() == 0;
        assert buffer.lastIndex() == 3;

        buffer.append(4, 5);
        assert buffer.end() == 5;
        assert buffer.startIndex() == 0;
        assert buffer.endIndex() == 5;
        assert buffer.firstIndex() == 2;
        assert buffer.lastIndex() == 5;
    }
}
