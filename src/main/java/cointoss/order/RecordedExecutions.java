/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.order;

import java.util.LinkedList;

import cointoss.Execution;
import kiss.Signaling;

/**
 * @version 2018/07/08 11:55:02
 */
@SuppressWarnings("serial")
public class RecordedExecutions extends LinkedList<Execution> {

    /** The event publisher. */
    final Signaling<Execution> additions = new Signaling();

    /**
     * Record the related execution.
     * 
     * @param execution A related execution to record.
     */
    public void record(Execution execution) {
        addLast(execution);
        additions.accept(execution);
    }
}
