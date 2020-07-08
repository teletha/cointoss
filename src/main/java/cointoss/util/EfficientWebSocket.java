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

import java.lang.reflect.Field;
import java.net.http.WebSocket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import cointoss.Market;
import cointoss.execution.ExecutionLog.LogType;
import cointoss.market.bitflyer.BitFlyer;
import kiss.I;
import kiss.JSON;
import kiss.Observer;
import kiss.Signal;

public class EfficientWebSocket {

    /** The connection address. */
    private final String uri;

    /** The id extractor. */
    private final Function<JSON, String> extractId;

    /** The maximum subscription size. */
    private final int max;

    /** The cached connection. */
    private WebSocket ws;

    /** The websocket's state. */
    private State state = State.Disconnected;

    /** Temporary buffer for commands called before the connection was established. */
    private final Deque<IdentifiableTopic> queued = new ArrayDeque();

    /** The signal tee. */
    private final Map<String, Supersonic<JSON>> signals = new HashMap();

    /** The reject filter. */
    private Predicate<JSON> reject;

    /** The processing when the ID changes depending on the content of the response. */
    private Function<JSON, String> update;

    /**
     * 
     * @param uri
     * @param max
     * @param extractId
     */
    public EfficientWebSocket(String uri, int max, Function<JSON, String> extractId) {
        this.uri = Objects.requireNonNull(uri);
        this.extractId = Objects.requireNonNull(extractId);
        this.max = max;
    }

    /**
     * The subscription ID may be determined by the content of the response, so we must extract the
     * new ID from the response.
     * 
     * @param extractNewId
     * @return
     */
    public final EfficientWebSocket updateIdBy(Function<JSON, String> extractNewId) {
        this.update = extractNewId;
        return this;
    }

    /**
     * Ignore JSONs that match the specified criteria. This process is very efficient because it is
     * tried only once for each JSON data on the base signal.
     * 
     * @param condition
     * @return
     */
    public final EfficientWebSocket ignoreIf(Predicate<JSON> condition) {
        this.reject = condition;
        return this;
    }

    /**
     * Execute command on this connection.
     * 
     * @param topic A subscription command (i.e. bean-like object).
     * @return A shared connection.
     */
    public final synchronized Signal<JSON> subscribe(IdentifiableTopic topic) {
        Objects.requireNonNull(topic);

        Supersonic<JSON> signal = signals.computeIfAbsent(topic.id, id -> {
            Supersonic<JSON> supersonic = new Supersonic(topic);

            // The subscription ID may be determined by the content of the response.
            if (update != null) {
                supersonic.size = 1;
                supersonic.expose.take(1).to(json -> signals.put(update.apply(json), signals.get(topic.id)));
            }

            return supersonic;
        });

        return signal.expose.effectOnDispose(() -> ws.sendText(I.write(topic.unsubscribeCommand), true));
    }

