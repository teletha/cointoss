/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bitflyer;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import cointoss.execution.Execution;
import icy.manipulator.Icy;

@Icy
public class BitFlyerExecution2Model extends Execution {

    /** The bitflyer ID date fromat. */
    private static final DateTimeFormatter Format = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    /** Buyer id of this execution. */
    public String buy_child_order_acceptance_id = "";

    /** Seller id of this execution. */
    public String sell_child_order_acceptance_id = "";

    /** The executed date-time. */
    public ZonedDateTime exec_date;

    /**
     * Estimate consecutive type.
     * 
     * @param previous
     */
    private void estimateConsecutiveType(BitFlyerExecution2 previous) {
        if (buy_child_order_acceptance_id.equals(previous.buy_child_order_acceptance_id)) {
            if (sell_child_order_acceptance_id.equals(previous.sell_child_order_acceptance_id)) {
                setConsecutive(Execution.ConsecutiveSameBoth);
            } else {
                setConsecutive(Execution.ConsecutiveSameBuyer);
            }
        } else if (sell_child_order_acceptance_id.equals(previous.sell_child_order_acceptance_id)) {
            setConsecutive(Execution.ConsecutiveSameSeller);
        } else {
            setConsecutive(Execution.ConsecutiveDifference);
        }
    }

    /**
     * <p>
     * Analyze Taker's order ID and obtain approximate order time (Since there is a bot which
     * specifies non-standard id format, ignore it in that case).
     * </p>
     * <ol>
     * <li>Execution Date : UTC</li>
     * <li>Server Order ID Date : UTC (i.e. stop-limit or IFD order)</li>
     * <li>User Order ID Date : JST+9:00</li>
     * </ol>
     *
     * @param exe
     * @return
     */
    private void estimateDelay() {
        String taker = direction.isBuy() ? buy_child_order_acceptance_id : sell_child_order_acceptance_id;

        try {
            // order format is like the following [JRF20180427-123407-869661]
            // exclude illegal format
            if (taker == null || taker.length() != 25 || !taker.startsWith("JRF")) {
                setDelay(DelayInestimable);
                return;
            }

            // remove tail random numbers
            taker = taker.substring(3, 18);

            // parse as datetime
            long orderTime = LocalDateTime.parse(taker, Format).toEpochSecond(ZoneOffset.UTC);
            long executedTime = exec_date.toEpochSecond() + 1;
            int diff = (int) (executedTime - orderTime);

            // estimate server order (over 9*60*60)
            if (diff < -32000) {
                diff += 32400;
            }

            if (diff < 0) {
                setDelay(DelayInestimable);
            } else if (180 < diff) {
                setDelay(DelayHuge);
            } else {
                setDelay(diff);
            }
        } catch (DateTimeParseException e) {
            setDelay(DelayInestimable);
        }
    }
}
