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
import java.util.function.Supplier;

import cointoss.Market;
import cointoss.MarketService;
import cointoss.Trader;
import cointoss.analyze.Analyzer;
import cointoss.analyze.Statistics;
import cointoss.analyze.TradingStatistics;
import cointoss.execution.ExecutionLog;
import cointoss.execution.ExecutionLog.LogType;
import cointoss.market.MarketServiceProvider;
import cointoss.util.Chrono;
import cointoss.util.Num;
import cointoss.verify.BackTest;
import kiss.I;
import kiss.Signal;
import kiss.WiseSupplier;
import kiss.model.Model;
import stylist.Style;
import stylist.StyleDSL;
import trademate.chart.ChartView;
import trademate.chart.PlotScript;
import trademate.chart.builtin.TraderVisualizer;
import trademate.setting.SettingStyles;
import transcript.Transcript;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;
import viewtify.ui.UILabel;
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

    private UIComboBox<TradingStatistics> logSelection;

    /** UI */
    private UITableView results;

    /** UI */
    private UITableColumn<TradingStatistics, String> name;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, TradingStatistics> period;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, TradingStatistics> holdSize;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Statistics> holdTimeForProfit;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Statistics> holdTimeForLoss;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Statistics> realizedProfit;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Statistics> unrealizedProfit;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Statistics> realizedLoss;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Statistics> unrealizedLoss;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Statistics> profit;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, String> total;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Num> winRatio;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Num> profitFactor;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, Num> drawDown;

    /** The trading statistics. */
    private UITableColumn<TradingStatistics, TradingStatistics> scenarioCount;

    /**
     * UI definition.
     */
    class view extends UI implements SettingStyles {

        {

            $(vbox, () -> {
                $(hbox, style.fill, () -> {
                    $(chart);
                    $(vbox, style.config, () -> {
                        $(marketSelection);
                        $(hbox, FormRow, () -> {
                            label(en("Start"), style.formLabel);
                            $(startDate, style.formInput);
                            label(en("End"), style.formLabel);
                            $(endDate, style.formInput);
                        });
                        $(runner);
                    });
                });

                $(hbox, () -> {
                    $(results, style.testResult, () -> {
                        $(name);
                        $(period);
                        $(holdSize);
                        $(holdTimeForProfit);
                        $(holdTimeForLoss);
                        $(realizedProfit);
                        $(unrealizedProfit);
                        $(realizedLoss);
                        $(unrealizedLoss);
                        $(profit);
                        $(total);
                        $(winRatio);
                        $(profitFactor);
                        $(drawDown);
                        $(scenarioCount);
                    });
                });
            });
        }
    }

    /**
     * Style definition
     */
    interface style extends StyleDSL {
        Style fill = () -> {
            display.height.fill().width.fill();
        };

        Style config = () -> {
            display.minWidth(400, px).maxWidth(400, px);
        };

        Style testResult = () -> {
            display.maxHeight(250, px).minHeight(250, px);
        };

        Style formLabel = () -> {
            display.minWidth(40, px);
            padding.top(3, px);
        };

        Style formInput = () -> {
            display.minWidth(110, px);
            margin.right(20, px);
        };

        Style max = () -> {
            font.color(255, 230, 230);
        };

        Style mean = () -> {
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
                    .traders()
                    .initialBaseCurrency(3000000)
                    .type(LogType.Fast)
                    .run(this);
        });

        configureTradingLogView();
    }

    /**
     * Setting for trading log.
     */
    private void configureTradingLogView() {
        name.text(en("Name")).model(log -> log.name);
        period.text(en("Period")).model(log -> log).render(this::renderPeriod);
        holdSize.text(en("Hold Size")).model(log -> log).render(this::renderPositionSize);
        holdTimeForProfit.text(en("Profit Span")).model(log -> log.holdTimeOnProfitTrade).render(this::render);
        holdTimeForLoss.text(en("Loss Span")).model(log -> log.holdTimeOnLossTrade).render(this::render);
        realizedProfit.text(en("Realized Profit")).model(log -> log.profitRange).render(this::render);
        unrealizedProfit.text(en("Unrealized Profit")).model(log -> log.unrealizedProfitRange).render(this::render);
        realizedLoss.text(en("Realized Loss")).model(log -> log.lossRange).render(this::render);
        unrealizedLoss.text(en("Unrealized Loss")).model(log -> log.unrealizedLossRange).render(this::render);
        profit.text(en("Profit")).model(log -> log.profitAndLoss).render(this::render);
        total.text(en("Total Profit")).model(log -> log.profitAndLoss.formattedTotal());
        winRatio.text(en("Win Rate")).model(log -> log.winningRate());
        profitFactor.text(en("Profit Factor")).model(TradingStatistics::profitFactor);
        drawDown.text(en("Drawdown")).model(log -> log.drawDownRatio);
        scenarioCount.text(en("Trade Count")).model(log -> log).render(this::renderScenarioCount);
    }

    /**
     * Render scenario count.
     * 
     * @param cell
     * @param log
     */
    private void renderPeriod(UILabel label, TradingStatistics log) {
        UILabel remaining = make(UILabel.class).tooltip(en("Start")).text(Chrono.formatAsDate(log.startDate)).style(style.mean);
        UILabel total = make(UILabel.class).tooltip(en("End")).text(Chrono.formatAsDate(log.endDate)).style(style.mean);

        label.textV(remaining, total);
    }

    /**
     * Render scenario count.
     * 
     * @param cell
     * @param log
     */
    private void renderScenarioCount(UILabel label, TradingStatistics log) {
        UILabel remaining = make(UILabel.class).tooltip(en("Remaining")).text(log.active).style(style.mean);
        UILabel total = make(UILabel.class).tooltip(en("Total")).text(log.total).style(style.max);

        label.textV(remaining, total);
    }

    /**
     * Render position size.
     * 
     * @param cell
     * @param log
     */
    private void renderPositionSize(UILabel label, TradingStatistics log) {
        int target = marketSelection.value().setting.targetCurrencyScaleSize;

        UILabel mean = make(UILabel.class).tooltip(en("Remaining")).text(log.holdCurrentSize.scale(target)).style(style.mean);
        UILabel max = make(UILabel.class).tooltip(en("Max")).text(log.holdMaxSize.scale(target)).style(style.max);

        label.textV(mean, max);
    }

    /**
     * Render {@link Statistics}.
     * 
     * @param cell
     * @param statistics
     */
    private void render(UILabel label, Statistics statistics) {
        UILabel mean = make(UILabel.class).tooltip(en("Mean")).text(statistics.formattedMean()).style(style.mean);
        UILabel max = make(UILabel.class).tooltip(en("Max")).text(statistics.formattedMax()).style(style.max);
        UILabel min = make(UILabel.class).tooltip(en("Min")).text(statistics.formattedMin()).style(style.max);

        label.textV(mean, max, min);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize(Market market, List<Trader> traders) {
        chart.reduceRealtimeUpdate();
        chart.market.set(market);
        chart.scripts.clear();
        chart.scripts.addAll(I.signal(traders).flatMap(this::createTraderPlotScript).startWith(TraderVisualizer::new).toList());
    }

    /**
     * Create {@link PlotScript} from member class within {@link Trader}.
     * 
     * @param trader
     * @return
     */
    private Signal<Supplier<PlotScript>> createTraderPlotScript(Trader trader) {
        return I.signal(trader)
                .flatArray(t -> t.getClass().getDeclaredClasses())
                .take(clazz -> PlotScript.class.isAssignableFrom(clazz))
                .map(clazz -> Model.collectConstructors(clazz)[0])
                .effect(c -> c.setAccessible(true))
                .map(c -> (WiseSupplier<PlotScript>) () -> (PlotScript) c.newInstance(trader));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void analyze(Market market, List<TradingStatistics> logs, boolean detail) {
        for (TradingStatistics log : logs) {
            System.out.println(log);
            results.addItemAtLast(log);
        }

        Viewtify.inUI(() -> {
            logSelection.values(logs);

            market.dispose();
        });
    }
}