    /**
     * Close this connection.
     */
    public final void close() {
        if (ws != null) {
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "");
        }
    }

    private int size;

    /**
     * Try to subscribe the specified topic.
     * 
     * @param topic
     */
    private void trySubscribe(IdentifiableTopic topic) {
        size++;
        System.out.println("subscribe " + size);

        switch (state) {
        case Disconnected:
            connect();
            queued.add(topic);
            break;

        case Connecting:
            queued.add(topic);
            break;

        case Connected:
            ws.sendText(I.write(topic), true);
            break;

        case Disconnecting:
            // ignore
            break;
        }
    }

    /**
     * Connect websocket.
     */
    private void connect() {
        state = State.Connecting;

        I.http(uri, ws -> {
            synchronized (this) {
                System.out.println("Connect to " + uri);
                state = State.Connected;
                this.ws = ws;
                for (IdentifiableTopic command : queued) {
                    System.out.println("Command " + command.id);
                    ws.sendText(I.write(command), true);
                }
                queued.clear();
            }
        }).to(text -> {
            // System.out.println(text);
            JSON json = I.json(text);

            if (reject != null && reject.test(json)) {
                return;
            }

            Supersonic<JSON> signaling = signals.get(extractId.apply(json));
            if (signaling != null) {
                signaling.accept(json);
            }
        }, e -> {
            state = State.Disconnected;
            System.out.println("Error in WS " + uri);
            signals.values().forEach(signal -> signal.error(e));
        }, () -> {
            state = State.Disconnected;
            System.out.println("Complete WS " + uri);
            signals.values().forEach(signal -> signal.complete());
        });
    }

    /**
     * Try to unsubscribe the specified topic.
     * 
     * @param topic
     */
    private void tryUnsubscribe(IdentifiableTopic topic) {
        size--;
        System.out.println("unsbscribe " + size);

        switch (state) {
        case Disconnected:
            // ignore
            break;

        case Connecting:
            queued.remove(topic);
            break;

        case Connected:
            ws.sendText(I.write(topic.unsubscribe()), true);
            break;

        case Disconnecting:
            // ignore
            break;
        }
    }

    /**
     * Disconnect websocket.
     */
    private void disconnect() {
        state = State.Disconnecting;

        ws.sendClose(1000, "");
    }

    /**
     * State Pattern
     */
    private static enum State {
        Disconnected, Connecting, Connected, Disconnecting;
    }

    /**
     * 
     */
    public static abstract class IdentifiableTopic implements Cloneable {

        /** The identifier. */
        private final String id;

        /** The subscription command. */
        private final String subscribeCommand;

        /** The unsubscription command. */
        private final String unsubscribeCommand;

        /**
         * 
         */
        protected IdentifiableTopic(String id) {
            this(id, "subscribe", "unsubscribe");
        }

        /**
         * 
         */
        protected IdentifiableTopic(String id, String subscribeCommand, String unsubscribeCommand) {
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("ID must be non-empty value.");
            }
            this.id = id;
            this.subscribeCommand = Objects.requireNonNull(subscribeCommand);
            this.unsubscribeCommand = Objects.requireNonNull(unsubscribeCommand);
        }

        /**
         * Go to Unsubscribe mode.
         * 
         * @return
         */
        private IdentifiableTopic unsubscribe() {
            try {
                IdentifiableTopic cloned = (IdentifiableTopic) clone();
                for (Field field : getClass().getFields()) {
                    if (field.getType() == String.class && Objects.equals(field.get(cloned), subscribeCommand)) {
                        field.setAccessible(true);
                        field.set(cloned, unsubscribeCommand);
                        break;
                    }
                }
                return cloned;
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return I.write(this);
        }
    }

    /**
     * Supersonic {@link Signal} support subject.
     */
    private class Supersonic<V> implements Observer<V> {

        /** The associated topic. */
        private IdentifiableTopic topic;

        /** The number of internal listeners. */
        private int size = 0;

        /** The internal listeners. */
        private Observer[] observers = new Observer[0];

        /** The array manipulator. */
        private final ArrayList<Observer> holder = new ArrayList();

        /** The exposed interface. */
        private final Signal<V> expose = new Signal<>((observer, disposer) -> {
            if (size == 0) trySubscribe(topic);

            holder.add(observer);
            update();

            return disposer.add(() -> {
                holder.remove(observer);
                update();

                if (size == 0) tryUnsubscribe(topic);
            });
        });

        /**
         * @param topic
         */
        private Supersonic(IdentifiableTopic topic) {
            this.topic = topic;
        }

        private void update() {
            observers = holder.toArray(new Observer[holder.size()]);
            size = observers.length;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void accept(V value) {
            for (int i = 0; i < size; i++) {
                observers[i].accept(value);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void complete() {
            for (int i = 0; i < size; i++) {
                observers[i].complete();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void error(Throwable error) {
            for (int i = 0; i < size; i++) {
                observers[i].error(error);
            }
        }
    }

    /**
     * 
     */
    static class Command extends IdentifiableTopic {

        public long id = 123;

        public String jsonrpc = "2.0";

        public String method = "subscribe";

        public Map<String, String> params = new HashMap();

        /**
         * @param id
         */
        public Command(String channel, String marketName) {
            super("[" + channel + marketName + "]");
            params.put("channel", channel + marketName);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Market m = new Market(BitFlyer.FX_BTC_JPY);
        m.readLog(x -> x.fromToday(LogType.Fast).effect(e -> {
        }));

        Thread.sleep(1000 * 60 * 10);
    }

}
