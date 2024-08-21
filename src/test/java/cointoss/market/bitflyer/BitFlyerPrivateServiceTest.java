/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bitflyer;

import java.net.http.HttpClient;
import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.MarketService;
import cointoss.market.PrivateServiceTestTemplate;
import cointoss.order.Order;
import cointoss.order.OrderState;
import cointoss.util.Chrono;
import cointoss.util.EfficientWebSocket;
import kiss.I;

public class BitFlyerPrivateServiceTest extends PrivateServiceTestTemplate<BitFlyerService> {

    private class TestableService extends BitFlyerService {

        TestableService(MarketService service) {
            super(service.marketName, service.setting);
        }

        @Override
        protected HttpClient client() {
            return httpClient;
        }

        @Override
        protected EfficientWebSocket clientRealtimely() {
            try {
                if (websocketServer.hasReplyRule()) {
                    return super.clientRealtimely().withClient(websocketServer.httpClient()).withScheduler(chronus);
                } else {
                    usedRealWebSocket = true;
                    return super.clientRealtimely().enableDebug();
                }
            } catch (Exception e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected BitFlyerService constructMarketService() {
        return new TestableService(BitFlyer.FX_BTC_JPY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void orderActive() {
        httpClient.onGet().doReturnJSON("""
                [
                  {
                    "id": 0,
                    "child_order_id": "JFX20200714-015807-586848F",
                    "product_code": "FX_BTC_JPY",
                    "side": "SELL",
                    "child_order_type": "LIMIT",
                    "price": 1096329.0,
                    "average_price": 0.0,
                    "size": 0.01,
                    "child_order_state": "ACTIVE",
                    "expire_date": "2020-08-13T01:58:06",
                    "child_order_date": "2020-07-14T01:58:06",
                    "child_order_acceptance_id": "JRF20200714-015806-840451",
                    "outstanding_size": 0.01,
                    "cancel_size": 0.0,
                    "executed_size": 0.0,
                    "total_commission": 0.0
                  }
                ]
                """);

        List<Order> list = service.orders(OrderState.ACTIVE).waitForTerminate().toList();
        assert list.size() == 1;

        Order order = list.get(0);
        assert order.id.equals("JRF20200714-015806-840451");
        assert order.orientation.isNegative();
        assert order.type.isMaker();
        assert order.size.is(0.01);
        assert order.remainingSize().is(0.01);
        assert order.executedSize.is(0);
        assert order.price.is(1096329);
        assert order.creationTime.isEqual(Chrono.utc(2020, 7, 14, 1, 58, 6, 0));
        assert order.isActive();
        assert order.isNotCanceled();
        assert order.isNotCompleted();
        assert order.isNotExpired();
        assert order.isNotTerminated();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test

    public void orderActiveEmpty() {
        httpClient.onGet().doReturnJSON("[]");

        List<Order> list = service.orders(OrderState.ACTIVE).toList();
        assert list.size() == 0;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    @Test
    public void orderCanceled() {
        httpClient.onGet().doReturnJSON("""
                [
                  {
                    "id": 2022690384,
                    "child_order_id": "JFX20200710-956385-647201F",
                    "product_code": "FX_BTC_JPY",
                    "side": "BUY",
                    "child_order_type": "LIMIT",
                    "price": 986402.000000000000,
                    "average_price": 986402.000000000000,
                    "size": 0.500000000000,
                    "child_order_state": "CANCELED",
                    "expire_date": "2020-08-09T09:01:43",
                    "child_order_date": "2020-07-10T09:01:43",
                    "child_order_acceptance_id": "JRF20200710-956385-394856",
                    "outstanding_size": 0.000000000000,
                    "cancel_size": 0.200000000000,
                    "executed_size": 0.300000000000,
                    "total_commission": 0.000000000000
                  }
                ]
                """);

        List<Order> list = service.orders(OrderState.CANCELED).waitForTerminate().toList();
        assert list.size() == 1;

        Order order = list.get(0);
        assert order.id.equals("JRF20200710-956385-394856");
        assert order.orientation.isPositive();
        assert order.type.isMaker();
        assert order.size.is(0.5);
        assert order.remainingSize().is(0.2);
        assert order.executedSize.is(0.3);
        assert order.price.is(986402);
        assert order.creationTime.isEqual(Chrono.utc(2020, 7, 10, 9, 1, 43, 0));
        assert order.isNotActive();
        assert order.isCanceled();
        assert order.isNotCompleted();
        assert order.isNotExpired();
        assert order.isTerminated();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    @Test
    public void orderCanceledEmpty() {
        httpClient.onGet().doReturnJSON("[]");

        List<Order> list = service.orders(OrderState.CANCELED).toList();
        assert list.size() == 0;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    @Test
    public void orderCompleted() {
        httpClient.onGet().doReturnJSON("""
                [
                  {
                    "id": 2022690384,
                    "child_order_id": "JFX20200710-956385-647201F",
                    "product_code": "FX_BTC_JPY",
                    "side": "BUY",
                    "child_order_type": "LIMIT",
                    "price": 986402.000000000000,
                    "average_price": 986402.000000000000,
                    "size": 0.500000000000,
                    "child_order_state": "COMPLETED",
                    "expire_date": "2020-08-09T09:01:43",
                    "child_order_date": "2020-07-10T09:01:43",
                    "child_order_acceptance_id": "JRF20200710-956385-394856",
                    "outstanding_size": 0.000000000000,
                    "cancel_size": 0.000000000000,
                    "executed_size": 0.500000000000,
                    "total_commission": 0.000000000000
                  }
                ]
                """);

        List<Order> list = service.orders(OrderState.COMPLETED).waitForTerminate().toList();
        assert list.size() == 1;

        Order order = list.get(0);
        assert order.id.equals("JRF20200710-956385-394856");
        assert order.orientation.isPositive();
        assert order.type.isMaker();
        assert order.size.is(0.5);
        assert order.remainingSize().is(0);
        assert order.executedSize.is(0.5);
        assert order.price.is(986402);
        assert order.creationTime.isEqual(Chrono.utc(2020, 7, 10, 9, 1, 43, 0));
        assert order.isNotActive();
        assert order.isNotCanceled();
        assert order.isCompleted();
        assert order.isNotExpired();
        assert order.isTerminated();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    @Test
    public void orderCompletedEmpty() {
        httpClient.onGet().doReturnJSON("[]");

        List<Order> list = service.orders(OrderState.COMPLETED).toList();
        assert list.size() == 0;
    }

    /**
     * {@inheritDoc}
     */

    @Override
    @Test
    public void orders() {
        httpClient.onGet().doReturnJSON("""
                [
                  {
                    "id": 0,
                    "child_order_id": "JFX20200714-015807-586848F",
                    "product_code": "FX_BTC_JPY",
                    "side": "SELL",
                    "child_order_type": "LIMIT",
                    "price": 1096329.0,
                    "average_price": 0.0,
                    "size": 0.01,
                    "child_order_state": "ACTIVE",
                    "expire_date": "2020-08-13T01:58:06",
                    "child_order_date": "2020-07-14T01:58:06",
                    "child_order_acceptance_id": "JRF20200714-015806-840451",
                    "outstanding_size": 0.01,
                    "cancel_size": 0.0,
                    "executed_size": 0.0,
                    "total_commission": 0.0
                  },
                  {
                    "id": 2022690384,
                    "child_order_id": "JFX20200710-956385-647201F",
                    "product_code": "FX_BTC_JPY",
                    "side": "BUY",
                    "child_order_type": "LIMIT",
                    "price": 986402.000000000000,
                    "average_price": 986402.000000000000,
                    "size": 0.500000000000,
                    "child_order_state": "COMPLETED",
                    "expire_date": "2020-08-09T09:01:43",
                    "child_order_date": "2020-07-10T09:01:43",
                    "child_order_acceptance_id": "JRF20200710-956385-394856",
                    "outstanding_size": 0.000000000000,
                    "cancel_size": 0.000000000000,
                    "executed_size": 0.500000000000,
                    "total_commission": 0.000000000000
                  }
                ]
                """);

        List<Order> list = service.orders().waitForTerminate().toList();
        assert list.size() == 2;

        Order order = list.get(0);
        assert order.id.equals("JRF20200714-015806-840451");
        assert order.orientation.isNegative();
        assert order.type.isMaker();
        assert order.size.is(0.01);
        assert order.remainingSize().is(0.01);
        assert order.executedSize.is(0);
        assert order.price.is(1096329);
        assert order.creationTime.isEqual(Chrono.utc(2020, 7, 14, 1, 58, 6, 0));
        assert order.isActive();
        assert order.isNotCanceled();
        assert order.isNotCompleted();
        assert order.isNotExpired();
        assert order.isNotTerminated();

        order = list.get(1);
        assert order.id.equals("JRF20200710-956385-394856");
        assert order.orientation.isPositive();
        assert order.type.isMaker();
        assert order.size.is(0.5);
        assert order.remainingSize().is(0);
        assert order.executedSize.is(0.5);
        assert order.price.is(986402);
        assert order.creationTime.isEqual(Chrono.utc(2020, 7, 10, 9, 1, 43, 0));
        assert order.isNotActive();
        assert order.isNotCanceled();
        assert order.isCompleted();
        assert order.isNotExpired();
        assert order.isTerminated();
    }

    /**
     * {@inheritDoc}
     */

    @Override
    @Test
    public void ordersEmpty() {
        httpClient.onGet().doReturnJSON("[]");

        List<Order> list = service.orders().toList();
        assert list.size() == 0;
    }
}