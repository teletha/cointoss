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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @version 2017/08/26 16:22:59
 */
public interface Rule {

    /**
     * <p>
     * Check whether we can trade now or not.
     * </p>
     * 
     * @param market A target market.
     * @param exe A current execution.
     * @return A result.
     */
    boolean satisfy(Market market, Execution exe);

    /**
     * Compose rule.
     * 
     * @param rule Another rule.
     * @return A composed rule.
     */
    default Rule and(Rule rule) {
        return (m, e) -> satisfy(m, e) && rule.satisfy(m, e);
    }

    /**
     * Compose rule.
     * 
     * @param rule Another rule.
     * @return A composed rule.
     */
    default Rule or(Rule rule) {
        return (m, e) -> satisfy(m, e) || rule.satisfy(m, e);
    }

    /**
     * Compose rule.
     * 
     * @param rule Another rule.
     * @return A composed rule.
     */
    default Rule xor(Rule rule) {
        return (m, e) -> satisfy(m, e) ^ rule.satisfy(m, e);
    }

    /**
     * Compose rule.
     * 
     * @return A composed rule.
     */
    default Rule negate() {
        return (m, e) -> !satisfy(m, e);
    }

    /**
     * Compose rule.
     * 
     * @return A composed rule.
     */
    default Rule only(int times) {
        AtomicInteger count = new AtomicInteger(times);

        return (m, e) -> count.decrementAndGet() <= 0 ? satisfy(m, e) : false;
    }

    /**
     * <p>
     * Rule writer.
     * </p>
     * 
     * @param rule
     * @return
     */
    static Rule when(Rule rule) {
        return rule;
    }

    /**
     * @param timeLimit
     * @return
     */
    static Rule when(LocalDateTime timeLimit) {
        return (m, e) -> e.exec_date.isAfter(timeLimit);
    }
}
