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

import static trademate.setting.SettingStyles.*;

import java.time.Period;

import cointoss.Market;
import cointoss.MarketLog;
import cointoss.MarketService;
import cointoss.market.MarketProvider;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.ticker.TickSpan;
import cointoss.util.Chrono;
import kiss.Extensible;
import kiss.I;
import kiss.Manageable;
import kiss.Singleton;
import trademate.BackTestView.Message;
import trademate.chart.ChartView;
import viewtify.Viewtify;
import viewtify.dsl.UIDefinition;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UIDatePicker;
import viewtify.ui.View;
import viewtify.ui.helper.User;

/**
 * @version 2018/08/30 3:16:28
 */
public class BackTestView extends View<Message> {

    private UIComboBox<MarketService> market;

    private UIDatePicker startDate;

    private UIDatePicker endDate;

    private UIButton startButton;

    private ChartView chart;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UIDefinition declareUI() {
        return new UIDefinition() {
            {
                hbox(() -> {
                    $(chart);
                    vbox(() -> {
                        $(market);
                        hbox(FormRow, () -> {
                            label($.startDateLabel(), FormLabel);
                            $(startDate, FormInput);
                        });
                        hbox(FormRow, () -> {
                            label($.endDateLabel(), FormLabel);
                            $(endDate, FormInput);
                        });
                        $(startButton);
                    });
                });
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        market.values(0, MarketProvider.availableMarkets());
        startDate.initial(Chrono.utcNow().minusDays(10)).uneditable().requireWhen(market).require(() -> {
            MarketLog log = market.value().log;

            assert startDate.isBeforeOrSame(log.lastCacheDate()) : $.logIsNotFound();
            assert startDate.isAfterOrSame(log.firstCacheDate()) : $.logIsNotFound();
        }).observe((o, n) -> {
            endDate.value(v -> v.plus(Period.between(o, n)));
        });

        endDate.initial(Chrono.utcNow()).uneditable().require(() -> {
            assert startDate.isBeforeOrSame(endDate) : I.i18n(Message::endDateMustBeAfterStartDate);
        });

        // Market market = new Market(BitFlyer.BTC_JPY).readLog(log -> log.at(2018, 1, 17));

        // Viewtify.Terminator.add(market);
        // chart.market.set(market);
        // chart.ticker.set(market.tickers.tickerBy(TickSpan.Minute1));

        startButton.disableWhen(startDate.isInvalid()).when(User.MouseClick).to(e -> {
            Market m = new Market(BitFlyer.FX_BTC_JPY);
            chart.market.set(m);
            chart.ticker.set(m.tickers.tickerBy(TickSpan.Minute1));
            chart.market.to(v -> v.readLog(log -> log.range(startDate.zoned(), endDate.zoned())));

            Viewtify.Terminator.add(m);
        });
    }

    /**
     * @version 2018/08/03 17:01:00
     */
    @SuppressWarnings("unused")
    @Manageable(lifestyle = Singleton.class)
    static class Message implements Extensible {

        /**
         * Label for start button.
         * 
         * @return
         */
        String startButton() {
            return "Start";
        }

        /**
         * Label for start date.
         * 
         * @return
         */
        String startDateLabel() {
            return "Start Date";
        }

        /**
         * Label for end date.
         * 
         * @return
         */
        String endDateLabel() {
            return "End Date";
        }

        /**
         * Log is not found.
         * 
         * @return
         */
        public String logIsNotFound() {
            return "No logs were found for the specified date.";
        }

        /**
         * Log is not found.
         * 
         * @return
         */
        public String endDateMustBeAfterStartDate() {
            return "The end date must be after the start date.";
        }

        /**
         * Japanese bundle.
         * 
         * @version 2018/08/03 17:06:23
         */
        private static class Message_ja extends Message {

            /**
             * {@inheritDoc}
             */
            @Override
            String startButton() {
                return "開始";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String startDateLabel() {
                return "開始日";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String endDateLabel() {
                return "終了日";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String logIsNotFound() {
                return "指定された日付のログが見つかりません。";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            public String endDateMustBeAfterStartDate() {
                return "終了日は開始日よりも後にしてください。";
            }
        }
    }
}
