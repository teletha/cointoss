/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package cointoss.market.bittrex;

import static cointoss.market.bittrex.BitTrexType.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import eu.verdelhan.ta4j.Decimal;
import kiss.I;
import kiss.Signal;

/**
 * @version 2017/08/31 19:09:13
 */
public class BitTrex {

    private final Decimal fee = Decimal.valueOf("0.9975");

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

            Decimal btc = Decimal.ONE;

            // exchange to coin
            Decimal coin = btc.dividedBy(btc2coin.middleAsk()).multipliedBy(fee);

            // exchange to eth
            Decimal eth = coin.multipliedBy(eth2coin.middleBid()).multipliedBy(fee);

            // exchange to btc
            Decimal result = eth.multipliedBy(btc2eth.middleBid()).multipliedBy(fee);

            if (result.isGreaterThan(btc)) {
                System.out.println("BTC-" + type + "-ETH-BTC \t" + result + "  ");
            }

            btc = Decimal.ONE;

            // exchange to eth
            eth = btc.dividedBy(btc2eth.middleAsk()).multipliedBy(fee);

            // exchange to coin
            coin = eth.dividedBy(eth2coin.middleAsk()).multipliedBy(fee);

            // exchange to btc
            result = coin.multipliedBy(btc2coin.middleBid()).multipliedBy(fee);

            if (result.isGreaterThan(btc)) {
                System.out.println("BTC-ETH-" + type + "-BTC \t" + result);
            }
        });
    }

    /*
     * 
     */
    public static void main(String[] args) throws InterruptedException, MalformedURLException {
        I.load(Decimal.Codec.class, false);

        BitTrex trex = new BitTrex();

        while (true) {
            I.signal(BitTrexType.values()).skip(BTC, ETH).take(t -> t.marketETH).to(type -> {
                trex.check(type);
            });

            Thread.sleep(20 * 1000);
        }

    }
}
