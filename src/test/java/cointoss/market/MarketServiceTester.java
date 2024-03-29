/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market;

import java.lang.reflect.Constructor;
import java.net.http.HttpClient;
import java.util.Arrays;
import java.util.concurrent.Callable;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import com.pgssoft.httpclient.HttpClientMock;
import com.pgssoft.httpclient.RecordableHttpClientMock;

import antibug.Chronus;
import antibug.WebSocketServer;
import antibug.WebSocketServer.WebSocketClient;
import cointoss.MarketService;
import cointoss.util.EfficientWebSocket;
import hypatia.Num;
import kiss.I;
import kiss.Model;
import kiss.WiseBiFunction;
import kiss.WiseFunction;
import kiss.WiseTriFunction;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.scaffold.subclass.ConstructorStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.SuperCall;
import net.bytebuddy.matcher.ElementMatchers;

@Execution(ExecutionMode.SAME_THREAD)
public abstract class MarketServiceTester<S extends MarketService> {

    static {
        I.load(Num.class);
    }

    /** The mocked http client interface. */
    protected final HttpClientMock httpClient = RecordableHttpClientMock.build();

    /** The mocked websocket server. */
    protected final WebSocketServer websocketServer = new WebSocketServer();

    /** The websocket client interface. */
    protected final WebSocketClient websocketClient = websocketServer.websocketClient();

    /** The manageable scheduler. */
    protected final Chronus chronus = new Chronus();

    /** The testing {@link MarketService} which can mock the network access.. */
    protected S service;

    /** The target market service class to test. */
    private Class<S> base;

    /** The mocked market service class. */
    private Class<S> mocked;

    /** The debug state. */
    private boolean usedRealWebSocket;

    /**
     * Create mocked class at runtime for each test case instances.
     */
    @BeforeEach
    void before() {
        base = (Class<S>) Model.collectParameters(getClass(), MarketServiceTester.class)[0];
        mocked = (Class<S>) new ByteBuddy().subclass(base, ConstructorStrategy.Default.IMITATE_SUPER_CLASS)
                .method(ElementMatchers.namedOneOf("client", "clientRealtimely"))
                .intercept(MethodDelegation.to(new MockedNetworkSupplier()))
                .make()
                .load(base.getClassLoader())
                .getLoaded();

        try {
            service = constructMarketService();
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    @AfterEach
    void after() throws Exception {
        if (usedRealWebSocket) {
            Thread.sleep(12500);
        }
    }

    /**
     * Returns a mocked API, overriding the API for network access.
     */
    protected class MockedNetworkSupplier {

        public HttpClient client() {
            return httpClient;
        }

        public EfficientWebSocket clientRealtimely(@SuperCall Callable<EfficientWebSocket> superMethod) {
            try {
                if (websocketServer.hasReplyRule()) {
                    return superMethod.call().withClient(websocketServer.httpClient()).withScheduler(chronus);
                } else {
                    usedRealWebSocket = true;
                    return superMethod.call().enableDebug();
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * Create a market service for testing.
     * 
     * @return
     * @see MarketServiceTester#construct(WiseFunction, Object)
     * @see MarketServiceTester#construct(WiseBiFunction, Object, Object)
     * @see MarketServiceTester#construct(WiseTriFunction, Object, Object, Object)
     */
    protected abstract S constructMarketService();

    /**
     * Create a mocked market service for testing.
     * 
     * @param constructor A reference for constructor.
     * @param param1 A first parameter on constructor.
     * @return A mocked service.
     */
    protected final <P1> S construct(WiseFunction<P1, S> constructor, P1 param1) {
        try {
            return findConstructor(param1).newInstance(param1);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Create a mocked market service for testing.
     * 
     * @param constructor A reference for constructor.
     * @param param1 A first parameter on constructor.
     * @param param2 A second parameter on constructor.
     * @return A mocked service.
     */
    protected final <P1, P2> S construct(WiseBiFunction<P1, P2, S> constructor, P1 param1, P2 param2) {
        try {
            return findConstructor(param1, param2).newInstance(param1, param2);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Create a mocked market service for testing.
     * 
     * @param constructor A reference for constructor.
     * @param param1 A first parameter on constructor.
     * @param param2 A second parameter on constructor.
     * @param param3 A third parameter on constructor.
     * @return A mocked service.
     */
    protected final <P1, P2, P3> S construct(WiseTriFunction<P1, P2, P3, S> constructor, P1 param1, P2 param2, P3 param3) {
        try {
            return findConstructor(param1, param2, param3).newInstance(param1, param2, param3);
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * Find the suitable constructor.
     * 
     * @param params
     * @return
     */
    private Constructor<S> findConstructor(Object... params) {
        for (Constructor<?> constructor : mocked.getDeclaredConstructors()) {
            if (matchParameterTypes(constructor.getParameterTypes(), params)) {
                constructor.setAccessible(true);
                return (Constructor<S>) constructor;
            }
        }
        throw new Error("Suitable constructor is not found. " + Arrays.toString(mocked.getDeclaredConstructors()));
    }

    /**
     * Matching parameter types.
     * 
     * @param types
     * @param params
     * @return
     */
    private boolean matchParameterTypes(Class[] types, Object[] params) {
        if (types.length != params.length) {
            return false;
        }

        for (int i = 0; i < types.length; i++) {
            if (!I.wrap(types[i]).isInstance(params[i])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Notify no implementation error.
     */
    void notImplemented() {
        throw new UnsupportedOperationException("Please implement test case");
    }
}