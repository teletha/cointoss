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

import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import icy.manipulator.Icy;
import kiss.Disposable;
import kiss.I;
import kiss.JSON;
import kiss.Observer;
import kiss.Signal;

@Icy
public abstract class EfficientWebSocketModel {

    /** Logging utility. */
    private static final Logger logger = LogManager.getLogger();

    /** The cached connection. */
    private WebSocket ws;

    /** Temporary buffer for commands called before the connection was established. */
    private final Deque<IdentifiableTopic> queue = new ArrayDeque();

    /** The signal tee. */
    private final Map<String, Supersonic> signals = new HashMap();

    /** The management of subscriptions. */
    private int subscriptions;

    /** The current subscribing topics. */
    private final Set<IdentifiableTopic> subscribings = ConcurrentHashMap.newKeySet();

    private boolean debug;

    @Icy.Property(copiable = true)
    public abstract String address();

    /**
     * Extract channel id from massage.
     * 
     * @param extractId An id extractor.
     * @return Chainable API.
     */
    @Icy.Property
    public abstract Function<JSON, String> extractId();

    /**
     * The subscription ID may be determined by the content of the response, so we must extract the
     * new ID from the response.
     * 
     * @return Chainable API.
     */
    @Icy.Property
    public Function<JSON, String> updateId() {
        return null;
    }

    /**
     * Sets the maximum number of subscriptions per connection. Default value is 25.
     * 
     * @param size The maximum number of subscriptions per connection. A number less than or equal
     *            to 0 is considered unlimited.
     * @return Chainable API.
     */
    @Icy.Property
    public int maximumSubscriptions() {
        return Integer.MAX_VALUE;
    }

    /**
     * Ignore JSON that match the specified criteria. This process is very efficient because it is
     * tried only once for each JSON data on the base stream.
     * 
     * @param condition
     * @return Chainable API.
     */
    @Icy.Property
    public Predicate<JSON> ignoreMessageIf() {
        return null;
    }

    /**
     * Reconnect socket when some message match the specified criteria.
     * 
     * @param condition
     * @return Chainable API.
     */
    @Icy.Property
    public Predicate<JSON> recconnectIf() {
        return null;
    }

    /**
     * Stop reconnecting socket when some message match the specified criteria.
     * 
     * @param condition
     * @return Chainable API.
     */
    @Icy.Property
    public Predicate<JSON> stopRecconnectIf() {
        return null;
    }

    /**
     * Invoked when connection is established.
     * 
     * @return
     */
    @Icy.Property
    public Consumer<WebSocket> whenConnected() {
        return null;
    }

    /**
     * Outputs a detailed log.
     */
    public EfficientWebSocket enableDebug() {
        this.debug = true;
        return (EfficientWebSocket) this;
    }

    /**
     * Outputs a detailed log.
     * 
     * @return Chainable API.
     */
    @Icy.Property(copiable = true)
    public HttpClient client() {
        return null;
    }

    /**
     * Outputs a detailed log.
     * 
     * @return Chainable API.
     */
    @Icy.Property(copiable = true)
    public ScheduledExecutorService scheduler() {
        return null;
    }

    /**
     * Outputs a detailed log.
     * 
     * @return Chainable API.
     */
    @Icy.Property(copiable = true)
    public cointoss.util.APILimiter limiter() {
        return null;
    }

    /**
     * Execute command on this connection.
     * 
     * @param topic A subscription command (i.e. bean-like object).
     * @return A shared connection.
     */
    public final synchronized Signal<JSON> subscribe(IdentifiableTopic topic) {
        Objects.requireNonNull(topic);

        return signals.computeIfAbsent(topic.id, id -> new Supersonic(topic)).expose;
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
            sendSubscriptionToRemote(topic);
        }

