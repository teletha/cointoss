/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.order;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import cointoss.Position;
import cointoss.PositionManager;
import cointoss.Side;
import cointoss.order.Order;
import cointoss.order.OrderBookList;
import cointoss.util.Num;
import kiss.Variable;
import trademate.TradingView;
import viewtify.UI;
import viewtify.View;
import viewtify.Viewtify;
import viewtify.ui.UITableColumn;
import viewtify.ui.UITableView;

/**
 * @version 2018/04/26 14:54:03
 */
public class PositionCatalog extends View {

    /** The date formatter. */
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd HH:mm:ss");

    /** UI */
    private @UI UITableView<Position> positions;

    /** UI */
    private @UI UITableColumn<Position, ZonedDateTime> openPositionDate;

    /** UI */
    private @UI UITableColumn<Position, Side> openPositionSide;

    /** UI */
    private @UI UITableColumn<Position, Num> openPositionAmount;

    /** UI */
    private @UI UITableColumn<Position, Num> openPositionPrice;

    /** UI */
    private @UI UITableColumn<Position, Num> openPositionProfitAndLoss;

    /** Parent View */
    private @UI TradingView view;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void initialize() {
        Position positive1 = new Position();
        positive1.side = Side.BUY;
        positive1.date = ZonedDateTime.now();
        positive1.price = Num.of(700000);
        positive1.size = Variable.of(Num.ONE);

        Position positive2 = new Position();
        positive2.side = Side.BUY;
        positive2.date = ZonedDateTime.now();
        positive2.price = Num.of(700001);
        positive2.size = Variable.of(Num.ONE);
        PositionManager manager = view.market().positions;

        positions.ui.setItems(Viewtify.observe(manager.items, manager.added, manager.removed));

        manager.add(positive1);
        manager.add(positive2);

        openPositionDate.model(o -> o.date).render((ui, item) -> ui.text(formatter.format(item)));
        openPositionSide.model(o -> o.side).render((ui, item) -> ui.text(item).styleOnly(item));
        openPositionAmount.modelByVar(o -> o.size).header(Viewtify.calculate("数量 ").concat(manager.size).trim());
        openPositionPrice.model(o -> o.price).header(Viewtify.calculate("価格 ").concat(manager.price).trim());
        openPositionProfitAndLoss.modelByVar(o -> o.profit).header(Viewtify.calculate("損益 ").concat(manager.profit).trim());
        positions.selectMultipleRows().context($ -> {
            $.menu("撤退").whenUserClick(() -> positions.selection().forEach(this::retreat));
        });
    }

    /**
     * Request exit order.
     * 
     * @param position
     */
    private void retreat(Position position) {
        OrderBookList book = view.market().orderBook.bookFor(position.inverse());
        Num price = book.computeBestPrice(Num.ZERO, Num.TWO);

        view.order(Order.limit(position.inverse(), position.size.v, price));
    }
}
