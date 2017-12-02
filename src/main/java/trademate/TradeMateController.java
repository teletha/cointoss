/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package trademate;

import javafx.application.Platform;
import javafx.fxml.FXML;

/**
 * @version 2017/11/13 18:47:42
 */

public class TradeMateController {

    /**
     * Terminate this application.
     */
    @FXML
    void activeMenuQuit() {
        Platform.exit();
    }
}
