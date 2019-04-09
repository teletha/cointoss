/*
 * Copyright (C) 2019 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.setting;

import javafx.geometry.Pos;
import javafx.util.Duration;

import org.controlsfx.control.Notifications;

import com.google.common.base.Supplier;

import cointoss.util.Network;
import kiss.I;
import kiss.Storable;
import kiss.Variable;
import trademate.TradeMate;
import transcript.Transcript;
import viewtify.Viewtify;

/**
 * @version 2018/09/16 8:44:00
 */
public class Notificator implements Storable<Notificator> {

    private static final Transcript LongTrend = Transcript.en("Long Trend");

    private static final Transcript ShortTrend = Transcript.en("Short Trend");

    private static final Transcript Execution = Transcript.en("Execution");

    private static final Transcript OrderFailed = Transcript.en("Order Failed");

    private static final Transcript PriceSignal = Transcript.en("Price Signal");

    public final Notify longTrend = new Notify(LongTrend);

    public final Notify shortTrend = new Notify(ShortTrend);

    public final Notify execution = new Notify(Execution);

    public final Notify orderFailed = new Notify(OrderFailed);

    public final Notify priceSignal = new Notify(PriceSignal);

    /** The desktop position. */
    public final Variable<DesktopPosition> desktopPosition = Variable.of(DesktopPosition.BottomRight);

    /** The duration of desktop display. */
    public final Variable<java.time.Duration> desktopDuration = Variable.of(java.time.Duration.ofSeconds(1));

    /** The access token for LINE. */
    public final Variable<String> lineAccessToken = Variable.empty();

    /**
     * 
     */
    private Notificator() {
        restore().auto();
    }

    /**
     * @version 2018/08/30 10:30:06
     */
    public class Notify implements Supplier<String> {

        /** Showing desktop notification. */
        public final Variable<Boolean> desktop = Variable.of(false);

        /** Showing line notification. */
        public final Variable<Boolean> line = Variable.of(false);

        /** Showing notification pane. */
        public final Variable<Sound> sound = Variable.of(Sound.なし);

        /** The type name. */
        private final CharSequence name;

        /**
         * 
         */
        public Notify(CharSequence name) {
            this.name = name;
        }

        /**
         * Notify something.
         * 
         * @param message
         */
        public void notify(CharSequence message) {
            // by sound
            if (sound.isPresent() && sound.isNot(Sound.なし)) {
                sound.v.play();
            }

            // to desktop
            if (desktop.is(true)) {
                Viewtify.inUI(() -> {
                    Notifications.create()
                            .darkStyle()
                            .hideCloseButton()
                            .position(desktopPosition.v.position)
                            .hideAfter(Duration.seconds(desktopDuration.v.getSeconds()))
                            .text(message.toString())
                            .owner(I.make(TradeMate.class).screen())
                            .show();
                });
            }

            // to LINE
            if (line.is(true)) {
                I.make(Network.class).line(message).to(I.NoOP);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String get() {
            return name.toString();
        }
    }

    /**
     * 
     */
    public static enum DesktopPosition {
        TopLeft(Pos.TOP_LEFT, Transcript.en("TopLeft")),

        TopRight(Pos.TOP_RIGHT, Transcript.en("TopRight")),

        BottomLeft(Pos.BOTTOM_LEFT, Transcript.en("BottomLeft")),

        BottomRight(Pos.BOTTOM_RIGHT, Transcript.en("BottomRight"));

        /** The actual position. */
        private final Pos position;

        /** The readable text. */
        private final Transcript text;

        /**
         * @param position
         */
        private DesktopPosition(Pos position, Transcript text) {
            this.position = position;
            this.text = text;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return text.toString();
        }
    }
}
