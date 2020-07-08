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
import cointoss.market.bitmex.BitMex;
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

    /** Temporary buffer for commands called before the connection was established. */
    private final Deque<IdentifiableTopic> queue = new ArrayDeque();

    /** The signal tee. */
    private final Map<String, Supersonic<JSON>> signals = new HashMap();

    /** The reject filter. */
    private Predicate<JSON> reject;

    /** The processing when the ID changes depending on the content of the response. */
    private Function<JSON, String> update;

    /** The management of subscriptions. */
    private int subscriptions;

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

            return supersonic;
        });

        return signal.expose.effectOnDispose(() -> send(topic.unsubscribe()));
    }

    /**
     * Subscribe the specified topic.
     * 
     * @param topic A topic to subscribe.
     */
    private synchronized void sendSubscribe(IdentifiableTopic topic) {
        if (ws == null) {
            queue.add(topic);
        } else {
            send(topic);
        }

        if (subscriptions++ == 0) {
            connect();
        }
    }

    /**
     * Unsubscribe the specified topic.
     * 
     * @param topic A topic to unsubscribe.
     */
    private synchronized void snedUnsubscribe(IdentifiableTopic topic) {
        send(topic.unsubscribe());

        if (--subscriptions == 0) {
            disconnect();
            queue.clear();
        }
    }

    /**
     * Send message to this websocket.
     * 
     * @param topic A topic to subscribe.
     */
    private void send(IdentifiableTopic topic) {
        if (ws != null) {
            try {
                System.out.println("Send " + topic.id);
                ws.sendText(I.write(topic), true);
            } catch (Throwable e) {
                // ignore
            }
        }
    }

    /**
     * Connect to the server by websocket.
     */
    private void connect() {
        I.http(uri, ws -> {
            this.ws = ws;
            for (IdentifiableTopic command : queue) {
                send(command);
            }
            queue.clear();
        }).to(text -> {
            JSON json = I.json(text);

            if (reject != null && reject.test(json)) {
                return;
            }

            Supersonic<JSON> signaling = signals.get(extractId.apply(json));
            if (signaling != null) {
                signaling.accept(json);
            }
        }, e -> {
            System.out.println("Error in WS " + uri);
            signals.values().forEach(signal -> signal.error(e));
        }, () -> {
            System.out.println("Complete WS " + uri);
            signals.values().forEach(signal -> signal.complete());
        });
    }

    /**
     * Send close message to disconnect this websocket.
     */
    private void disconnect() {
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "");
            } catch (Throwable e) {
                // ignore
            } finally {
                ws = null;
            }
        }
    }

    /**
     * Identifiable topic which can represents subscribe and unsubscribe commands.
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
            holder.add(observer);
            update();

            if (size == 1) {
                registerUpdater();
                sendSubscribe(topic);
            }

            return disposer.add(() -> {
                holder.remove(observer);
                update();

                if (size == 0) snedUnsubscribe(topic);
            });
        });

        /**
         * Bind to topic.
         * 
         * @param topic
         */
        private Supersonic(IdentifiableTopic topic) {
            this.topic = topic;
        }

        /**
         * Update observers.
         */
        private void update() {
            observers = holder.toArray(new Observer[holder.size()]);
            size = observers.length;
        }

        /**
         * The subscription ID may be determined by the content of the response.
         */
        private void registerUpdater() {
            if (update != null) {
                class Updater implements Observer<JSON> {

                    @Override
                    public void accept(JSON json) {
                        // update id
                        signals.put(update.apply(json), signals.get(topic.id));
                        System.out.println("Update ID " + update.apply(json) + "  " + signals.keySet());

                        // remove myself
                        holder.remove(this);
                        update();
                    }
                }

                // Add updater at head, we will make sure that only this updater is processed for
                // the next event.
                holder.add(0, new Updater());
                update();
                size = 1;
            }
        }

        @Override
        public void accept(V value) {
            for (int i = 0; i < size; i++) {
                observers[i].accept(value);
            }
        }

        @Override
        public void complete() {
            for (int i = 0; i < size; i++) {
                observers[i].complete();
            }
        }

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
        // Thread.setDefaultUncaughtExceptionHandler((e, x) -> {
        // x.printStackTrace();
        // });

        Market m = new Market(BitMex.XBT_USD);
        m.readLog(x -> x.fromToday(LogType.Fast).effect(e -> {
            System.out.println(e);
        }));

        Thread.sleep(1000 * 60 * 10);
    }

}
