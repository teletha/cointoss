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
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableRow;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.ScrollEvent;
import javafx.util.Callback;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cointoss.MarketBackend;
import cointoss.Order;
import cointoss.Order.Quantity;
import cointoss.OrderState;
import cointoss.Side;
import cointoss.market.bitflyer.BitFlyer;
import cointoss.util.Num;
import cointoss.visual.mate.console.Console;
import kiss.Disposable;
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

    /** UI */
    private @FXML UIText orderSize;

    /** UI */
    private @FXML UISpinner<Num> orderSizeAmount;

    /** UI */
    private @FXML UIText orderPrice;

    /** UI */
    private @FXML UISpinner<Num> orderPriceAmount;

    /** UI */
    private @FXML UISpinner<Integer> orderDivideSize;

    /** UI */
    private @FXML UIText orderPriceInterval;

    /** UI */
    private @FXML UISpinner<Num> orderPriceIntervalAmount;

    /** UI */
    private @FXML UIButton orderLimitLong;

    /** UI */
    private @FXML UIButton orderLimitShort;

    /** UI */
    private @FXML UIComboBox<Quantity> orderQuantity;

    /** UI */
    private @FXML TreeTableView<Object> requestedOrders;

    /** UI */
    private @FXML TreeTableColumn<Object, Object> requestedOrdersDate;

    /** UI */
    private @FXML TreeTableColumn<Object, Object> requestedOrdersSide;

    /** UI */
    private @FXML TreeTableColumn<Object, Object> requestedOrdersAmount;

    /** UI */
    private @FXML TreeTableColumn<Object, Object> requestedOrdersPrice;

    /** UI */
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

        requestedOrders.setRoot(root);
        requestedOrders.setShowRoot(false);
        requestedOrders.setRowFactory(table -> new OrderStateRow());
        requestedOrdersDate.setCellValueFactory(new OrderStateValueCell(s -> new SimpleStringProperty(""), o -> o.child_order_date));
        requestedOrdersSide.setCellValueFactory(new OrderStateValueCell(OrderSet::side, Order::sideProperty));
        requestedOrdersAmount.setCellValueFactory(new OrderStateValueCell(OrderSet::amount, Order::size));
        requestedOrdersPrice.setCellValueFactory(new OrderStateValueCell(OrderSet::averagePrice, Order::price));

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
                order.child_order_state.set(OrderState.REQUESTING);

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
                    order.child_order_acceptance_id = id;
                    order.child_order_state.set(OrderState.ACTIVE);
                    logger.info("Accept order [{}]", order);
                });
            }
        });
    }

    /**
     * @version 2017/11/27 14:59:36
     */
    private static class OrderStateRow extends TreeTableRow<Object> {

        /** The bind manager. */
        private Disposable bind = Disposable.empty();

        /**
         * 
         */
        private OrderStateRow() {
            itemProperty().addListener((s, o, n) -> {
                if (o instanceof Order) {
                    bind.dispose();
                }

                if (n instanceof Order) {
                    bind = ((Order) n).child_order_state.observeNow().to(state -> Viewtify.style(this, state));
                }
            });
        }
    }

    /**
     * @version 2017/11/26 12:45:18
     */
    private static class OrderStateValueCell
            implements Callback<TreeTableColumn.CellDataFeatures<Object, Object>, ObservableValue<Object>> {

        /** The value converter. */
        private final Function<OrderSet, ObservableValue> forSet;

        /** The value converter. */
        private final Function<Order, ObservableValue> forOrder;

        /**
         * @param forSet
         * @param forOrder
         */
        private OrderStateValueCell(Function<OrderSet, ObservableValue> forSet, Function<Order, ObservableValue> forOrder) {
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
