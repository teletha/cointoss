/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.util;

public interface PentaConsumer<Param1, Param2, Param3, Param4, Param5> {

    void accept(Param1 param1, Param2 param2, Param3 param3, Param4 param4, Param5 param5);
}
