/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss;

/**
 * @version 2018/02/28 16:28:41
 */
public enum MarketHealth {
    Normal("🌑"), Busy("🌘"), VeryBusy("🌗"), SuperBusy("🌖"), NoOrder("🌕"), Stop("💀");

    /** The human-readable status. */
    public final String mark;

    /**
     * @param mark
     */
    private MarketHealth(String mark) {
        this.mark = mark;
    }
}