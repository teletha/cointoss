/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import com.google.common.base.Strings;

import cointoss.execution.Execution;
import cointoss.ticker.RealtimeTicker;
import cointoss.util.Chrono;
import cointoss.util.Primitives;
import stylist.Style;
import stylist.StyleDSL;
import viewtify.Viewtify;
import viewtify.style.FormStyles;
import viewtify.ui.UILabel;
import viewtify.ui.UIListView;
import viewtify.ui.UISpinner;
import viewtify.ui.View;
import viewtify.ui.ViewDSL;
import viewtify.util.Icon;

public class ExecutionView extends View {

    private UILabel delay;

    private UILabel countLong;

    private UILabel countShort;

    private UILabel countRatio;

    private UILabel volumeLong;

    private UILabel volumeShort;

    private UILabel volumeRatio;

    private UILabel losscutCountLong;

    private UILabel losscutCountShort;

    private UILabel losscutVolumeLong;

    private UILabel losscutVolumeShort;

    private UILabel spread;

    /** The execution list. */
    private UIListView<Execution> executionCumulativeList;

    /** UI for interval configuration. */
    private UISpinner<Integer> takerSize;

    /** Parent View */
    private TradingView view;

    class view extends ViewDSL implements TradeMateStyle, FormStyles {

        {
            $(vbox, style.root, FormLabelMin, () -> {
                form(en("Delay"), delay);
                form(en("Count"), FormInputMin, countLong.style(Long), countShort.style(Short), countRatio);
                form(en("Volume"), FormInputMin, volumeLong.style(Long), volumeShort.style(Short), volumeRatio);
                form(en("Losscut"), FormInputMin, losscutCountLong.style(Long), losscutCountShort.style(Short));
                form(en("Losssize"), FormInputMin, losscutVolumeLong.style(Long), losscutVolumeShort.style(Short));
                form(en("Spread"), FormInputMin, spread);

                $(hbox, () -> {
                    $(takerSize, style.takerSize);
                });
                $(executionCumulativeList, style.fill);
            });
        }
    }

    interface style extends StyleDSL {
        Style root = () -> {
            display.minWidth(180, px).maxWidth(180, px).height.fill();

            $.descendant(() -> {
                text.unselectable();
            });
        };

        Style takerSize = () -> {
            display.width(70, px);
        };

        Style fill = () -> {
            display.height.fill();
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        view.market.tickers.latest.observe()
                .take(view.chart.showRealtimeUpdate)
                .throttle(1000, TimeUnit.MILLISECONDS)
                .on(Viewtify.UIThread)
                .to(e -> {
                    long diff = Chrono.currentTimeMills() - e.mills;
                    if (diff < 0) {
                        delay.tooltip("The time on your computer may not be accurate.\r\nPlease synchronize the time with public NTP server.");
                        delay.ui.setGraphic(Icon.Error.image());
                    } else if (1000 < diff) {
                        delay.tooltip("You are experiencing significant delays and may be referring to outdated data.\r\nWe recommend that you stop trading.");
                        delay.ui.setGraphic(Icon.Error.image());
                    } else {
                        delay.untooltip();
                        delay.ui.setGraphic(null);
                    }
                    delay.text(diff + "ms");
                });

        view.market.orderBook.spread.observe().take(view.chart.showRealtimeUpdate).on(Viewtify.UIThread).to(price -> spread.text(price));

        RealtimeTicker realtime = view.market.tickers.realtime(60);
        Chrono.seconds().take(view.chart.showRealtimeUpdate).on(Viewtify.UIThread).to(() -> {
            int longCount = realtime.longCount();
            int shortCount = realtime.shortCount();
            double longVolume = realtime.longVolume();
            double shortVolume = realtime.shortVolume();

            countLong.text(longCount);
            countShort.text(shortCount);
            countRatio.text(Primitives.percent(longCount, longCount + shortCount));

            volumeLong.text(Primitives.roundString(longVolume, 1));
            volumeShort.text(Primitives.roundString(shortVolume, 1));
            volumeRatio.text(Primitives.percent(longVolume, longVolume + shortVolume));

            losscutCountLong.text(realtime.longLosscutCount());
            losscutCountShort.text(realtime.shortLosscutCount());

            losscutVolumeLong.text(Primitives.roundString(realtime.longLosscutVolume(), 1));
            losscutVolumeShort.text(Primitives.roundString(realtime.shortLosscutVolume(), 1));
        });

        int scale = view.market.service.setting.target.scale;

        // configure UI
        takerSize.initialize(IntStream.range(1, 51).boxed());
        executionCumulativeList.render((label, e) -> update(label, e, scale)).take(takerSize, (e, size) -> size <= e.accumulative);

        // load big taker log
        view.service.add(view.market.timelineByTaker.take(view.chart.showRealtimeUpdate.observing()).on(Viewtify.UIThread).to(e -> {
            if (1 <= e.accumulative) {
                executionCumulativeList.addItemAtFirst(e);

                if (1000 < executionCumulativeList.size()) {
                    executionCumulativeList.removeItemAtLast();
                }
            }
        }));
    }

    private void update(UILabel label, Execution e, int scale) {
        String text = Chrono.system(e.date).format(Chrono.Time) + "  " + Strings.padEnd(e.price.toString(), 5, ' ') + " \t" + Primitives
                .roundString(e.accumulative, scale);

        label.text(text).styleOnly(TradeMateStyle.Side.of(e.direction));
    }
}