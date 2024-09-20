/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.gmo;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.execution.HttpLogHouse;
import cointoss.util.APILimiter;
import cointoss.util.Chrono;
import hypatia.Num;
import kiss.I;
import kiss.Signal;
import kiss.XML;

class OfficialLogHouse extends HttpLogHouse {

    /** The API limit. */
    private static final APILimiter LIMITER = APILimiter.with.limit(2).refresh(250, TimeUnit.MILLISECONDS);

    OfficialLogHouse(MarketService service) {
        super(service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String locate(ZonedDateTime date) {
        String formatted = Chrono.DateCompact.format(date);

        return String.format("https://api.coin.z.com/data/trades/" + service.marketName + "/" + formatted.substring(0, 4) + "/" + formatted
                .substring(4, 6) + "/" + formatted + "_" + service.marketName + ".csv.gz");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<ZonedDateTime> collect() {
        String uri = "https://api.coin.z.com/data/trades/" + service.marketName + "/";
        Function<Signal<XML>, Signal<String>> collect = s -> s.flatIterable(x -> x.find("ul li:first-child a, ul li:last-child a"))
                .map(XML::text);

        return I.http(uri, XML.class).plug(collect).concatMap(year -> {
            return I.http(uri + year + "/", XML.class).plug(collect).concatMap(month -> {
                return I.http(uri + year + "/" + month + "/", XML.class).plug(collect).map(name -> {
                    return Chrono.utc(name.substring(0, name.indexOf("_")));
                });
            });
        }).buffer().flatMap(b -> Chrono.range(b.getFirst(), b.getLast()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> convert(ZonedDateTime date) {
        ZonedDateTime following = date.plusDays(1);

        long[] prev = new long[3];
        DateTimeFormatter timeFormatOnLog = DateTimeFormatter.ofPattern("yyyy-MM-dd' 'HH:mm:ss.SSS");

        LIMITER.acquire();

        return downloadCSV(date).concat(downloadCSV(following)).map(values -> {
            Direction side = Direction.parse(values[1]);
            Num size = Num.of(values[2]);
            Num price = Num.of(values[3]);
            ZonedDateTime time = LocalDateTime.parse(values[4], timeFormatOnLog).atZone(Chrono.UTC);

            return GMOService.Support.createExecution(side, size, price, time, prev);
        }).skipUntil(e -> e.date.isAfter(date)).takeWhile(e -> e.date.isBefore(following));
    }
}