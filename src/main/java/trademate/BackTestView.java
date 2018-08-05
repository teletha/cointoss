/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import java.time.LocalDate;
import java.time.Period;

import cointoss.Market;
import cointoss.MarketLog;
import cointoss.MarketService;
import cointoss.market.MarketProvider;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.TickSpan;
import cointoss.util.Chrono;
import kiss.I;
import kiss.Signal;
import trademate.chart.ChartView;
import viewtify.UI;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;
import viewtify.ui.helper.User;

/**
 * @version 2018/06/26 21:28:54
 */
public class BackTestView extends View {

    @UI
    private UIComboBox<MarketService> market;

    @UI
    private UIDatePicker startDate;

    @UI
    private UIDatePicker endDate;

    @UI
    private UIButton start;

    @UI
    private ChartView chart;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Signal<LocalDate> cacheLast = market.observe().map(m -> m.log.lastCacheDate().toLocalDate());

        Viewtify.calculate(market.model()).map(MarketService::log).map(MarketLog::lastCacheDate);

        market.values(0, MarketProvider.availableMarkets());
        startDate.initial(Chrono.utcNow().minusDays(10)).uneditable().require(() -> {
            MarketLog log = market.value().log;

            assert startDate.isBeforeOrSame(log.lastCacheDate()) : I.i18n(Message::logIsNotFound);
            assert startDate.isAfterOrSame(log.firstCacheDate()) : I.i18n(Message::logIsNotFound);
        }).restrictS(v -> v.combineLatest(cacheLast).map(e -> e.ⅰ.isBefore(e.ⅱ))).observe((o, n) -> {
            endDate.value(v -> v.plus(Period.between(o, n)));
        });
        endDate.initial(Chrono.utcNow()).uneditable().require(() -> {
            assert startDate.isBeforeOrSame(endDate) : I.i18n(Message::endDateMustBeAfterStartDate);
        });
        start.disableWhen(startDate.isInvalid());

        Market market = new Market(BitFlyer.BTC_JPY).readLog(log -> log.at(2018, 1, 17));

        Viewtify.Terminator.add(market);
        chart.market.set(market);
        chart.ticker.set(market.tickers.tickerBy(TickSpan.Minute1));

        start.when(User.MouseClick).to(e -> {
            Market m = new Market(BitFlyer.FX_BTC_JPY);
            chart.market.set(m);
            chart.market.to(v -> v.readLog(log -> log.range(Chrono.utc(startDate.value()), Chrono.utc(endDate.value()))));
        });
    }
}
