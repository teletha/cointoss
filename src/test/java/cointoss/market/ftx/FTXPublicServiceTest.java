/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.ftx;

import java.util.List;

import org.junit.jupiter.api.Test;

import cointoss.Direction;
import cointoss.execution.Execution;
import cointoss.market.PublicServiceTestTemplate;
import cointoss.util.Chrono;

class FTXPublicServiceTest extends PublicServiceTestTemplate<FTXService> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected FTXService constructMarketService() {
        return construct(FTXService::new, FTX.BTC_USD.marketName, FTX.BTC_USD.setting);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executions() {
        httpClient.onGet().doReturnJSON("""
                {
                  "result": [
                    {
                      "id": 67425873,
                      "liquidation": false,
                      "price": 9540.0,
                      "side": "sell",
                      "size": 0.001,
                      "time": "2020-07-23T00:46:17.838284+00:00"
                    },
                    {
                      "id": 67425867,
                      "liquidation": false,
                      "price": 9540.5,
                      "side": "buy",
                      "size": 0.0004,
                      "time": "2020-07-23T00:46:16.720449+00:00"
                    },
                    {
                      "id": 67425812,
                      "liquidation": false,
                      "price": 9540.5,
                      "side": "buy",
                      "size": 0.0004,
                      "time": "2020-07-23T00:46:01.638901+00:00"
                    }
                  ]
                }
                """);

        List<Execution> list = service.executions(1, 10).toList();
        Execution e = list.get(0);
        assert e.id == 1828074164;
        assert e.direction == Direction.BUY;
        assert e.price.is(999224);
        assert e.size.is(0.1);
        assert e.date.isEqual(Chrono.utc(2020, 7, 13, 7, 41, 43, 97));
        assert e.buyer.equals("JRF20200713-074142-266150");
        assert e.seller.equals("JRF20200713-074142-809298");
        assert e.consecutive == Execution.ConsecutiveDifference;

        e = list.get(1);
        assert e.id == 1828074165;
        assert e.direction == Direction.BUY;
        assert e.price.is(999231);
        assert e.size.is(0.01);
        assert e.date.isEqual(Chrono.utc(2020, 7, 13, 7, 41, 43, 97));
        assert e.buyer.equals("JRF20200713-074142-266150");
        assert e.seller.equals("JRF20200713-074142-697549");
        assert e.consecutive == Execution.ConsecutiveSameBuyer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionLatest() {
        super.executionLatest();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimely() {
        super.executionRealtimely();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveBuy() {
        super.executionRealtimelyConsecutiveBuy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyConsecutiveSell() {
        super.executionRealtimelyConsecutiveSell();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Test
    public void executionRealtimelyWithMultipleChannels() {
        super.executionRealtimelyWithMultipleChannels();
    }
}
