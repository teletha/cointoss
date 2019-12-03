/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static trademate.CommonText.*;
import static transcript.Transcript.en;

import java.math.RoundingMode;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javafx.scene.control.Spinner;
import javafx.scene.input.ScrollEvent;

import cointoss.Direction;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.market.bitflyer.SFD;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.util.Num;
import kiss.I;
import kiss.WiseBiConsumer;
import stylist.Style;
import stylist.StyleDSL;
import trademate.TradeMateStyle;
import trademate.TradingView;
import viewtify.Viewtify;
import viewtify.ui.UI;
import viewtify.ui.UIButton;
import viewtify.ui.UILabel;
import viewtify.ui.UISpinner;
import viewtify.ui.UIText;
import viewtify.ui.View;
import viewtify.ui.helper.User;

public class OrderBuilder extends View {

    private Predicate<UIText> positiveNumber = ui -> {
        try {
            return Num.of(ui.value()).isPositive();
        } catch (

        NumberFormatException e) {
            return false;
        }
    };

    /** UI */
    private UIText orderSize;

    /** UI */
    private UISpinner<Num> orderSizeAmount;

    /** UI */

    UIText orderPrice;

    /** UI */
    private UISpinner<Num> orderPriceAmount;

    /** UI */
    private UISpinner<Integer> orderDivideSize;

    /** UI */
    private UISpinner<Integer> orderDivideIntervalAmount;

    /** UI */
    private UISpinner<Num> optimizeThreshold;

    /** UI */
    private UIText orderPriceInterval;

    /** UI */
    private UISpinner<Num> orderPriceIntervalAmount;

    /** UI */
    private UIButton orderLimitLong;

    /** UI */
    private UIButton orderLimitShort;

    /** UI */
    private UIButton orderCancel;

    /** UI */
    private UIButton orderStop;

    /** UI */
    private UIButton orderReverse;

    /** UI */
    private TradingView view;

    /** UI */
    private UILabel sfdPrice500;

    /** UI */
    private UILabel sfdPrice499;

    /** UI */
    private UILabel sfdPrice498;

    /** UI */
    private UILabel sfdPrice497;

    /** UI */
    private UILabel sfdPrice495;

    /** UI */
    private UILabel sfdPrice494;

    /** UI */
    private UILabel sfdPrice490;

