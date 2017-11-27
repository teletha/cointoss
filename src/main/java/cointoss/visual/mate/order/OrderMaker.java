/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.visual.mate.order;

import static java.util.concurrent.TimeUnit.*;

import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ScrollEvent;
import javafx.util.Callback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.MarketBackend;
import cointoss.Order;
import cointoss.Order.Quantity;
import cointoss.Side;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;
import cointoss.visual.mate.Console;
import kiss.I;
import kiss.WiseBiConsumer;
import viewtify.User;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UIButton;
import viewtify.ui.UIComboBox;
import viewtify.ui.UISpinner;
import viewtify.ui.UIText;

/**
 * @version 2017/11/14 23:47:09
 */
public class OrderMaker extends View {

    /** LOGGING */
    private static final Logger logger = LogManager.getLogger();

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

    /** The backend service. */
    private final MarketBackend service = BitFlyer.FX_BTC_JPY.service();

    /** The backend service. */
    private final OrderManager manager = I.make(OrderManager.class);

    private @FXML UIText orderSize;

    private @FXML UISpinner<Num> orderSizeAmount;

    private @FXML UIText orderPrice;

    private @FXML UISpinner<Num> orderPriceAmount;

    private @FXML UISpinner<Integer> orderDivideSize;

    private @FXML UIText orderPriceInterval;

    private @FXML UISpinner<Num> orderPriceIntervalAmount;

    private @FXML UIButton orderLimitLong;

    private @FXML UIButton orderLimitShort;

    private @FXML UIComboBox<Quantity> orderQuantity;

    private @FXML TreeTableView<Object> requestedOrders;

    private @FXML TreeTableColumn<Object, Object> requestedOrdersDate;

    private @FXML TreeTableColumn<Object, Object> requestedOrdersSide;

    private @FXML TreeTableColumn<Object, Object> requestedOrdersAmount;

    private @FXML TreeTableColumn<Object, Object> requestedOrdersPrice;

    private @FXML Console console;

    /** The root item. */
    private final TreeItem<Object> root = new TreeItem();

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        orderSize.initial("0").when(User.Scroll, changeBy(orderSizeAmount.ui)).require(positiveNumber);
        orderSizeAmount.values(Num.of("0.01"), Num.of("0.1"), Num.ONE).initial(Num.ONE);

        orderPrice.initial("0").when(User.Scroll, changeBy(orderPriceAmount.ui)).require(positiveNumber);
        orderPriceAmount.values(Num.ONE, Num.HUNDRED, Num.THOUSAND).initial(Num.ONE);

        orderDivideSize.values(IntStream.range(1, 31).boxed()).initial(1);
        orderPriceInterval.initial("0")
                .when(User.Scroll, changeBy(orderPriceIntervalAmount.ui))
                .require(positiveNumber)
                .parent()
                .disableWhen(orderDivideSize.ui.valueProperty().isEqualTo(1));
        orderPriceIntervalAmount.values(Num.TEN, Num.HUNDRED, Num.THOUSAND).initial(Num.TEN);

        // validate order condition
        orderLimitLong.parent().disableWhen(orderSize.isInvalid().or(orderPrice.isInvalid()));

