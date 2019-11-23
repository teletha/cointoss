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

import cointoss.Market;
import cointoss.MarketService;
import cointoss.TradingLog;
import cointoss.analyze.Analyzer;
import cointoss.analyze.Statistics;
import cointoss.execution.ExecutionLog;
import cointoss.market.MarketServiceProvider;
import cointoss.trading.LazyBear;
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
import viewtify.ui.UILabel;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableColumn.UITableCell;
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

    /** The trading statistics. */
    private UITableColumn<TradingLog, String> size;

    /** The trading statistics. */
    private UITableColumn<TradingLog, Statistics> realizedProfit;

    /** The trading statistics. */
    private UITableColumn<TradingLog, Statistics> unrealizedProfit;

    /** The trading statistics. */
    private UITableColumn<TradingLog, Statistics> realizedLoss;

    /** The trading statistics. */
    private UITableColumn<TradingLog, Statistics> unrealizedLoss;

    /** UI */
    private UITableColumn<TradingLog, Num> profitAndLoss;

    /** UI */
    private UITableColumn<TradingLog, Num> winRatio;

    /** UI */
    private UITableColumn<TradingLog, Num> riskRewardRatio;

    /**
     * UI definition.
     */
    class view extends UI implements SettingStyles {

        {
            $(vbox, () -> {
                $(hbox, () -> {
                    $(chart);
                    $(vbox, () -> {
                        $(marketSelection);
                        $(hbox, FormRow, () -> {
                            label(en("Start"), style.formLabel);
                            $(startDate, style.FormInput);
                            label(en("End"), style.formLabel);
                            $(endDate, style.FormInput);
                        });
                        $(runner);
                    });
                });

                $(hbox, () -> {
                    $(results, style.testResult, () -> {
                        $(name);
                        $(size);
                        $(realizedProfit);
                        $(unrealizedProfit);
                        $(realizedLoss);
                        $(unrealizedLoss);

                        $(profitAndLoss);
                        $(winRatio);
                        $(riskRewardRatio);
                    });
                });
            });
        }
    }

    /**
     * Style definition
     */
    interface style extends StyleDSL {
        Style testResult = () -> {
            display.width(800, px).maxHeight(150, px).minHeight(150, px);
        };

        Style formLabel = () -> {
            display.minWidth(40, px);
            padding.top(3, px);
        };

        Style FormInput = () -> {
            display.minWidth(110, px);
            margin.right(20, px);
        };

        Style max = () -> {
            font.color(255, 230, 230);
        };

        Style mean = () -> {
            margin.right(5, px);
            font.color(205, 230, 250);
        };
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
                    .traders(LazyBear::new)
                    .initialBaseCurrency(3000000)
                    .run(this);
        });

        configureTradingLogView();
    }

    /**
     * Setting for trading log.
     */
    private void configureTradingLogView() {
        name.header(en("Name")).model(log -> log.trader.name());
        size.header(en("Size(Max)")).model(log -> log.holdCurrentSize + "/" + log.holdMaxSize);
        realizedProfit.header(en("Realized Profit")).model(log -> log.profitRange).render(this::render);
        unrealizedProfit.header(en("Unrealized Profit")).model(log -> log.unrealizedProfitRange).render(this::render);
        realizedLoss.header(en("Realized Loss")).model(log -> log.lossRange).render(this::render);
        unrealizedLoss.header(en("Unrealized Loss")).model(log -> log.unrealizedLossRange).render(this::render);

        profitAndLoss.header(en("Profit")).model(log -> log.profitAndLoss.total());
        winRatio.header(en("Win Rate")).model(log -> log.winningRate());
    }

    private void render(UITableCell cell, Statistics statistics) {
        int base = marketSelection.value().setting.baseCurrencyScaleSize;

        UILabel mean = make(UILabel.class).tooltip(en("mean")).text(statistics.mean().scale(base)).style(style.mean);
        UILabel max = make(UILabel.class).tooltip(en("max")).text(statistics.max().scale(base)).style(style.max);

        cell.text(mean, max);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Market market) {
        chart.showOrderSupport.set(false);
        chart.showPositionSupport.set(false);
        chart.showLatestPrice.set(false);
        chart.showRealtimeUpdate.set(false);

        chart.market.set(market);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void analyze(Market market, List<TradingLog> logs, boolean detail) {
        for (TradingLog log : logs) {
            System.out.println(log);
            results.values.add(log);
        }

        Viewtify.inUI(() -> {
            logSelection.values(logs);

            market.dispose();
        });
    }
}