    /**
     * {@inheritDoc}
     */
    @Override
    protected UI declareUI() {
        return new UI() {
            {
                $(vbox, S.Root, () -> {
                    $(hbox, S.Row, () -> {
                        label(Amount, S.Label);
                        $(orderSize, S.Form);
                        $(orderSizeAmount, S.FormMin);
                    });
                    $(hbox, S.Row, () -> {
                        label(Price, S.Label);
                        $(orderPrice, S.Form);
                        $(orderPriceAmount, S.FormMin);
                    });
                    $(hbox, S.Row, () -> {
                        label(en("Variances"), S.Label);
                        $(orderDivideSize, S.Form);
                        $(orderDivideIntervalAmount, S.FormMin);
                    });
                    $(hbox, S.Row, () -> {
                        label(en("Price Interval"), S.Label);
                        $(orderPriceInterval, S.Form);
                        $(orderPriceIntervalAmount, S.FormMin);
                    });
                    $(hbox, S.Row, () -> {
                        label(en("Threshold"), S.Label);
                        $(optimizeThreshold, S.Form);
                    });
                    $(hbox, S.Row, () -> {
                        $(orderLimitShort, S.FormButton, TradeMateStyle.Short);
                        $(orderLimitLong, S.FormButton, TradeMateStyle.Long);
                    });
                    $(hbox, S.Row, () -> {
                        $(orderCancel, S.FormButton);
                        $(orderStop, S.FormButton);
                        $(orderReverse, S.FormButton);
                    });

                    $(hbox, S.Row, () -> {
                        $(sfdPrice500, S.SFD);
                    });
                    $(hbox, S.Row, () -> {
                        $(sfdPrice499, S.SFD);
                    });
                    $(hbox, S.Row, () -> {
                        $(sfdPrice498, S.SFD);
                    });
                    $(hbox, S.Row, () -> {
                        $(sfdPrice497, S.SFD);
                    });

                    $(hbox, S.Row, () -> {
                        $(sfdPrice495, S.SFD);
                    });
                    $(hbox, S.Row, () -> {
                        $(sfdPrice494, S.SFD);
                    });
                    $(hbox, S.Row, () -> {
                        $(sfdPrice490, S.SFD);
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
        orderSize.initial("0").when(User.Scroll, changeBy(orderSizeAmount.ui)).require(positiveNumber);
        orderSizeAmount.values(0, view.service.setting.targetCurrencyBidSizes());

        orderPrice.initial("0").when(User.Scroll, changeBy(orderPriceAmount.ui)).require(positiveNumber);
        orderPriceAmount.values(0, Num.ONE, Num.HUNDRED, Num.THOUSAND, Num.of(10000));

        orderDivideSize.values(0, IntStream.range(1, 31).boxed());
        orderDivideIntervalAmount.values(0, IntStream.range(0, 20).boxed()).disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));
        optimizeThreshold.values(0, Num.range(0, 20));
        orderPriceInterval.initial("0")
                .when(User.Scroll, changeBy(orderPriceIntervalAmount.ui))
                .require(positiveNumber)
                .parent()
                .disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));
        orderPriceIntervalAmount.values(0, Num.TEN, Num.HUNDRED, Num.THOUSAND);

        // validate order condition
        orderLimitLong.parent().disableWhen(orderSize.isInvalid(), orderPrice.isInvalid());

        orderLimitLong.text(Buy).when(User.MouseClick).throttle(1000, MILLISECONDS).mapTo(Direction.BUY).to(this::requestOrder);
        orderLimitShort.text(Sell).when(User.MouseClick).throttle(1000, MILLISECONDS).mapTo(Direction.SELL).to(this::requestOrder);

        orderCancel.text(en("Cancel")).when(User.MouseClick).to(() -> view.market.orders.cancelNowAll());
        orderStop.text(en("Stop")).when(User.MouseClick).to(() -> view.market.stop().to(I.NoOP));
        orderReverse.text(en("Reverse")).when(User.MouseClick).to(() -> view.market.reverse().to(I.NoOP));

        if (view.market.service == BitFlyer.FX_BTC_JPY) {
            view.market.service.add(SFD.latestBTC.on(Viewtify.UIThread).to(price -> {
                sfdPrice500.text("5.00% " + price.multiply(1.05).scale(0));
                sfdPrice499.text("4.99% " + price.multiply(1.0499).scale(1));
                sfdPrice498.text("4.98% " + price.multiply(1.0498).scale(2));
                sfdPrice497.text("4.97% " + price.multiply(1.0497).scale(3));
                sfdPrice495.text("4.95% " + price.multiply(1.0495).scale(4));
                sfdPrice494.text("4.94% " + price.multiply(1.0494).scale(5));
                sfdPrice490.text("4.90% " + price.multiply(1.0490).scale(6));
            }));
        }
    }

    /**
     * Support wheel change.
     * 
     * @param source
     * @param amount
     * @return
     */
    private WiseBiConsumer<ScrollEvent, UIText> changeBy(Spinner<Num> spinner) {
        return (e, ui) -> {
            Num current = Num.of(ui.value());
            double deltaY = e.getDeltaY();

            if (deltaY > 0) {
                // increment
                ui.value(current.plus(spinner.getValue()).toString());
            } else if (deltaY < 0) {
                // decrement
                ui.value(Num.max(Num.ZERO, current.minus(spinner.getValue())).toString());
            }
        };
    }

    /**
     * Request order by API.
     * 
     * @param side
     * @return
     */
    private void requestOrder(Direction side) {
        Viewtify.inWorker(() -> {
            // ========================================
            // Create Model
            // ========================================
            Num size = orderSize.valueOr(Num.ZERO);
            Num initSize = size;
            Num price = orderPrice.valueOr(Num.ZERO);
            int divideSize = orderDivideSize.value();
            int increaseInterval = orderDivideIntervalAmount.value();
            Num priceInterval = orderPriceInterval.valueOr(Num.ZERO).multiply(side.isBuy() ? -1 : 1);

            for (int i = 0; i < divideSize; i++) {
                Num optimizedSize = increaseInterval == 0 ? Num.ZERO
                        : Num.of(i).divide(increaseInterval).scale(0, RoundingMode.FLOOR).multiply(initSize.divide(2));
                Num optimizedPrice = view.market.orderBook.computeBestPrice(side, price, optimizeThreshold.value(), Num.of(2));

                Order order = Order.with.direction(side, size.plus(optimizedSize)).price(optimizedPrice).state(OrderState.REQUESTING);
                price = optimizedPrice.plus(priceInterval);

                view.market.request(order).to(I.NoOP);
            }
        });
    }

    /**
     * @version 2018/09/09 9:14:18
     */
    private interface S extends StyleDSL {

        Style Root = () -> {
            padding.horizontal(10, px);
            display.minWidth(280, px);
        };

        Style Row = () -> {
            padding.top(8, px);
            text.verticalAlign.middle();
        };

        Style Label = () -> {
            display.width(80, px);
            display.height(27, px);
            padding.top(5, px);
        };

        Style SFD = () -> {
            display.width(260, px);
            display.height(27, px);
            padding.top(5, px);
        };

        Style Form = () -> {
            display.maxWidth(80, px).height(27, px);
        };

        Style FormMin = () -> {
            display.maxWidth(70, px).height(27, px);
            margin.left(15, px);
        };

        Style FormButton = () -> {
            display.width(62, px).height(31, px);
        };
    }
}
