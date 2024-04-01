# Changelog

## [1.6.0](https://github.com/teletha/cointoss/compare/v1.5.0...v1.6.0) (2024-04-01)


### Features

* add alias method Direction#isBuy and #isSell ([caca812](https://github.com/teletha/cointoss/commit/caca812d80ed2c8a469cd1c044304bb294487477))
* export arithmetic classes as hypatia ([aca7c61](https://github.com/teletha/cointoss/commit/aca7c61c447b407ce22c181e22a8982c42718427))
* remove Directional, use Orientational instead ([0d3e3d7](https://github.com/teletha/cointoss/commit/0d3e3d7d1a33981d98f3c6a1bbce88605775bee7))
* remove order related events on OrderManager ([58dd36d](https://github.com/teletha/cointoss/commit/58dd36db31f355c5416f38476c43cfec7ef3abbe))


### Bug Fixes

* OrderManager should ignore the duplicated order ([e38701a](https://github.com/teletha/cointoss/commit/e38701aaaa743f3d73a2dc72aad2968f37017ce3))
* pass all tests in order management ([89c80e5](https://github.com/teletha/cointoss/commit/89c80e50654d72d670a7b9b64c8b2e38e55fcbfd))
* refactoring order manager ([c71f2ef](https://github.com/teletha/cointoss/commit/c71f2effd098f21c36d3b2548493ba23d523bcba))
* remove deprecated method ([8732a77](https://github.com/teletha/cointoss/commit/8732a7784efba6ed1c8b735f453571fe763bda20))
* update hypatia ([d2c2679](https://github.com/teletha/cointoss/commit/d2c267999a9fe644a76b6a92eb39b17d66a4af34))
* VerifiableMarketService should use the partial order execution ([aa0a968](https://github.com/teletha/cointoss/commit/aa0a968ad244e4bcaed164858c72cf97f19a1738))

## [1.5.0](https://github.com/teletha/cointoss/compare/v1.4.0...v1.5.0) (2024-03-28)


### Features

* add Makable#makeClaster ([9dcfe04](https://github.com/teletha/cointoss/commit/9dcfe048461a42fa0e5aab0776db9372fe24ef6d))
* add RetryPolicy#delayOnLimitOverflow ([803d301](https://github.com/teletha/cointoss/commit/803d3017dd6aef12762e1ba9189ac5bf0f532397))
* add RetryPolicy#delayOnMaintenance ([8dec7d8](https://github.com/teletha/cointoss/commit/8dec7d8732f70a3a2dfb25e533d0266a6717356f))
* add various markets on binance ([fb78baa](https://github.com/teletha/cointoss/commit/fb78baa8d9a2c7493dcfadff24b9fc0c363aea80))
* add various markets on bybit ([718b7f3](https://github.com/teletha/cointoss/commit/718b7f3c93b03978e9bd69786b6d2cdea089f027))
* all rest access is running in worker threads now ([e90ad9c](https://github.com/teletha/cointoss/commit/e90ad9c10330b4cbd13a71951122ad9ededbf873))
* change log layout ([db4065b](https://github.com/teletha/cointoss/commit/db4065b7ecb044dbb3e57055c499338d8a1f09ab))
* configure drawing frame rate ([ca64a1f](https://github.com/teletha/cointoss/commit/ca64a1fdba7c74afcc539c2ba384ccc407ee3df5))
* drop historical tarde flag ([7bbfdc0](https://github.com/teletha/cointoss/commit/7bbfdc08aa59c7689b05f87629d855d3a7237dd8))
* fast local log scan ([889088a](https://github.com/teletha/cointoss/commit/889088a7de618a22af70f461105307647dacc65f))
* integrate chart info and name ([7eb1b5d](https://github.com/teletha/cointoss/commit/7eb1b5dd2b4cc14b083280592d52b3f6bb6ae81b))
* isolate scanning of log repository ([fb4beb9](https://github.com/teletha/cointoss/commit/fb4beb961162958515dd79e7829a031542e7a119))
* make entry cancelable ([f07c19e](https://github.com/teletha/cointoss/commit/f07c19e10ae290f2660a8c55cbda5105648f9298))
* order like cluster ([27ee8e3](https://github.com/teletha/cointoss/commit/27ee8e348b5f643ef3e43412856ce5c0ffc42a24))
* provide chart part system ([69c3d7b](https://github.com/teletha/cointoss/commit/69c3d7b62d8bc539a69102c93a731d3960f90262))
* provide market name and info parts ([2ca16d9](https://github.com/teletha/cointoss/commit/2ca16d9274d6da919cf17c8088dcae5b1f2cef12))
* provide price-ranged volume part ([1c3b41e](https://github.com/teletha/cointoss/commit/1c3b41eb9a6cfee767ddaac019a24ad9774c092a))
* remove FTX completely ([c821d34](https://github.com/teletha/cointoss/commit/c821d344356f0e99becf8a36052922181ecb3cee))
* remove FTX from volume view ([d6085a0](https://github.com/teletha/cointoss/commit/d6085a00c6f9b8fb526eceb60601f48cb13cdd9c))
* remove Huobi completely ([f754bbd](https://github.com/teletha/cointoss/commit/f754bbd892548ff99adfc73cd9c78aa6a583d777))
* remove Liquid completely ([cf05b7e](https://github.com/teletha/cointoss/commit/cf05b7ec00102467443e2c1392ab0d1c1df8e2fd))
* remove Market#cancel ([f14f2f6](https://github.com/teletha/cointoss/commit/f14f2f692acfcdd45745576aaec7a4f87edfc2e3))
* remove Market#open, #close and #ticker. ([ad6d603](https://github.com/teletha/cointoss/commit/ad6d603f336b7e30c4dcec88f8d3e9f661376ceb))
* remove TemporalData ([0612887](https://github.com/teletha/cointoss/commit/0612887962ea4f98d84b01e96a95705e94be6daf))
* separate BinanceF from Binance ([36b03df](https://github.com/teletha/cointoss/commit/36b03dfde23052c1b826af610b5f3a40b529a5b5))
* share realtime stream ([671ca4a](https://github.com/teletha/cointoss/commit/671ca4a92c7895f3c6853dd9ccdbd9844081c560))
* update bybit and gmo api ([e4c65c6](https://github.com/teletha/cointoss/commit/e4c65c6cfc5bf1e535594511fba3b16f8dc85724))
* update commons-net ([60df66b](https://github.com/teletha/cointoss/commit/60df66b2140566cc863719590f5e47d597d54e7b))


### Bug Fixes

* avoid NPE ([6ddd02b](https://github.com/teletha/cointoss/commit/6ddd02b3272d1a107bfc853ca25291156c2d2420))
* avoid NPE ([39052e5](https://github.com/teletha/cointoss/commit/39052e56bc82d8702c9b8f4db9f8d1bc9302ea5e))
* Bitflyer logger is broken in some markets. ([d8ec406](https://github.com/teletha/cointoss/commit/d8ec406122a74cc122bfef772cfee0c58ac2f7b5))
* bybit api v5 ([7f6422f](https://github.com/teletha/cointoss/commit/7f6422f7a75ed606c37618e3ad4f27f6e63d1be3))
* Bybit orderbook is broken ([63084b4](https://github.com/teletha/cointoss/commit/63084b4d71d431cc699e1a45af6df2db4d56ac16))
* change color by order direction ([53a9746](https://github.com/teletha/cointoss/commit/53a9746c27db48ac6eec96f83084a245698f7c45))
* cleanup ExecutionLog API ([7ab42e3](https://github.com/teletha/cointoss/commit/7ab42e37b2fa94a8f65045eb5f1e34189a43509b))
* divided by zero ([5f6cdb2](https://github.com/teletha/cointoss/commit/5f6cdb2b396cd9f78af204f2be4a237937124053))
* drop event related api from ChartPart ([acab136](https://github.com/teletha/cointoss/commit/acab1368e2a26126c04e17b5d98d11d6d7d5f846))
* make market initialisation more smoothly ([26a67f0](https://github.com/teletha/cointoss/commit/26a67f0a1707f862e91eb9a118e26a56aebc48a0))
* manage chart parts ([9ba7c04](https://github.com/teletha/cointoss/commit/9ba7c04ce0a3465a0619929f754010561d6ed116))
* remove bucket4j ([68fed5f](https://github.com/teletha/cointoss/commit/68fed5f72df9aea64d796c5ef0927d22db4aa82a))
* show setting view with window mode ([d7a5e38](https://github.com/teletha/cointoss/commit/d7a5e382cf920257a3c19c372e2c6e8d217c1225))
* update ci process ([0e8454a](https://github.com/teletha/cointoss/commit/0e8454ae3f173976adf4a71b7a7fb991a2e9d9d0))
* update license ([4c162bf](https://github.com/teletha/cointoss/commit/4c162bf8a20cedb9310c241a67826131cf2fb8ff))
* update translation ([0490e22](https://github.com/teletha/cointoss/commit/0490e226302089b3ac699e701981c2b09d6d1c0c))
* update viewtify ([4097ab8](https://github.com/teletha/cointoss/commit/4097ab8efab868ab2f791107adb904f13ab8e29a))
* update viewtify ([2c9c609](https://github.com/teletha/cointoss/commit/2c9c6093d2561adf95fc914dff36db03478bdd1a))
* use .env ([ac43b2e](https://github.com/teletha/cointoss/commit/ac43b2e3859486dcca821fce2a21686cb55d484d))

## [1.4.0](https://github.com/teletha/cointoss/compare/v1.3.0...v1.4.0) (2023-12-02)


### Features

* Add ASTR and GLMR spot market on Binance. ([f67048f](https://github.com/teletha/cointoss/commit/f67048f0e860bbbf292017181af0559e64d51b03))
* Add global setting. ([63ca6ae](https://github.com/teletha/cointoss/commit/63ca6ae6178a8934c52390fbbc09a32a04f7f020))
* Add SOL, AVAX, LUNA markets on Binance and FTX. ([1084f5d](https://github.com/teletha/cointoss/commit/1084f5daa9c8146f31f2d5aca25d146f96f2aa5c))
* Purge primitive collections as primavera. ([e8866e5](https://github.com/teletha/cointoss/commit/e8866e54a8eb550bff2b5ecad11decd85e5eb4de))
* TradingExit interface. ([9d73976](https://github.com/teletha/cointoss/commit/9d73976a0b2aeecc9c16f2e2d6a92d7f5bb7eb36))
* TradingFilter provides timezone restrictions. ([d36c64f](https://github.com/teletha/cointoss/commit/d36c64f8b064ae6c6f108b4d4f2e471105739e52))
* update binance market ([9cf0c88](https://github.com/teletha/cointoss/commit/9cf0c88a1d27262558f06a4323e7b19ff5561bb5))


### Bug Fixes

* Bitflyer order size. ([48bdd1b](https://github.com/teletha/cointoss/commit/48bdd1b6df39f8f5611ea055554e71e596f1ac0f))
* Decrease the number of retry. ([ce24257](https://github.com/teletha/cointoss/commit/ce242571f2e91491f2105299d15ec378eba2874c))
* Drop all FTX markets. ([91caecf](https://github.com/teletha/cointoss/commit/91caecf69f27d9e8c00a71ad1c3991832228dc42))
* Memory leak. ([6b40fa2](https://github.com/teletha/cointoss/commit/6b40fa23d43fc792acb68dbf2c6b8cb7ff09a2cd))
* PriceVolume is not timescalable now. ([d28bc4c](https://github.com/teletha/cointoss/commit/d28bc4c401c7210157a37eabb321cf7776cf1b29))
* Remove LUNA market on FTX. ([c6ab51f](https://github.com/teletha/cointoss/commit/c6ab51fc6ce453aabf1156a8b9897972810982ed))
* Setting UI is broken. ([a9b5aeb](https://github.com/teletha/cointoss/commit/a9b5aeb4c27863f37d996da3e853089ca8f1283a))
* Test never generate entry log. ([cecf3a7](https://github.com/teletha/cointoss/commit/cecf3a79ca6d2e8c1195565b49600f8c0c96515f))
* update binance ([804c4cc](https://github.com/teletha/cointoss/commit/804c4cc2404f78a003c290f6f9814d68f6f74ac2))
* update binance pair ([c1ef48b](https://github.com/teletha/cointoss/commit/c1ef48ba1bb350eaaefd26270b19037f4425c748))
* update future market ([9f2da2f](https://github.com/teletha/cointoss/commit/9f2da2f150f21935431f06df7cf3d962ac087fe7))
* update market on binancef and ftx. ([9549abc](https://github.com/teletha/cointoss/commit/9549abcd5ad281c546e80b09af1ee02ca289c350))
* update sinobu ([7f1e63c](https://github.com/teletha/cointoss/commit/7f1e63cd7d4d20597c9de9528711ac7c588354c5))
* update sinobu ([76d0425](https://github.com/teletha/cointoss/commit/76d04254d26d5f8ef217c3ea9ac7250ccd80b7a7))
* Update sinobu. ([886bff1](https://github.com/teletha/cointoss/commit/886bff19af69c1419f107768ef00bbcf260b066f))
* update view ([2b967c3](https://github.com/teletha/cointoss/commit/2b967c31abc35bb2e0318cc374e8baadde6f111f))
* update viewtify ([5d84fd2](https://github.com/teletha/cointoss/commit/5d84fd23660344083a8bc24e3904e70fa1a902fb))
* update viewtify ([8a9ba5c](https://github.com/teletha/cointoss/commit/8a9ba5c3eb0d7ceaae015dbaf44b88228de167be))
* update viewtify ([de53b2f](https://github.com/teletha/cointoss/commit/de53b2fcd173aee823bcecc5cba341fcf61b7d6e))
* update viewtify ([dfa017c](https://github.com/teletha/cointoss/commit/dfa017c40f427234f8b52bf5ca7b3f0b35c0ab85))
* update viewtify ([6ed46d9](https://github.com/teletha/cointoss/commit/6ed46d9be79c05d951677fe74d13aba575b7eecb))
* update viewtify ([849c0de](https://github.com/teletha/cointoss/commit/849c0de74c80539b6ed78d245a66c10077c9e20e))
* update viewtify ([47ea505](https://github.com/teletha/cointoss/commit/47ea50569ede23aebd2328a69092feba87e59de4))
* update viewtify ([36c4f13](https://github.com/teletha/cointoss/commit/36c4f136b6122d2c76a6f9d85d30114e0526d1a6))
* Websocket api of coincheck is broken. ([3c9a39e](https://github.com/teletha/cointoss/commit/3c9a39e4e64b8535d45d66da5923e582ed08f5f7))

## [1.3.0](https://www.github.com/teletha/cointoss/compare/v1.2.0...v1.3.0) (2022-03-28)


### Features

* Add BTCUSD(T)0930 market on Binance and FTX. ([e90b699](https://www.github.com/teletha/cointoss/commit/e90b699ef605fff2b4e28203c01abc91a8cc31a8))
* Add mono color theme. ([9568af1](https://www.github.com/teletha/cointoss/commit/9568af1cca181e0c7eed21389fdcd1c17bfb1dde))
* Add USDT pair for BNB, FTT, GMT and UNI on Binance. ([dad01e8](https://www.github.com/teletha/cointoss/commit/dad01e8d66936cf8cc4fa436bb57bf8bb1fd0626))
* Add XRP, XLM and MON on bitflyer's spot market. ([56577e5](https://www.github.com/teletha/cointoss/commit/56577e536e62fd24d24d9cffeb5cf152daf8f797))
* Configurable orderbook size. ([5635952](https://www.github.com/teletha/cointoss/commit/563595210eca927f11ed97cde0885e8484dd1a34))
* Drop span for  8 hours. ([4bead2a](https://www.github.com/teletha/cointoss/commit/4bead2af637bb965505959d0f733a90e865fb7c0))
* Drop span for 30 minutes. ([64e2bcb](https://www.github.com/teletha/cointoss/commit/64e2bcb1fa19f550f28a02429d56b0fe8e3773fc))
* Limit orderbook size. ([0f49de4](https://www.github.com/teletha/cointoss/commit/0f49de44c1dbe83601f6984044de7a69b01cdf67))
* Optimize memory and performance for orderbook related system. ([23be455](https://www.github.com/teletha/cointoss/commit/23be4550b22102b4e5f6d0aee84381db8cb4e53a))


### Bug Fixes

* Bitflyer's session maintainer was broken. ([108efa3](https://www.github.com/teletha/cointoss/commit/108efa3a92e5242fc7545b46337f781540bbd84e))
* Bybit's orderbook was broken. ([1fb6e0d](https://www.github.com/teletha/cointoss/commit/1fb6e0d140e6c18c766a850400b993dccd3177d6))
* Chart draws the price notifier on the correct position. ([5f22064](https://www.github.com/teletha/cointoss/commit/5f22064fe8aa9f4d3989f3a3b5660e6ebb9b689e))
* Decrease the amount of ticks. ([2badea6](https://www.github.com/teletha/cointoss/commit/2badea69a5a2bc4c9ee93f2ae7554e0e31d49777))
* Display the rounded profit and loss. ([3fad0a6](https://www.github.com/teletha/cointoss/commit/3fad0a6c1adab220788d9e02eef6e99c5c73e97b))
* Get the account id automatically. ([007914c](https://www.github.com/teletha/cointoss/commit/007914c1bdb41a05aaf549f37bab81db0d024607))
* Memory leak on writing log. ([0541732](https://www.github.com/teletha/cointoss/commit/05417328dfa8a435d38c17b896902a996bb802fb))
* Optimize orderbook change. ([65469b6](https://www.github.com/teletha/cointoss/commit/65469b6a7584ea844164a2d44a50ddefad0b2cea))
* Reduce memory usage. ([311a914](https://www.github.com/teletha/cointoss/commit/311a91456c0cafcdd11909265317e8f2bcefb149))
* Reduce orderbook size. ([7458989](https://www.github.com/teletha/cointoss/commit/7458989608668532e264cb2423f241825ef31dfa))
* Reduce the number of target spans on SMA indicator. ([e231796](https://www.github.com/teletha/cointoss/commit/e2317968d49bca4bd0d0cc69e1e8ed6634fd0fa9))
* Reduce update timing on BinanceF's orderbook. ([5546b49](https://www.github.com/teletha/cointoss/commit/5546b49f2f65ac2c4f769ad3e4f9ad8d98512b40))
* Reduce update timing on Bitfinex's orderbook. ([80ecc4d](https://www.github.com/teletha/cointoss/commit/80ecc4dcf500a652e01fca46154462e3c0ce131f))
* Reduce update timing on Coinbase's orderbook. ([73ac5fe](https://www.github.com/teletha/cointoss/commit/73ac5fec8663f2578d8a7c0513fabd47a3eeba02))
* Update Bybit. ([e351f76](https://www.github.com/teletha/cointoss/commit/e351f76977c1dd943bf9d54d67606894b1dc2295))

## [1.2.0](https://www.github.com/teletha/cointoss/compare/v1.1.0...v1.2.0) (2022-03-10)


### Features

* Add 10 sec span. ([d23828f](https://www.github.com/teletha/cointoss/commit/d23828f9a1806b841bbe265bd28e8c494274283b))
* Add buld data supplier on FeatherStore. ([a1aa7c1](https://www.github.com/teletha/cointoss/commit/a1aa7c122461c2554616bbd6cb57008ef3992ad6))
* Add FTX theme. ([f9ae373](https://www.github.com/teletha/cointoss/commit/f9ae3736b2b5a5491c8473f5798636138c720750))
* OrderBookPage uses Num no longer. ([728a795](https://www.github.com/teletha/cointoss/commit/728a79507b53cdd75dadf9d395cb8c3b735cc076))
* Reduce memory usage for grouped order books. ([0fee178](https://www.github.com/teletha/cointoss/commit/0fee178f83ff29fbf20211886f4f08ee3422eeeb))
* Reduce the amount of vram usage hugely. ([247cd8d](https://www.github.com/teletha/cointoss/commit/247cd8d04786b5ddd0a3c82d8b212b298173e9fe))
* Reduce usage of Num in GroupedOrderBook. ([f9c3818](https://www.github.com/teletha/cointoss/commit/f9c3818eca020d2dce9dc35a8e1e69b717145eee))
* Tick uses Num no longer. ([a70c912](https://www.github.com/teletha/cointoss/commit/a70c912e1d6adc0a5b973a35b87f2a925a03ba50))


### Bug Fixes

* Don't draw market info. ([6f37cb5](https://www.github.com/teletha/cointoss/commit/6f37cb51e782b6a2c430f8042625898e4d0f9dd7))
* Make scanning logs more faster. ([023d91d](https://www.github.com/teletha/cointoss/commit/023d91deaa9350dbd8ebcd98802263e21664f4a4))
* Reduce memory usage in OrderBook's range. ([dcff044](https://www.github.com/teletha/cointoss/commit/dcff0447f5cd395c2542e290c4055099ca09d45f))
* Reduce usage of Num in GroupedOrderBook. ([bf3f688](https://www.github.com/teletha/cointoss/commit/bf3f688d9db93a15e3e7fa4515d4a9c2086d9cc4))
* Reduce usage of Num in GroupedOrderBook. ([651a2ad](https://www.github.com/teletha/cointoss/commit/651a2ad02eef787a59fce60c2cc3330a9e10e07d))
* Reduce usage of Num in OrderBookManager. ([5a1102d](https://www.github.com/teletha/cointoss/commit/5a1102d6fccc45094c9b4a6728d49d5e2c4ecb6f))
* Reduce usage of Num in OrderBookPage. ([d305999](https://www.github.com/teletha/cointoss/commit/d305999a99d3228f73e127062e425c69525841c4))
* Y-axis is no longer able to zoom. ([d8019ce](https://www.github.com/teletha/cointoss/commit/d8019cee9d4408131922ce879394fcdcecb30afd))

## [1.1.0](https://www.github.com/teletha/cointoss/compare/v1.0.0...v1.1.0) (2022-02-17)


### Features

* Add BTC/USD(T) markets on FTX. ([dc9108a](https://www.github.com/teletha/cointoss/commit/dc9108a784b1c9230bdb2e5256b50fd211f873f7))
* Chart can scrolls on chart. ([bc17a00](https://www.github.com/teletha/cointoss/commit/bc17a00fc1c92ce4fdf05ac76c4c9a5f02ef790b))
* Chart is draggable. ([fa1107f](https://www.github.com/teletha/cointoss/commit/fa1107f8cda99e0f2a3087bb2b5519062f9ea157))
* Drop wave trend indicator. ([578ab1f](https://www.github.com/teletha/cointoss/commit/578ab1fc01f09106bb4b5a809eb919e2eaf7eeb9))
* Smooth chart scroll. ([4b7ba00](https://www.github.com/teletha/cointoss/commit/4b7ba003ef2dfd994f746dfd2ea397b9e2a359ae))
* Suggest interval value smartly on bot parameter. ([e413c54](https://www.github.com/teletha/cointoss/commit/e413c54dc6bb03bde05d9cbaca1ff4526df58661))


### Bug Fixes

* Accept invalid response on Binance. ([ab94cfc](https://www.github.com/teletha/cointoss/commit/ab94cfc3171ad18e39cc41af6c4186e9631ee73e))
* Accept the invalid response in Bitmex. ([cac7546](https://www.github.com/teletha/cointoss/commit/cac7546c9a48de1fba4de3656fbdf8490206a37c))
* Format console output. ([deca9b4](https://www.github.com/teletha/cointoss/commit/deca9b465214f52d1cc998ae7784a1b327c7e8f7))
* Reduce memory usage. ([4bd6d57](https://www.github.com/teletha/cointoss/commit/4bd6d57689436f8e5980a53624eb67c6c5c683f4))
* Update sinobu. ([f8bf41c](https://www.github.com/teletha/cointoss/commit/f8bf41ca4079852d9b331e3eae45bb33c60ad98b))

## 1.0.0 (2022-02-04)


### Features

* Add new BTCUSD220325 market on BinanceF. ([37f9034](https://www.github.com/teletha/cointoss/commit/37f90340e0a0843edb3cc23ad04220e2c273ef9d))
* Add Num#random. ([a01c64b](https://www.github.com/teletha/cointoss/commit/a01c64bc0b2369299c8172fee359912c61a6b622))
* Drop log4j2 by using sinobu's fast and min logger. ([ce772e3](https://www.github.com/teletha/cointoss/commit/ce772e3ffba678602d3fcd6de633fe8c9a8bc5b5))
* Drop Signal#delay(Supplier<Duration>). ([bc32695](https://www.github.com/teletha/cointoss/commit/bc326951d390ae81cc13d60be6944cc47832cdee))
* Enable CI. ([b897627](https://www.github.com/teletha/cointoss/commit/b897627b6dfe14f8ab44a2f2a39866756d697246))
* New market BTCUSD211231 on BinanceF. ([f99ec0a](https://www.github.com/teletha/cointoss/commit/f99ec0a5ea039edb0306c0cea88e1ac12d351467))
* New market ETH-1231, ETH-0325, BTC-0325 on FTX. ([7e9ce8a](https://www.github.com/teletha/cointoss/commit/7e9ce8a8dc6ef8ab24fde651b73adc935abe9d88))
* update javafx. ([23834c5](https://www.github.com/teletha/cointoss/commit/23834c5811857ae4b2eba0dcd931e75d537fd079))


### Bug Fixes

* Additional plot script is cached for every span. ([8b2929e](https://www.github.com/teletha/cointoss/commit/8b2929ee79ee580ab39c4693327b131b74598a07))
* All logs are corrupt, remove them!!!!!!!! ([80f8617](https://www.github.com/teletha/cointoss/commit/80f8617ee6c863547b121f1bd169eb3ca03be5e8))
* Axis label is not displayed when indicator was end. ([75dcf23](https://www.github.com/teletha/cointoss/commit/75dcf236772109bb8ca32ec2fb508d6b8dc84c0f))
* Bybit execution recording is broken. ([d8a2484](https://www.github.com/teletha/cointoss/commit/d8a2484e8d01d05ed93bcd81a3f72182ebfb0eb8))
* Can't initialize trader when power assert was invoked. ([4f184fa](https://www.github.com/teletha/cointoss/commit/4f184fa1192b7d31cb2217e166cec96998afcbf4))
* Cancel action freeze UI. ([3b176aa](https://www.github.com/teletha/cointoss/commit/3b176aa5b831106e19a5f59df03b038a31b1ad79))
* Chart can't clear info when mouse is out of canvas. ([892b789](https://www.github.com/teletha/cointoss/commit/892b789cf3b948906907cf451e86d06f945bd47f))
* Chart padding is broken. ([8155e28](https://www.github.com/teletha/cointoss/commit/8155e2870f46b9dd86ccb8133b301764fab3c6ee))
* Chart span is not reactive. ([3f84692](https://www.github.com/teletha/cointoss/commit/3f8469242db3d35c799192767fb97c2b824f9f6a))
* Coinbase's websocket try to connect repeatedly. ([8b4f1c4](https://www.github.com/teletha/cointoss/commit/8b4f1c4f944a2abd96a78778f65a028ab3b622fb))
* Decrease retry time. ([309ded0](https://www.github.com/teletha/cointoss/commit/309ded094a3eeda96492274d842b6f2dc521cb87))
* Division by zero. ([fd30253](https://www.github.com/teletha/cointoss/commit/fd302534977c5917fb3227d0a6a5412dac2a509c))
* Drop big-math. ([1c12a26](https://www.github.com/teletha/cointoss/commit/1c12a2610a98b9bf3cb699ec3a3a35c7645677fa))
* Drop commons-math3. ([b275191](https://www.github.com/teletha/cointoss/commit/b275191f44d112788c63e1a367846697347a9d99))
* EfficientWebSocket can't restore connection on error. ([2160b7a](https://www.github.com/teletha/cointoss/commit/2160b7a8459fef0f58e923c516bb917e768ece09))
* ExecutionLog is broken. ([ad9c3d2](https://www.github.com/teletha/cointoss/commit/ad9c3d2b58c78f788de9b9da7c82bdf1e8572ea7))
* Fail to draw chart when start or end time is null. ([b1e84da](https://www.github.com/teletha/cointoss/commit/b1e84da93762b48f5ed28b02284240117ae124f0))
* Fatal runtime error in ChartView. ([030795c](https://www.github.com/teletha/cointoss/commit/030795c42523c64b3cf082420f94c626c30a0d39))
* Javac can compile primitive collection. ([320a022](https://www.github.com/teletha/cointoss/commit/320a022e23417d5b4207fb05a13ef583acbafe07))
* Javac can compile sources. ([1e183eb](https://www.github.com/teletha/cointoss/commit/1e183eb4b56baf93248eb8ac6d91609a8eda2c77))
* LogWriter throws NPE. ([bf27f9c](https://www.github.com/teletha/cointoss/commit/bf27f9ca2244eb2677c89243b3903ab0465cfcf0))
* Make more margin so as not to violate API limit restrictions. ([4171e97](https://www.github.com/teletha/cointoss/commit/4171e973621fccf203103607bad123d755a46871))
* Memory Leak. ([1746b3f](https://www.github.com/teletha/cointoss/commit/1746b3fa04ce1c34edb93ddcb31509deb3d7bb58))
* OrderBook often throws IndexOutOfBoundsException. ([474f8c2](https://www.github.com/teletha/cointoss/commit/474f8c2d60ef36b09db0c2cd4c3c2a403c108946))
* OrderBook often throws IndexOutOfBoundsException. ([744def8](https://www.github.com/teletha/cointoss/commit/744def87b4d89467b32624a459757fd9b3afc3cf))
* Pass test on CI. ([c40fb33](https://www.github.com/teletha/cointoss/commit/c40fb33171f3e1454f579a7ca633f0b09cecfdcc))
* Profitable is broken in complex scenario. ([63bdd63](https://www.github.com/teletha/cointoss/commit/63bdd638a111f5eac9c2ac986175a8c3ab5eee60))
* ReDrawing market name on chart. ([f7a9adf](https://www.github.com/teletha/cointoss/commit/f7a9adf021f0cec6d894d2d8dc236dfd25010a84))
* Reduce drawing chart while scrolling. ([1cbbbe5](https://www.github.com/teletha/cointoss/commit/1cbbbe5c994e34b9304b2f6b68b41d36fc4b226f))
* SegmentBuffer index error. ([0a88646](https://www.github.com/teletha/cointoss/commit/0a88646fa3e0611d198d56bd7e090cf5cca78f12))
* Span segment size is too small. ([512bc1b](https://www.github.com/teletha/cointoss/commit/512bc1b825138474b23076acd8d2182123dfbe89))
* Support new Bybit archived log. ([2ce0eb6](https://www.github.com/teletha/cointoss/commit/2ce0eb6c44d1c563cf62a37afd65f2b1d42694a5))
* Taker order has high priority than maker order. ([1fb4e9d](https://www.github.com/teletha/cointoss/commit/1fb4e9d7c997651c6f91251000e04e6c067b56f2))
* The orderbook on chart appears in a non-intuitive location. ([f83ae58](https://www.github.com/teletha/cointoss/commit/f83ae58540c74bb2a59221edbe8d0217a0f2503a))
* typo ([b7d81b8](https://www.github.com/teletha/cointoss/commit/b7d81b8d8b0266b12cbba933b5b5fe27ddaa8931))
* Update future markets in Binance and FTX. ([59a7857](https://www.github.com/teletha/cointoss/commit/59a785761cb486a10010b8c89065f174b7c03094))
* update icy-manipulator ([b05ebe4](https://www.github.com/teletha/cointoss/commit/b05ebe4c00c17519f26a3957c5b62970d7e43ec5))
* Websocket can't retry when connecting error. ([fd5306b](https://www.github.com/teletha/cointoss/commit/fd5306b655e6314504eab6aa7240e1601b0cac68))
