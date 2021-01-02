/*
 * Copyright (C) 2021 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.order;

/**
 * @version 2018/07/08 10:36:09
 */
public enum QuantityCondition {
    GoodTillCanceled("GTC"), ImmediateOrCancel("IOC"), FillOrKill("FOK");

    /** A standard abbreviation. */
    public final String abbreviation;

    /**
     * @param abbreviation
     */
    private QuantityCondition(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}