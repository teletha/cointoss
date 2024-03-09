/*
 * Copyright (C) 2024 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market;

@SuppressWarnings("serial")
public class MaintenanceError extends Error {

    public MaintenanceError(String message) {
        super(message);
    }
}
