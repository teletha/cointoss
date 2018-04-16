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

/**
 * @version 2018/04/14 15:04:55
 */
public class CompressedTimeSeriesTest {

    public static void main(String[] args) {
        CompressedTimeSeries series = new CompressedTimeSeries(new byte[1024 * 1024 * 1024]);

        long time = System.currentTimeMillis();
        for (int i = 0; i < 2000; i++) {
            series.append(time + i, i);
        }
        System.out.println(series.size());
    }
}
