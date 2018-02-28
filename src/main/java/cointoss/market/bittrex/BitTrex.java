/*
 * Copyright (C) 2018 CoinToss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss.market.bittrex;

import static cointoss.market.bittrex.BitTrexType.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import cointoss.util.Num;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/08/31 19:09:13
 */
public class BitTrex {

    private final Num fee = Num.of("0.9975");

    /**
     * Read ticker.
     * 
     * @param base
     * @param target
     * @return
     * @throws IOException
     */
    public Signal<BitTrexTicker> ticker(BitTrexType base, BitTrexType target) {
        try {
            return I.json(new URL("https://bittrex.com/api/v1.1/public/getticker?market=" + base + "-" + target))
                    .find("result", BitTrexTicker.class);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Read order book.
     * 
     * @param base
     * @param target
     * @return
     */
    public Signal<BitTrexOrderBook> orderbook(BitTrexType base, BitTrexType target) {
        try {
            return I.json(new URL("https://bittrex.com/api/v1.1/public/getorderbook?type=both&market=" + base + "-" + target))
                    .find("result", BitTrexOrderBook.class);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    private void check(BitTrexType type) {
        orderbook(BTC, type).combine(orderbook(ETH, type), orderbook(BTC, ETH)).to(o -> {
            BitTrexOrderBook btc2coin = o.ⅰ;
            BitTrexOrderBook eth2coin = o.ⅱ;
            BitTrexOrderBook btc2eth = o.ⅲ;

            Num btc = Num.ONE;

            // exchange to coin
            Num coin = btc.divide(btc2coin.middleAsk()).multiply(fee);

            // exchange to eth
            Num eth = coin.multiply(eth2coin.middleBid()).multiply(fee);

            // exchange to btc
            Num result = eth.multiply(btc2eth.middleBid()).multiply(fee);

            if (result.isGreaterThan(btc)) {
                System.out.println("BTC-" + type + "-ETH-BTC \t" + result + "  ");
            }

            btc = Num.ONE;

            // exchange to eth
            eth = btc.divide(btc2eth.middleAsk()).multiply(fee);

            // exchange to coin
            coin = eth.divide(eth2coin.middleAsk()).multiply(fee);

            // exchange to btc
            result = coin.multiply(btc2coin.middleBid()).multiply(fee);

            if (result.isGreaterThan(btc)) {
                System.out.println("BTC-ETH-" + type + "-BTC \t" + result);
            }
        });
    }

    /*
     * 
     */
    public static void main(String[] args) throws InterruptedException, MalformedURLException {
        BitTrex trex = new BitTrex();

        while (true) {
            I.signal(BitTrexType.values()).skip(BTC, ETH).take(t -> t.marketETH).to(type -> {
                trex.check(type);
            });

            Thread.sleep(20 * 1000);
        }

    }
}