        orderLimitLong.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.BUY).to(this::requestOrder);
        orderLimitShort.when(User.Click).throttle(1000, MILLISECONDS).mapTo(Side.SELL).to(this::requestOrder);

        orderQuantity.values(Quantity.values()).initial(Quantity.GoodTillCanceled);

        // columns.get(1).setCellValueFactory(e -> new
        // SimpleObjectProperty(e.getValue().getValue().side()));
        // columns.get(2).setCellValueFactory(e -> new
        // SimpleObjectProperty(e.getValue().getValue().size()));
        // columns.get(3).setCellValueFactory(e -> new
        // SimpleObjectProperty(e.getValue().getValue().price()));

        requestedOrders.setRoot(root);
        requestedOrders.setShowRoot(false);
        requestedOrdersDate.setCellValueFactory(new OrderCellValueFactory(s -> new SimpleStringProperty(""), o -> o.child_order_date));
        requestedOrdersSide.setCellValueFactory(new OrderCellValueFactory(OrderSet::side, Order::sideProperty));
        requestedOrdersAmount.setCellValueFactory(new OrderCellValueFactory(OrderSet::amount, Order::size));
        requestedOrdersPrice.setCellValueFactory(new OrderCellValueFactory(OrderSet::averagePrice, Order::price));

        // columns.get(1).setCellValueFactory(c -> new
        // SimpleObjectProperty(c.getValue().getValue().side()));

        // requestedOrdersDate.setCellValueFactory(e -> e.);
        // requestedOrdersDate.setCellFactory(e -> new TableCell());
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
        Viewtify.inWorker(() -> {
            // ========================================
            // Create Model
            // ========================================
            OrderSet set = new OrderSet();

            Num size = orderSize.valueOr(Num.ZERO);
            Num price = orderPrice.valueOr(Num.ZERO);
            int divideSize = orderDivideSize.value();
            Num priceInterval = orderPriceInterval.valueOr(Num.ZERO).multiply(side.isBuy() ? -1 : 1);
            Quantity quantity = orderQuantity.value();

            for (int i = 0; i < divideSize; i++) {
                Order order = Order.limit(side, size, price).type(quantity);

                set.sub.add(order);

                price = price.plus(priceInterval);
            }

            // ========================================
            // Create View Model
            // ========================================
            TreeItem item;

            if (divideSize == 1) {
                item = new TreeItem(set.sub.get(0));
            } else {
                item = new TreeItem(set);
                item.setExpanded(true);

                // create sub orders for UI
                for (int i = 0; i < divideSize; i++) {
                    item.getChildren().add(new TreeItem(set.sub.get(i)));
                }
            }
            root.getChildren().add(item);

            // ========================================
            // Request to Server
            // ========================================
            for (Order order : set.sub) {
                logger.info("Request order [{}]", order);
                service.request(order).to(id -> {
                    System.out.println(id);
                });
            }
        });
    }

    /**
     * @version 2017/11/15 0:58:48
     */
    private static class PriceValueFactory extends SpinnerValueFactory<Num> {

        /** The amount of step. */
        private final Num amount;

        /**
         * @param amount
         */
        private PriceValueFactory(String amount) {
            this.amount = Num.of(amount);
            setValue(Num.ZERO);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void decrement(int steps) {
            Num value = getValue().minus(amount.multiply(steps));
            value = Num.max(value, Num.ZERO);

            setValue(value);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void increment(int steps) {
            Num value = getValue().plus(amount.multiply(steps));
            value = Num.min(value, Num.MAX);

            setValue(value);
        }
    }

    /**
     * @version 2017/11/19 15:40:13
     */
    private class Cell extends TableCell<Order, Object> {

        /**
         * {@inheritDoc}
         */
        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (item != null && !empty) {
                System.out.println(item);
            }
        }
    }

    /**
     * @version 2017/11/26 12:45:18
     */
    private static class OrderCellValueFactory
            implements Callback<TreeTableColumn.CellDataFeatures<Object, Object>, ObservableValue<Object>> {

        /** The value converter. */
        private final Function<OrderSet, ObservableValue> forSet;

        /** The value converter. */
        private final Function<Order, ObservableValue> forOrder;

        /**
         * @param forSet
         * @param forOrder
         */
        private OrderCellValueFactory(Function<OrderSet, ObservableValue> forSet, Function<Order, ObservableValue> forOrder) {
            this.forSet = forSet;
            this.forOrder = forOrder;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ObservableValue<Object> call(CellDataFeatures<Object, Object> features) {
            Object value = features.getValue().getValue();

            if (value instanceof OrderSet) {
                return forSet.apply((OrderSet) value);
            } else {
                return forOrder.apply((Order) value);
            }
        }
    }
}
