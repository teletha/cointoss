/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package viewtify;

import javafx.event.EventType;
import javafx.scene.input.ScrollEvent;

/**
 * @version 2017/11/15 10:44:45
 */
public class User {

    /** User Action */
    public static EventType<ScrollEvent> Scroll = ScrollEvent.SCROLL;

    /** User Action */
    public static EventType<ScrollEvent> ScrollStart = ScrollEvent.SCROLL_STARTED;

    /** User Action */
    public static EventType<ScrollEvent> ScrollFinish = ScrollEvent.SCROLL_FINISHED;
}
