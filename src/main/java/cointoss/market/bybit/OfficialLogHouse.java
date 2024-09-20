/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bybit;

import java.time.ZonedDateTime;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.execution.HttpLogHouse;
import cointoss.util.Chrono;
import hypatia.Num;
import kiss.I;
import kiss.Signal;
import kiss.XML;

class OfficialLogHouse extends HttpLogHouse {

    OfficialLogHouse(MarketService service) {
        super(service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String locate(ZonedDateTime date) {
        return "https://public.bybit.com/trading/" + service.marketName + "/" + service.marketName + Chrono.Date.format(date) + ".csv.gz";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<ZonedDateTime> collect() {
        String uri = "https://public.bybit.com/trading/" + service.marketName + "/";

        return I.http(uri, XML.class).flatIterable(o -> o.find("li a")).map(XML::text).map(name -> {
            return Chrono.utc(name.substring(service.marketName.length(), name.indexOf(".")));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> convert(ZonedDateTime date) {
        Signal<String[]> signal = downloadCSV(date);

        if (date.isBefore(Chrono.utc(2021, 12, 7))) {
            signal = signal.reverse();
        }

        long[] context = new long[3];
        return signal.map(values -> {
            ZonedDateTime time = parseTime(values[0]);
            Direction side = Direction.parse(values[2]);
            Num size = Num.of(values[9]);
            Num price = Num.of(values[4]);

            return BybitService.Support.createExecution(side, size, price, time, context);
        }).waitForTerminate();
    }

    /**
     * Parse date-time expression.
     * 
     * @param dateTime
     * @return
     */
    private ZonedDateTime parseTime(String dateTime) {
        dateTime = dateTime.replace(".", "");

        long modifier;
        switch (16 - dateTime.length()) {
        case 1:
            modifier = 10;
            break;
        case 2:
            modifier = 100;
            break;
        case 3:
            modifier = 1000;
            break;
        case 4:
            modifier = 10000;
            break;
        case 5:
            modifier = 100000;
            break;
        case 6:
            modifier = 1000000;
            break;
        default:
            modifier = 1;
            break;
        }
        return Chrono.utcByMicros(Long.parseLong(dateTime) * modifier);
    }
}