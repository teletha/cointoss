/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

import java.time.ZonedDateTime;

import cointoss.util.Num;
import kiss.Variable;

/**
 * @version 2017/07/25 1:36:12
 */
public class Position implements Directional {

    /** 建玉 */
    public Side side;

    /** 値段 */
    public Num price;

    /** サイズ */
    public Variable<Num> size = Variable.of(Num.ZERO);

    public Num commission;

    /** 累積スワップポイント */
    public Num swap_point_accumulate;

    /** 保証金 */
    public Num require_collateral;

    /** 成立日 */
    public ZonedDateTime open_date;

    /** レバレッジ */
    public int leverage;

    /** 確定損益合計 */
    public Variable<Num> pnl = Variable.of(Num.ZERO);

    /**
     * {@inheritDoc}
     */
    @Override
    public Side side() {
        return side;
    }
}
