/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.position;

import cointoss.Direction;
import cointoss.MarketService;
import cointoss.execution.Execution;
import cointoss.util.Num;
import kiss.Variable;

public final class PositionManager {

    private final Variable<Num> amount = Variable.of(Num.ZERO);

    /** The total size. */
    public final Variable<Num> size = amount.observeNow().map(Num::abs).to();

    private final Variable<Num> cost = Variable.of(Num.ZERO);

    /** The average price. */
    public final Variable<Num> price = cost.observeNow()
            .combineLatest(amount.observeNow())
            .map(v -> v.ⅱ.isZero() ? Num.ZERO : v.ⅰ.divide(v.ⅱ))
            .to();

    /** The total profit and loss. */
    public final Variable<Num> profit;

    /**
     * Manage position.
     * 
     * @param latest A latest market {@link Execution} holder.
     */
    public PositionManager(MarketService service, Variable<Execution> latest) {
        this.profit = price.observeNow().combineLatest(latest.observeNow()).map(v -> v.ⅱ.price.minus(v.ⅰ).multiply(size)).to();

        service.add(service.executionsRealtimelyForMe().to(v -> add(v.ⅰ, v.ⅲ)));
    }

    /**
     * Check the position state.
     * 
     * @return A result.
     */
    public boolean hasPosition() {
        return amount.v.isNotZero();
    }

    /**
     * Check the position state.
     * 
     * @return A result.
     */
    public boolean hasNoPosition() {
        return amount.v.isZero();
    }

    /**
     * Cehck the position state.
     * 
     * @return
     */
    public boolean isLong() {
        return amount.v.isPositive();
    }

    /**
     * Cehck the position state.
     * 
     * @return
     */
    public boolean isShort() {
        return amount.v.isNegative();
    }

    void add(Execution e) {
        add(e.side, e);
    }

    /**
     * <p>
     * Update position by the specified my execution.
     * </p>
     * <p>
     * This method is separate for test.
     * </p>
     * 
     * @param exe A my execution.
     */
    void add(Direction direction, Execution e) {
        amount.set(v -> v.plus(direction, e.size));
        cost.set(v -> v.plus(direction, e.size.multiply(e.price)));
    }
}
