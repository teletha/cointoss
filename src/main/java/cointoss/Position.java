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

import java.time.LocalDateTime;

import cointoss.util.Num;

/**
 * @version 2017/07/25 1:36:12
 */
public class Position {

    /** 建玉 */
    public Side side;

    /** 値段 */
    public Num price;

    /** サイズ */
    public Num size;

    public Num commission;

    /** 累積スワップポイント */
    public Num swap_point_accumulate;

    /** 保証金 */
    public Num require_collateral;

    /** 成立日 */
    public LocalDateTime open_date;

    /** レバレッジ */
    public int leverage;

    /** 確定損益合計 */
    public Num pnl;
}
