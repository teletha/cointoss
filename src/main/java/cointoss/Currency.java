/*
 * Copyright (C) 2024 The COINTOSS Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.util.Set;

import hypatia.Num;
import kiss.I;

public enum Currency {

    AAVE("Aave"),

    AED("United Arab Emirates Dirham"),

    AFN("Afghan Afghani"),

    AI16Z("AI16Z"),

    ALL("Albanian Lek"),

    AMD("Armenian Dram"),

    ANC("Anoncoin"),

    ANG("Netherlands Antillean Guilder"),

    AOA("Angolan Kwanza"),

    APEX("ApeX"),

    ARB("Arbitrum"),

    ARN("Aeron"),

    ARS("Argentine Peso"),

    ASTR("Astar"),

    ATOM("Cosmos"),

    AUD("Australian Dollar"),

    AUR("Auroracoin"),

    AVAX("Avalanche"),

    AVT("Aventus"),

    AWG("Aruban Florin"),

    AZN("Azerbaijani Manat"),

    BAM("Bosnia-Herzegovina Convertible Mark"),

    BAND("Band Protocol"),

    BAT("Basic Attention Token"),

    BBD("Barbadian Dollar"),

    BC("BlackCoin", "BLK"),

    BCA("BitcoinAtom"),

    BCC("BitConnect"),

    BCH("BitcoinCash"),

    BERA("Berachain"),

    BIF("Burundian Franc"),

    BMD("Bermudan Dollar"),

    BNB("Binance Coin"),

    BND("Brunei Dollar"),

    BTC("Bitcoin", "XBT"),

    BTG("Bitcoin Gold"),

    BTN("Bhutanese Ngultrum"),

    BZD("Belize Dollar"),

    CAD("Canadian Dollar"),

    CAKE("PancakeSwap"),

    CHF("Swiss Franc"),

    CNY("Chinese Yuan"),

    COMP("Compound"),

    COP("Colombian Peso"),

    COTI("COTI"),

    CRC("Costa Rican Colón"),

    CRO("Crypto.com Coin"),

    CRPT("Crypterium"),

    CTXC("Cortex"),

    CUP("Cuban Peso"),

    CVE("Cape Verdean Escudo"),

    CVT("CyberVein"),

    CZK("Czech Republic Koruna"),

    DAD("DAD"),

    DAI("Dai"),

    DASH("Dash"),

    DATA("Streamr"),

    DCR("Decred"),

    DGB("DigiByte"),

    DGC("Digitalcoin"),

    DGTX("Digitex Futures"),

    DJF("Djiboutian Franc"),

    DKK("Danish Krone"),

    DOGE("Dogecoin", "XDC", "XDG"),

    DOP("Dominican Peso"),

    DRGN("Dragonchain"),

    DRK("Darkcoin"),

    DZD("Algerian Dinar"),

    EDO("Eidoo"),

    EEK("Estonian Kroon"),

    EGD("egoldcoin"),

    EGP("Egyptian Pound"),

    ELA("Elastos"),

    ELF("aelf"),

    ENG("Enigma"),

    ENJ("Enjin Coin"),

    EOS("EOS"),

    ERD("Elrond"),

    ETC("Ether Classic"),

    ETH("Ether"),

    EUR("Euro"),

    FARTCOIN("FARTCOIN"),

    FIL("Filecoin"),

    FJD("Fijian Dollar"),

    FKP("Falkland Islands Pound"),

    FTC("Feathercoin"),

    FTM("Fantom"),

    FTT("FTX Token"),

    GBP("British Pound Sterling"),

    GLMR("Glimmer"),

    GMT("STEPN Governance Token"),

    GYD("Guyanaese Dollar"),

    HKD("Hong Kong Dollar"),

    HPT("Huobi Pool Token"),

    HT("Huobi Token"),

    HUSD("HUSD"),

    HYPE("Hyperliquid Token"),

    ILS("Israeli New Sheqel"),

    INR("Indian Rupee"),

    IOT("IOTA"),

    IRR("Iranian Rial"),

    JPY("Japanese Yen"),

    KAVA("Kava"),

    KCS("KuCoin Shares"),

    KICK("KickCoin"),

    KPW("North Korean Won"),

    KRW("South Korean Won"),

    KWD("Kuwaiti Dinar"),

    KYD("Cayman Islands Dollar"),

    LBP("Lebanese Pound"),

    LDO("Lodo DAO Token"),

    LEND("ETHLend"),

    LEO("UNUS SED LEO"),

    LINK("Chainlink"),

    LKR("Sri Lankan Rupee"),

    LOKI("Loki"),

    LRC("Loopring"),

    LRD("Liberian Dollar"),

    LSL("Lesotho Loti"),

    LSK("Lisk"),

    LTC("Litecoin", "XLT"),

    LTL("Lithuanian Litas"),

    LUNA("Terra"),

    MANA("Decentraland"),

    MATIC("Polygon"),

    MCO("Monaco"),

    MGA("Malagasy Ariary"),

    MNT("Mantle"),

    MONA("MonaCoin"),

    MOP("Macanese Pataca"),

    MRO("Mauritanian Ouguiya"),

    MUR("Mauritian Rupee"),

    MX("MX Token"),

    MXC("MXC"),

    MXN("Mexican Peso"),

    MYR("Malaysian Ringgit"),

    MZN("Mozambican Metical"),

    NAD("Namibian Dollar"),

    NANO("Nano"),

    NAS("Nebulas"),

    NEO("NEO"),

    NZD("New Zealand Dollar"),

    OMG("OmiseGO"),

    ONE("Harmony"),

    OP("Optimism"),

    ORBS("Orbs"),

    SBD("Solomon Islands Dollar"),

    SNX("Synthetix Network Token"),

    SOL("Solana"),

    SOLVE("SOLVE"),

    SOLV("Solve Protocol"),

    SRM("Serum"),

    STR("Stellar"),

    STRAT("Stratis"),

    STRK("Starknet Token"),

    STX("Blockstack"),

    SUI("SUI"),

    THB("Thai Baht"),

    THETA("THETA"),

    TJS("Tajikistani Somoni"),

    TMT("Turkmenistani Manat"),

    TND("Tunisian Dinar"),

    TRUMP("Officail Trump"),

    TRY("Turkish Lira"),

    TRX("Tron"),

    TUSD("TrueUSD"),

    TWD("New Taiwan Dollar"),

    TZS("Tanzanian Shilling"),

    UAH("Ukrainian Hryvnia"),

    UBT("Unibright"),

    UGX("Ugandan Shilling"),

    UNI("Uniswap"),

    USD("United States Dollar"),

    USDC("USD Coin", "UDC"),

    USDE("Unitary Status Dollar eCoin"),

    USDT("Tether USD Anchor"),

    UTC("Ultracoin"),

    UTK("Utrust"),

    UYU("Uruguayan Peso"),

    UZS("Uzbekistan Som"),

    VEF("Venezuelan Bolívar"),

    VEN("Hub Culture's Ven", "XVN"),

    VET("Hub Culture's Vet", "VEN"),

    WAVES("Waves"),

    WIF("WIF"),

    WLD("Worldcoin"),

    XLM("Stellar Lumen"),

    XMR("Monero"),

    XRP("Ripple"),

    YFI("Yearn.Finance Token"),

    ZAR("South African Rand"),

    ZEC("Zcash"),

    ZEN("ZenCash"),

    ZMW("Zambian Kwacha", "ZMK"),

    ZRC("ziftrCOIN"),

    ZRX("0x"),

    ZWL("Zimbabwean Dollar"),

    UNKNOWN("Unknown Currency for test");

    /** The commonly used identical currency code. */
    public final String code;

    /** The immutable set of currency codes. */
    public final Set<String> codes;

    /** The human readable name. */
    public final String name;

    /**
     * Create {@link Currency}.
     *
     * @param name
     * @param alternativeCodes
     */
    private Currency(String name, String... alternativeCodes) {
        this.name = name;
        this.code = name();
        this.codes = Set.of(I.array(alternativeCodes, code));
    }

    /**
     * Build {@link CurrencySetting} with minimum size.
     *
     * @return
     */
    public CurrencySetting minimumSize(double minimum) {
        return new CurrencySetting(this, Num.of(minimum), 0);
    }
}