        if (subscriptions++ == 0) {
            connect();
        }
    }

    /**
     * Send subscription message to this websocket.
     * 
     * @param topic A topic to subscribe.
     */
    private void sendSubscriptionToRemote(IdentifiableTopic topic) {
        if (ws != null && subscribings.add(topic)) {
            topic.subscribing = I.schedule(0, 10, TimeUnit.SECONDS, true, scheduler()).to(count -> {
                APILimiter limiter = limiter();
                if (limiter != null) {
                    limiter.acquire();
                }
                ws.sendText(I.write(topic), true);
                logger.info("Sent websocket command {} to {}. @{}", topic, address(), count);
            });
        }
    }

    /**
     * Unsubscribe the specified topic.
     * 
     * @param topic A topic to unsubscribe.
     */
    private synchronized void snedUnsubscribe(IdentifiableTopic topic) {
        if (ws != null) {
            try {
                IdentifiableTopic unsubscribe = topic.unsubscribe();
                ws.sendText(I.write(unsubscribe), true);
                logger.info("Sent websocket command {} to {}.", unsubscribe, address());
            } catch (Throwable e) {
                // ignore
            } finally {
                if (subscriptions == 0 || --subscriptions == 0) {
                    disconnect("No Subscriptions");
                }
            }
        }
    }

    /**
     * Connect to the server by websocket.
     */
    private void connect() {
        logger.trace("Starting websocket [{}].", address());

        I.http(address(), ws -> {
            logger.trace("Connected websocket [{}].", address());

            Consumer<WebSocket> connected = whenConnected();
            if (connected != null) {
                connected.accept(ws);
            }

            this.ws = ws;
            for (IdentifiableTopic command : queue) {
                sendSubscriptionToRemote(command);
            }
            queue.clear();
        }, client()).to(debug ? I.bundle(this::outputTestCode, this::dispatch) : this::dispatch, e -> {
            error(e);
        }, () -> {
            disconnect(null);
            signals.values().forEach(signal -> signal.complete());
        });
    }

    /**
     * Disconnect websocket connection and send error message to all channels.
     */
    private void error(Throwable e) {
        disconnect(e.getMessage());
        signals.values().forEach(signal -> signal.error(e));
    }

    /**
     * Dispatch websocket message.
     * 
     * @param text
     */
    private void dispatch(String text) {
        JSON json = I.json(text);

        Predicate<JSON> reject = ignoreMessageIf();
        if (reject != null && reject.test(json)) {
            return;
        }

        Supersonic signaling = signals.get(extractId().apply(json));
        if (signaling != null) {
            signaling.accept(json);
        } else {
            for (IdentifiableTopic topic : subscribings) {
                if (topic.verifySubscribedReply(json)) {
                    topic.subscribing.dispose();
                    topic.subscribing = null;
                    subscribings.remove(topic);
                    logger.trace("Accepted websocket subscription [{}] {}.", address(), topic.id);

                    Function<JSON, String> updater = updateId();
                    if (updater != null) {
                        String newId = updater.apply(json);
                        signals.put(newId, signals.get(topic.id));
                        logger.trace("Update websocket [{}] subscription id from '{}' to '{}'.", address(), topic.id, newId);
                    }
                    return;
                }
            }

            Predicate<JSON> recconnect = recconnectIf();
            if (recconnect != null && recconnect.test(json)) {
                error(new ConnectException("Server was terminated by some error, Try to reconnect."));
                return;
            }

            Predicate<JSON> stopping = stopRecconnectIf();
            if (stopping != null && stopping.test(json)) {
                error(new ConnectException("Server was terminated by some error, Try to reconnect."));
                return;
            }

            // we can't handle message
            logger.warn("Unknown message was recieved. [{}] {}", address(), text);
        }
    }

    /**
     * Output test code.
     * 
     * @param text
     */
    private void outputTestCode(String text) {
        System.out.println("server.sendJSON(\"" + text.replace('"', '\'') + "\");");
    }

    /**
     * Send close message to disconnect this websocket.
     */
    private void disconnect(String message) {
        if (ws != null) {
            try {
                ws.sendClose(WebSocket.NORMAL_CLOSURE, "");
            } catch (Throwable ignore) {
                // ignore
            } finally {
                ws = null;
            }
        }

        if (subscriptions != 0) {
            subscriptions = 0;
            queue.clear();

            if (message == null) {
                logger.error("Disconnected websocket [{}] normally.", address());
            } else {
                logger.error("Disconnected websocket [{}] because of {}.", address(), message);
            }
        }
    }

    /**
     * Get the root cause.
     * 
     * @param e
     * @return
     */
    private static Throwable cause(Throwable e) {
        Throwable cause = e.getCause();
        while (cause != null) {
            e = cause;
            cause = e.getCause();
        }
        return e;
    }

    /**
     * Identifiable topic which can represents subscribe and unsubscribe commands.
     */
    public static abstract class IdentifiableTopic<T extends IdentifiableTopic> implements Cloneable {

        /** The identifier. */
        private final String id;

        /** The unsubscription command builder. */
        private final Consumer<T> unsubscribeCommandBuilder;

        /** The subscrib process. */
        private Disposable subscribing = Disposable.empty();

        /**
         * @param id
         * @param unsubscribeCommandBuilder
         */
        protected IdentifiableTopic(String id, Consumer<T> unsubscribeCommandBuilder) {
            if (id == null || id.isEmpty()) {
                throw new IllegalArgumentException("ID must be non-empty value.");
            }

            if (unsubscribeCommandBuilder == null) {
                throw new IllegalArgumentException("Can't unsubscribe command.");
            }
            this.id = id;
            this.unsubscribeCommandBuilder = unsubscribeCommandBuilder;
        }

        /**
         * Go to Unsubscribe mode.
         * 
         * @return
         */
        private IdentifiableTopic unsubscribe() {
            try {
                T cloned = (T) clone();
                unsubscribeCommandBuilder.accept(cloned);
                return cloned;
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }

        /**
         * Make sure your channel subscription has been properly accepted.
         * 
         * @return
         */
        protected abstract boolean verifySubscribedReply(JSON reply);

        /**
         * {@inheritDoc}
         */
        @Override
        public final String toString() {
            return I.write(this).replaceAll("\\s", "");
        }
    }

    /**
     * Supersonic {@link Signal} support subject.
     */
    private class Supersonic implements Observer<JSON> {

        /** The associated topic. */
        private IdentifiableTopic topic;

        /** The number of internal listeners. */
        private int size = 0;

        /** The internal listeners. */
        private Observer[] deployed = new Observer[0];

        /** The array manipulator. */
        private final ArrayList<Observer> observers = new ArrayList();

        /** The exposed interface. */
        private final Signal<JSON> expose = new Signal<>((observer, disposer) -> {
            observers.add(observer);
            deploy();

            if (size == 1) {
                sendSubscribe(topic);
            }

            return disposer.add(() -> {
                observers.remove(observer);
                deploy();

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
         * Deploy observers.
         */
        private void deploy() {
            deployed = observers.toArray(new Observer[observers.size()]);
            size = deployed.length;
        }

        @Override
        public void accept(JSON value) {
            for (int i = 0; i < size; i++) {
                deployed[i].accept(value);
            }
        }

        @Override
        public void complete() {
            for (int i = 0; i < size; i++) {
                deployed[i].complete();
            }
        }

        @Override
        public void error(Throwable error) {
            for (int i = 0; i < size; i++) {
                deployed[i].error(error);
            }
        }
    }

    // public static void main(String[] args) throws InterruptedException {
    // // Thread.setDefaultUncaughtExceptionHandler((e, x) -> {
    // // x.printStackTrace();
    // // });
    //
    // Market m = new Market(Bitfinex.BTC_USDT);
    // m.readLog(x -> x.fromToday(LogType.Fast).throttle(3, TimeUnit.SECONDS).effect(e -> {
    // System.out.println(e);
    // }));
    //
    // Thread.sleep(1000 * 220);
    // }
}
