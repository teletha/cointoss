/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import cointoss.Execution;
import cointoss.MarketBuilder;
import filer.Filer;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/08/16 8:13:06
 */
public class BitFlyerBTCFXBuilder implements MarketBuilder {

    /** date format for log */
    private static final DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * {@inheritDoc}
     */
    @Override
    public Signal<Execution> initialize() {
        LocalDate start = LocalDate.of(2017, 6, 24);
        LocalDate end = LocalDate.of(2017, 6, 29);

        List<LocalDate> period = new ArrayList();

        while (start.isBefore(end)) {
            period.add(start);
            start = start.plusDays(1);
        }

        return I.signal(period)
                .map(i -> Filer.locate(".log/bitflyer/" + BitFlyerType.FX_BTC_JPY.name() + "/execution" + format.format(i) + ".log"))
                .flatMap(Filer::read)
                .map(Execution::new);
    }
}
