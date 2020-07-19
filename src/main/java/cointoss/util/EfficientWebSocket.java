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
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import kiss.Disposable;
import kiss.I;
import kiss.JSON;
import kiss.Observer;
import kiss.Signal;

public class EfficientWebSocket implements Cloneable {

    /** Logging utility. */
    private static final Logger logger = LogManager.getLogger();

    /** The connection address. */
    private final String uri;

    /** The maximum subscription size. */
    private int max = 25;

    /** The ID extractor. */
    private final Function<JSON, String> extractId;

    /** The rejectable message pattern. */
    private Predicate<JSON> reject;

    /** The cached connection. */
    private WebSocket ws;

    /** Temporary buffer for commands called before the connection was established. */
    private final Deque<IdentifiableTopic> queue = new ArrayDeque();

    /** The signal tee. */
    private final Map<String, Supersonic> signals = new HashMap();

    /** The management of subscriptions. */
    private int subscriptions;

    /** The flag for loggin in detail. */
    private boolean debug;

    /** The http client. */
    private HttpClient[] client = new HttpClient[0];

    /** The current subscribing topics. */
    private final Set<IdentifiableTopic> subscribings = ConcurrentHashMap.newKeySet();

    /**
     * @param uri
     * @param max
     * @param extractId
     */
    public EfficientWebSocket(String uri, Function<JSON, String> extractId) {
        this.uri = Objects.requireNonNull(uri);
        this.extractId = Objects.requireNonNull(extractId);
    }

    /**
     * Sets the maximum number of subscriptions per connection. Default value is 25.
     * 
     * @param size The maximum number of subscriptions per connection. A number less than or equal
     *            to 0 is considered unlimited.
     * @return Chainable API.
     */
    public final EfficientWebSocket maximumSubscriptions(int size) {
        if (size <= 0) {
            size = Integer.MAX_VALUE;
        }
        this.max = size;
        return this;
    }

    /**
     * Ignore JSON that match the specified criteria. This process is very efficient because it is
     * tried only once for each JSON data on the base stream.
     * 
     * @param condition
     * @return Chainable API.
     */
    public final EfficientWebSocket ignoreMessageIf(Predicate<JSON> condition) {
        this.reject = condition;
        return this;
    }

    /**
     * Outputs a detailed log.
     * 
     * @return Chainable API.
     */
    public final EfficientWebSocket enableDebug() {
        this.debug = true;
        return this;
    }

    /**
     * Outputs a detailed log.
     * 
     * @return Chainable API.
     */
    public final EfficientWebSocket enableDebug(HttpClient client) {
        this.client = client == null ? new HttpClient[0] : new HttpClient[] {client};
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

        return signals.computeIfAbsent(topic.id, id -> new Supersonic(topic)).expose;
    }

    /**
     * The subscription ID may be determined by the content of the response, so we must extract the
     * new ID from the response.
     *
     * @param topic A target topic to update.
     * @param newId A new id of the topic.
     */
    public final void registerId(IdentifiableTopic topic, String newId) {
        signals.put(newId, signals.get(topic.id));
        logger.info("Update websocket [{}] subscription id from '{}' to '{}'.", uri, topic.id, newId);
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
            sendSubscription(topic);
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
    private void sendSubscription(IdentifiableTopic topic) {
        if (ws != null) {
            subscribings.add(topic);
            topic.subscribing = I.schedule(0, 3, TimeUnit.SECONDS, true).to(count -> {
                ws.sendText(I.write(topic), true);
                logger.info("Sent websocket command {} to {}. @{}", topic, uri, count);
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
            ws.sendText(I.write(topic.unsubscribe()), true);
            logger.info("Sent websocket command {} to {}.", topic, uri);
        }

        if (subscriptions == 0 || --subscriptions == 0) {
            disconnect();
            queue.clear();
        }
    }

    /**
     * Connect to the server by websocket.
     */
    private void connect() {
        logger.info("Starting websocket [{}].", uri);

        I.http(uri, ws -> {
            logger.info("Connected websocket [{}].", uri);

            this.ws = ws;
            for (IdentifiableTopic command : queue) {
                sendSubscription(command);
            }
            queue.clear();
        }, client).to(debug ? I.bundle(this::debug, this::dispatch) : this::dispatch, e -> {
            logger.error("Disconnected websocket [{}].", uri, cause(e));
            disconnect();
            signals.values().forEach(signal -> signal.error(e));
        }, () -> {
            logger.info("Finished websocket [{}].", uri);
            disconnect();
            signals.values().forEach(signal -> signal.complete());
        });
    }

    /**
     * Dispatch websocket message.
     * 
     * @param text
     */
    private void dispatch(String text) {
        JSON json = I.json(text);

        if (reject != null && reject.test(json)) {
            return;
        }

        Supersonic signaling = signals.get(extractId.apply(json));
        if (signaling != null) {
            signaling.accept(json);
        } else {
            for (IdentifiableTopic topic : subscribings) {
                if (topic.verifySubscribedReply(json)) {
                    subscribings.remove(topic);
                    topic.subscribing.dispose();
                    topic.subscribing = null;
                    logger.info("Accepted websocket subscription [{}] {}.", uri, topic.id);
                    return;
                }
            }
            logger.warn("Unknown message was recieved. [{}] {}", uri, text);
        }
    }

    /**
     * Output test code.
     * 
     * @param text
     */
    private void debug(String text) {
        System.out.println("server.sendJSON(\"" + text.replace('"', '\'') + "\");");
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
        subscriptions = 0;
        queue.clear();
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
     * {@inheritDoc}
     */
    @Override
    public EfficientWebSocket clone() {
        EfficientWebSocket cloned = new EfficientWebSocket(uri, extractId);
        cloned.maximumSubscriptions(max);
        cloned.ignoreMessageIf(reject);

        return cloned;
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
