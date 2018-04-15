/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.influxdb;

import java.io.IOException;

import kiss.I;

/**
 * @version 2018/04/14 22:29:47
 */
public class InfluxDB {

    /**
     * Launch database.
     */
    private void launch() {
        try {
            new ProcessBuilder("influxd.exe", "-config", "influxdb.conf").start();
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}
