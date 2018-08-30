/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package trademate.preference;

import javafx.geometry.Pos;
import javafx.util.Duration;

import org.controlsfx.control.Notifications;

import cointoss.util.Network;
import kiss.Extensible;
import kiss.I;
import kiss.Variable;
import viewtify.Preference;
import viewtify.Viewtify;

/**
 * @version 2018/08/30 10:30:09
 */
public class Notificator extends Preference<Notificator> {

    /** The message resource. */
    private static final Lang message = I.i18n(Lang.class);

    public final Notify longTrend = new Notify(message.longTrend());

    public final Notify shortTrend = new Notify(message.shortTrend());

    public final Notify execution = new Notify(message.execution());

    public final Notify orderFailed = new Notify(message.orderFailed());

    public final Notify priceSignal = new Notify(message.priceSignal());

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
        restore().autoSave();
    }

    /**
     * @version 2018/08/30 10:30:06
     */
    public class Notify {

        /** Showing desktop notification. */
        public final Variable<Boolean> desktop = Variable.of(false);

        /** Showing line notification. */
        public final Variable<Boolean> line = Variable.of(false);

        /** Showing notification pane. */
        public final Variable<Sound> sound = Variable.of(Sound.なし);

        /** The type name. */
        private final String name;

        /**
         * 
         */
        public Notify(String name) {
            this.name = name;
        }

        /**
         * Notify something.
         * 
         * @param message
         */
        public void notify(String message) {
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
                            .text(message)
                            .owner(Viewtify.screen())
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
        public String toString() {
            return name;
        }
    }

    /**
     * @version 2018/08/27 20:42:39
     */
    public static enum DesktopPosition {
        TopLeft(Pos.TOP_LEFT), TopRight(Pos.TOP_RIGHT), BottomLeft(Pos.BOTTOM_LEFT), BottomRight(Pos.BOTTOM_RIGHT);

        /** The actual position. */
        private final Pos position;

        /**
         * @param position
         */
        private DesktopPosition(Pos position) {
            this.position = position;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            switch (this) {
            case TopLeft:
                return message.positionTopLeft();

            case TopRight:
                return message.positionTopRight();

            case BottomLeft:
                return message.positionBottomLeft();

            default:
                return message.positionBottomRight();
            }
        }
    }

    /**
     * @version 2018/08/27 20:45:08
     */
    private static class Lang implements Extensible {

        /**
         * Indicate desktop position.
         * 
         * @return
         */
        String positionTopLeft() {
            return "TopLeft";
        }

        /**
         * Indicate desktop position.
         * 
         * @return
         */
        String positionTopRight() {
            return "TopRight";
        }

        /**
         * Indicate desktop position.
         * 
         * @return
         */
        String positionBottomLeft() {
            return "BottomLeft";
        }

        /**
         * Indicate desktop position.
         * 
         * @return
         */
        String positionBottomRight() {
            return "BottomRight";
        }

        /**
         * Type name.
         * 
         * @return
         */
        String longTrend() {
            return "Long Trend";
        }

        /**
         * Type name.
         * 
         * @return
         */
        String shortTrend() {
            return "Short Trend";
        }

        /**
         * Type name.
         * 
         * @return
         */
        String execution() {
            return "Execution";
        }

        /**
         * Type name.
         * 
         * @return
         */
        String orderFailed() {
            return "Order Failed";
        }

        /**
         * Type name.
         * 
         * @return
         */
        String priceSignal() {
            return "Price Signal";
        }

        /**
         * @version 2018/08/27 20:47:17
         */
        @SuppressWarnings("unused")
        private static class Lang_ja extends Lang {

            /**
             * {@inheritDoc}
             */
            @Override
            String positionTopLeft() {
                return "左上";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String positionTopRight() {
                return "右上";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String positionBottomLeft() {
                return "左下";
            }

            /**
             * {@inheritDoc}
             */
            @Override
            String positionBottomRight() {
                return "右下";
            }

            @Override
            String longTrend() {
                return "買いトレンド";
            }

            @Override
            String shortTrend() {
                return "売りトレンド";
            }

            @Override
            String execution() {
                return "約定";
            }

            @Override
            String orderFailed() {
                return "注文失敗";
            }

            @Override
            String priceSignal() {
                return "指定値へ到達";
            }
        }
    }
}
