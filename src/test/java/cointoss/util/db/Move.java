/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util.db;

import psychopath.Directory;
import psychopath.Locator;

public class Move {

    public static void main(String[] args) {
        Directory dest = Locator.directory("H:\\log");
        Directory from = Locator.directory(".log");
        from.copyTo(dest, "**/execution20201106.clog", "**/execution20201107.clog", "**/execution20201108.clog", "**/execution20201109.log");
    }
}
