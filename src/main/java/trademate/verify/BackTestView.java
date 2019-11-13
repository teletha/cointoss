/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.verify;

import static transcript.Transcript.en;

import java.time.Period;
import java.util.List;
import java.util.concurrent.TimeUnit;

import cointoss.Direction;
import cointoss.Market;
import cointoss.MarketService;
import cointoss.analyze.Analyzer;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.ticker.Span;
import cointoss.trade.Scenario;
import cointoss.trade.Trader;
import cointoss.trade.TradingLog;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.verify.BackTest;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartView;
import trademate.setting.SettingStyles;
import transcript.Transcript;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;
import viewtify.ui.View;
import viewtify.ui.helper.User;

public class BackTestView extends View implements Analyzer {

    private static final Transcript LogIsNotFound = Transcript.en("No logs were found for the specified date.");

    private static final Transcript EndDateMustBeAfterStartDate = Transcript.en("The end date must be after the start date.");

    private UIComboBox<MarketService> marketSelection;

    private UIDatePicker startDate;

    private UIDatePicker endDate;

    private UIButton runner;

    private ChartView chart;

    private UIComboBox<TradingLog> logSelection;

    /** UI */
    private UITableView results;

    /** UI */
    private UITableColumn<TradingLog, String> name;

    /** UI */
    private UITableColumn<TradingLog, Num> profitAndLoss;

    /** UI */
    private UITableColumn<TradingLog, Num> winRatio;

    /** UI */
    private UITableColumn<TradingLog, Num> riskRewardRatio;

    /**
     * UI definition.
     */
    private class UIDefinition extends UI implements SettingStyles, Styles {

        {
            $(vbox, () -> {
                $(hbox, () -> {
                    $(chart);
                    $(vbox, () -> {
                        $(marketSelection);
                        $(hbox, FormRow, () -> {
                            label(en("Start Date"), FormLabel);
                            $(startDate, FormInput);
                        });
                        $(hbox, FormRow, () -> {
                            label(en("End Date"), FormLabel);
                            $(endDate, FormInput);
                        });
                        $(runner);
                    });
                });

                $(hbox, () -> {
                    $(results, Results, () -> {
                        $(name);
                        $(profitAndLoss);
                        $(winRatio);
                        $(riskRewardRatio);
                    });
                });
            });
        }
    }

    /**
     * 
     */
    private interface Styles extends StyleDSL {
        Style Results = () -> {
            display.width(300, px).maxHeight(150, px).minHeight(150, px);
        };

        Style Chart = () -> {
            display.width(300, px).height(150, px);
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UIDefinition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        marketSelection.values(0, MarketServiceProvider.availableMarketServices());
        startDate.initial(Chrono.utcNow().minusDays(10)).uneditable().requireWhen(marketSelection).require(() -> {
            ExecutionLog log = marketSelection.value().log;

            assert startDate.isBeforeOrSame(log.lastCacheDate()) : LogIsNotFound;
            assert startDate.isAfterOrSame(log.firstCacheDate()) : LogIsNotFound;
        }).observe((o, n) -> {
            endDate.value(v -> v.plus(Period.between(o, n)));
        });

        endDate.initial(Chrono.utcNow()).uneditable().require(() -> {
            assert startDate.isBeforeOrSame(endDate) : EndDateMustBeAfterStartDate;
        });

        runner.text(en("Run")).disableWhen(startDate.isInvalid()).when(User.MouseClick).on(Viewtify.WorkerThread).to(e -> {
            BackTest.with.service(marketSelection.value())
                    .start(startDate.zoned())
                    .end(endDate.zoned())
                    .traders(Sample::new)
                    .initialBaseCurrency(3000000)
                    .run(this);
        }, e -> {
            e.printStackTrace();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void analyze(Market market, List<TradingLog> logs) {
        for (TradingLog log : logs) {
            System.out.println(log.showByText(true));
        }

        Viewtify.inUI(() -> {
            // update chart
            chart.market.set(market);
            chart.ticker.set(market.tickers.of(Span.Minute5));

            logSelection.values(logs);

            market.dispose();
        });
    }

    /**
     * 
     */
    private static class Sample extends Trader {

        private Sample(Market market) {
            super(market);

            when(market.tickers.of(Span.Hour4).add.skip(1).take(1), tick -> {
                return new Scenario() {

                    @Override
                    protected void entry() {
                        entry(Direction.random(), 3, s -> s.make(market.tickers.latest.v.price));
                    }

                    /**
                     * {@inheritDoc}
                     */
                    @Override
                    protected void exit() {
                        exitAt(entryPrice.plus(this, 5000));
                        exitAt(entryPrice.minus(this, 5000));
                        exitAfter(15, TimeUnit.MINUTES);
                    }
                };
            });
        }
    }
}
