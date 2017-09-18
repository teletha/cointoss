/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.chart;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import cointoss.Execution;
import cointoss.Trend;
import cointoss.market.bitflyer.BitFlyer;
import filer.Filer;

/**
 * @version 2017/09/09 10:03:54
 */
public class ChartTrendTest {

    @Test
    public void downTrend() throws Exception {
        Chart chart = bitflyerFX("2017-09-02T11:00:00", "2017-09-05T12:59:59", Duration.ofHours(2));
        assert chart.trend() == Trend.Down;

        chart = bitflyerFX("2017-08-20T07:00:00", "2017-08-22T08:59:59", Duration.ofHours(2));
        assert chart.trend() == Trend.Down;
    }

    @Test
    public void upTrend() throws Exception {
        Chart chart = bitflyerFX("2017-09-05T13:00:00", "2017-09-07T00:59:59", Duration.ofHours(2));
        assert chart.trend() == Trend.Up;

        chart = bitflyerFX("2017-08-02T21:00:00", "2017-08-05T04:59:59", Duration.ofHours(2));
        assert chart.trend() == Trend.Up;
    }

    @Test
    public void range() throws Exception {
        Chart chart = bitflyerFX("2017-08-25T07:00:00", "2017-08-28T06:59:59", Duration.ofHours(2));
        assert chart.trend() == Trend.Range;

        chart = bitflyerFX("2017-08-08T11:00:00", "2017-08-11T22:59:59", Duration.ofHours(2));
        assert chart.trend() == Trend.Range;
    }

    /**
     * @param start Tokyo time
     * @param end Tokyo time
     * @param duration
     * @return
     */
    private Chart bitflyerFX(String start, String end, Duration duration) {
        return chart(BitFlyer.FX_BTC_JPY, start, end, duration);
    }

    /**
     * @param type
     * @param start
     * @param end
     * @param duration
     * @return
     */
    private Chart chart(BitFlyer type, String start, String end, Duration duration) {
        // convert Asia/Tokyo to UTC
        ZonedDateTime startTime = LocalDateTime.parse(start).minusHours(9).atZone(Execution.UTC);
        ZonedDateTime endTime = LocalDateTime.parse(end).minusHours(9).atZone(Execution.UTC);

        // search tick log
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMddhhmmss");
        Path file = Filer.locate("src/test/resources/trend").resolve(format.format(startTime) + "～" + format.format(endTime) + ".txt");

        Chart chart = new Chart(duration);

        if (Files.notExists(file)) {
            // crate new tick log from execution log
            type.log() //
                    .range(startTime, endTime)
                    .skipWhile(e -> e.isBefore(startTime))
                    .takeWhile(e -> e.isBefore(endTime))
                    .to(chart::tick);

            chart.writeTo(file);
        } else {
            chart.readFrom(file);
        }
        return chart;
    }
}
