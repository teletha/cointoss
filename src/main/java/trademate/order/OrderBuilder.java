/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate.order;

import static cointoss.order.Order.State.*;
import static java.util.concurrent.TimeUnit.*;

import java.math.RoundingMode;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javafx.scene.control.Spinner;
import javafx.scene.input.ScrollEvent;
import javafx.scene.media.AudioClip;

import cointoss.Side;
import cointoss.order.Order;
import cointoss.order.Order.Quantity;
import cointoss.order.Order.State;
import cointoss.trader.Spreader;
import cointoss.util.Num;
import kiss.I;
import kiss.WiseBiConsumer;
import trademate.Notificator;
import trademate.TradingView;
import viewtify.UI;
import viewtify.User;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIButton;
import viewtify.ui.UICheckBox;
import viewtify.ui.UIComboBox;
import viewtify.ui.UISpinner;
import viewtify.ui.UIText;

/**
 * @version 2017/11/27 23:21:48
 */
public class OrderBuilder extends View {

    private Predicate<UIText> positiveNumber = ui -> {
        try {
            return Num.of(ui.text()).isPositive();
        } catch (

        NumberFormatException e) {
            return false;
        }
    };

    private Predicate<UIText> negativeNumber = ui -> {
        try {
            return Num.of(ui.text()).isNegative();
        } catch (

        NumberFormatException e) {
            return false;
        }
    };

    /** UI */
    private @UI UIText orderSize;

    /** UI */
    private @UI UISpinner<Num> orderSizeAmount;

    /** UI */
    private @UI UIText orderPrice;

    /** UI */
    private @UI UISpinner<Num> orderPriceAmount;

    /** UI */
    private @UI UISpinner<Integer> orderDivideSize;

    /** UI */
    private @UI UISpinner<Integer> orderDivideIntervalAmount;

    /** UI */
    private @UI UISpinner<Num> optimizeThreshold;

    /** UI */
    private @UI UIText orderPriceInterval;

    /** UI */
    private @UI UISpinner<Num> orderPriceIntervalAmount;

    /** UI */
    private @UI UIButton orderLimitLong;

    /** UI */
    private @UI UIButton orderLimitShort;

    /** UI */
    private @UI UIComboBox<Quantity> orderQuantity;

    /** UI */
    private @UI TradingView view;

    /** UI */
    private @UI UICheckBox bot;

    private Notificator notificator = I.make(Notificator.class);

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderSize.initial("0").when(User.Scroll, changeBy(orderSizeAmount.ui)).require(positiveNumber);
        orderSizeAmount.values(2, Num.of("0.01"), Num.of("0.1"), Num.ONE);

        orderPrice.initial("0").when(User.Scroll, changeBy(orderPriceAmount.ui)).require(positiveNumber);
        orderPriceAmount.values(0, Num.ONE, Num.HUNDRED, Num.THOUSAND, Num.of(10000));

        orderDivideSize.values(0, IntStream.range(1, 31).boxed());
        orderDivideIntervalAmount.values(0, IntStream.range(0, 10).boxed()).disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));
        optimizeThreshold.values(0, Num.range(0, 20));
        orderPriceInterval.initial("0")
                .when(User.Scroll, changeBy(orderPriceIntervalAmount.ui))
                .require(positiveNumber)
                .parent()
                .disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));
        orderPriceIntervalAmount.values(0, Num.TEN, Num.HUNDRED, Num.THOUSAND);

        // validate order condition
        orderLimitLong.parent().disableWhen(orderSize.isInvalid().or(orderPrice.isInvalid()));

        orderLimitLong.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.BUY).to(this::requestOrder);
        orderLimitShort.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.SELL).to(this::requestOrder);

        orderQuantity.values(Quantity.values()).initial(Quantity.GoodTillCanceled).observe(v -> {
            try {
                notificator.orderFailed.notify("OKOKOK");
                AudioClip audioClip = new AudioClip(ClassLoader.getSystemResource("sound/Start.m4a").toExternalForm());
                audioClip.play();
                System.out.println("PLAY");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Spreader spreader = new Spreader();

        bot.observe(use -> {
            if (use) {
                view.market().add(spreader);
            } else {
                view.market().remove(spreader);
            }
        });
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
            Num current = Num.of(ui.text());
            double deltaY = e.getDeltaY();

            if (deltaY > 0) {
                // increment
                ui.text(current.plus(spinner.getValue()));
            } else if (deltaY < 0) {
                // decrement
                ui.text(current.minus(spinner.getValue()));
            }
        };
    }

    /**
     * Request order by API.
     * 
     * @param side
     * @return
     */
    private void requestOrder(Side side) {
        OrderSet set = new OrderSet();

        // ========================================
        // Create Model
        // ========================================
        Num size = orderSize.valueOr(Num.ZERO);
        Num price = orderPrice.valueOr(Num.ZERO);
        int divideSize = orderDivideSize.value();
        int increaseInterval = orderDivideIntervalAmount.value();
        Num priceInterval = orderPriceInterval.valueOr(Num.ZERO).multiply(side.isBuy() ? -1 : 1);
        Quantity quantity = orderQuantity.value();
        long group = System.nanoTime();

        for (int i = 0; i < divideSize; i++) {
            Num optimizedSize = increaseInterval == 0 ? Num.ZERO
                    : Num.of(i).divide(increaseInterval).scale(0, RoundingMode.FLOOR).multiply(orderSizeAmount.value());
            Num optimizedPrice = view.market().orderBook.computeBestPrice(side, price, optimizeThreshold.value(), Num.of(2));

            Order order = Order.limit(side, size.plus(optimizedSize), optimizedPrice).type(quantity);
            order.state.set(State.REQUESTING);
            order.state.observe().take(CANCELED, COMPLETED).take(1).to(() -> set.sub.remove(order));
            order.group = group;

            set.sub.add(order);

            price = optimizedPrice.plus(priceInterval);
        }

        // ========================================
        // Create View Model
        // ========================================
        view.orders.createOrderItem(set);

        // ========================================
        // Request to Server
        // ========================================
        for (Order order : set.sub) {
            Viewtify.inWorker(() -> {
                view.market().request(order).to(o -> {
                }, e -> {
                    notificator.orderFailed.notify("Reject order " + order);
                });
            });
        }
    }

}
