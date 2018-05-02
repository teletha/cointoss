/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

/**
 * @version 2018/05/02 23:20:10
 */

public class FcpCompressorTest {

    @Test
    public void testRoundtripWithTwoValues() {
        double[] doubles = new double[] {1.0, 0.0};
        FpcCompressor fpc = new FpcCompressor();

        ByteBuffer buffer = ByteBuffer.allocate(64);
        fpc.compress(buffer, doubles);

        buffer.flip();
        System.out.println(Arrays.toString(buffer.array()));

        FpcCompressor decompressor = new FpcCompressor();

        double[] dest = new double[2];
        decompressor.decompress(buffer, dest);

        assert Arrays.equals(doubles, dest);
    }

    @Test
    public void testRoundtripWithThreeValues() {
        double[] doubles = new double[] {1.0, 0.1, 0.5, 0.100105, 0.1, 0.1, 0.501, 111.4990191, 0.14142};
        FpcCompressor fpc = new FpcCompressor();

        ByteBuffer buffer = ByteBuffer.allocate(64);
        fpc.compress(buffer, doubles);

        buffer.flip();
        System.out.println(Arrays.toString(buffer.array()));

        FpcCompressor decompressor = new FpcCompressor();

        double[] dest = new double[9];
        decompressor.decompress(buffer, dest);

        assert Arrays.equals(dest, doubles);
    }

}