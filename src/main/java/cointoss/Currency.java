/*
 * Copyright (C) 2020 cointoss Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package cointoss;

import java.util.Set;

import cointoss.util.Num;
import kiss.I;

/**
 * A Currency class roughly modeled after {@link java.util.Currency}. Each object retains the code
 * it was acquired with -- so {@link #getInstance}("BTC").{@link #getCurrencyCode}() will always be
 * "BTC", even though the proposed ISO 4217 code is "XBT"
 */
public enum Currency {

    AED("AED", "United Arab Emirates Dirham"),

    AFN("AFN", "Afghan Afghani"),

    ALL("ALL", "Albanian Lek"),

    AMD("AMD", "Armenian Dram"),

    ANC("ANC", "Anoncoin"),

    ANG("ANG", "Netherlands Antillean Guilder"),

    AOA("AOA", "Angolan Kwanza"),

    ARN("ARN", "Aeron"),

    ARS("ARS", "Argentine Peso"),

    ATOM("ATOM", "Cosmos"),

    AUD("AUD", "Australian Dollar"),

    AUR("AUR", "Auroracoin"),

    AVT("AVT", "Aventus"),

    AWG("AWG", "Aruban Florin"),

    AZN("AZN", "Azerbaijani Manat"),

    BAM("BAM", "Bosnia-Herzegovina Convertible Mark"),

    BAT("BAT", "Basic Attention Token"),

    BBD("BBD", "Barbadian Dollar"),

    BC("BC", "BlackCoin", "BLK"),

    BCC("BCC", "BitConnect"),

    BCH("BCH", "BitcoinCash"),

    BCA("BCA", "BitcoinAtom"),

    BDT("BDT", "Bangladeshi Taka"),

    BGC("BGC", "Aten 'Black Gold' Coin"),

    BGN("BGN", "Bulgarian Lev"),

    BHD("BHD", "Bahraini Dinar"),

    BIF("BIF", "Burundian Franc"),

    BMD("BMD", "Bermudan Dollar"),

    BND("BND", "Brunei Dollar"),

    BOB("BOB", "Bolivian Boliviano"),

    BRL("BRL", "Brazilian Real"),

    BSD("BSD", "Bahamian Dollar"),

    BTC("BTC", "Bitcoin", "XBT"),

    BTG("BTG", "Bitcoin Gold"),

    BTN("BTN", "Bhutanese Ngultrum"),

    BWP("BWP", "Botswanan Pula"),

    BYR("BYR", "Belarusian Ruble"),

    BZD("BZD", "Belize Dollar"),

    CAD("CAD", "Canadian Dollar"),

    CDF("CDF", "Congolese Franc"),

    CHF("CHF", "Swiss Franc"),

    CLF("CLF", "Chilean Unit of Account (UF)"),

    CLP("CLP", "Chilean Peso"),

    CNC("CNC", "Chinacoin"),

    CNY("CNY", "Chinese Yuan"),

    COP("COP", "Colombian Peso"),

    CRC("CRC", "Costa Rican Colón"),

    CUP("CUP", "Cuban Peso"),

    CVE("CVE", "Cape Verdean Escudo"),

    CZK("CZK", "Czech Republic Koruna"),

    DASH("DASH", "Dash"),

    DCR("DCR", "Decred"),

    DGB("DGB", "DigiByte"),

    DJF("DJF", "Djiboutian Franc"),

    DKK("DKK", "Danish Krone"),

    DOGE("DOGE", "Dogecoin", "XDC", "XDG"),

    DOP("DOP", "Dominican Peso"),

    DGC("DGC", "Digitalcoin"),

    DVC("DVC", "Devcoin"),

    DRK("DRK", "Darkcoin"),

    DZD("DZD", "Algerian Dinar"),

    EDO("EDO", "Eidoo"),

    EEK("EEK", "Estonian Kroon"),

    EGD("EGD", "egoldcoin"),

    EGP("EGP", "Egyptian Pound"),

    EOS("EOS", "EOS"),

    ETB("ETB", "Ethiopian Birr"),

    ETC("ETC", "Ether Classic"),

    ETH("ETH", "Ether"),

    EUR("EUR", "Euro"),

    FJD("FJD", "Fijian Dollar"),

    _1ST("1ST", "First Blood"),

    FKP("FKP", "Falkland Islands Pound"),

    FTC("FTC", "Feathercoin"),

    GBP("GBP", "British Pound Sterling"),

    GEL("GEL", "Georgian Lari"),

    GHS("GHS", "Ghanaian Cedi"),

    GHs("GHS", "Gigahashes per second"),

    GIP("GIP", "Gibraltar Pound"),

    GMD("GMD", "Gambian Dalasi"),

    GNF("GNF", "Guinean Franc"),

    GNO("GNO", "Gnosis"),

    GNT("GNT", "Golem"),

    GTQ("GTQ", "Guatemalan Quetzal"),

    GVT("GVT", "Genesis Vision"),

    GYD("GYD", "Guyanaese Dollar"),

    HKD("HKD", "Hong Kong Dollar"),

    HVN("HVN", "Hive"),

    HNL("HNL", "Honduran Lempira"),

    HRK("HRK", "Croatian Kuna"),

    HTG("HTG", "Haitian Gourde"),

    HUF("HUF", "Hungarian Forint"),

    ICN("ICN", "Iconomi"),

    IDR("IDR", "Indonesian Rupiah"),

    ILS("ILS", "Israeli New Sheqel"),

    INR("INR", "Indian Rupee"),

    IOC("IOC", "I/OCoin"),

    IOT("IOT", "IOTA"),

    IQD("IQD", "Iraqi Dinar"),

    IRR("IRR", "Iranian Rial"),

    ISK("ISK", "Icelandic Króna"),

    IXC("IXC", "iXcoin"),

    JEP("JEP", "Jersey Pound"),

    JMD("JMD", "Jamaican Dollar"),

    JOD("JOD", "Jordanian Dinar"),

    JPY("JPY", "Japanese Yen"),

    KES("KES", "Kenyan Shilling"),

    KGS("KGS", "Kyrgystani Som"),

    KHR("KHR", "Cambodian Riel"),

    KICK("KICK", "KickCoin"),

    KMF("KMF", "Comorian Franc"),

    KPW("KPW", "North Korean Won"),

    KRW("KRW", "South Korean Won"),

    KWD("KWD", "Kuwaiti Dinar"),

    KYD("KYD", "Cayman Islands Dollar"),

    KZT("KZT", "Kazakhstani Tenge"),

    LAK("LAK", "Laotian Kip"),

    LBP("LBP", "Lebanese Pound"),

    LSK("LSK", "Lisk"),

    LKR("LKR", "Sri Lankan Rupee"),

    LRD("LRD", "Liberian Dollar"),

    LSL("LSL", "Lesotho Loti"),

    LTC("LTC", "Litecoin", "XLT"),

    LTL("LTL", "Lithuanian Litas"),

    LVL("LVL", "Latvian Lats"),

    LYD("LYD", "Libyan Dinar"),

    MAD("MAD", "Moroccan Dirham"),

    MDL("MDL", "Moldovan Leu"),

    MEC("MEC", "MegaCoin"),

    MGA("MGA", "Malagasy Ariary"),

    MKD("MKD", "Macedonian Denar"),

    MLN("MLN", "Melonport"),

    MMK("MMK", "Myanma Kyat"),

    MNT("MNT", "Mongolian Tugrik"),

    MOP("MOP", "Macanese Pataca"),

    MRO("MRO", "Mauritanian Ouguiya"),

    MSC("MSC", "Mason Coin"),

    MUR("MUR", "Mauritian Rupee"),

    MVR("MVR", "Maldivian Rufiyaa"),

    MWK("MWK", "Malawian Kwacha"),

    MXN("MXN", "Mexican Peso"),

    MYR("MYR", "Malaysian Ringgit"),

    MZN("MZN", "Mozambican Metical"),

    NAD("NAD", "Namibian Dollar"),

    NOBS("NOBS", "No BS Crypto"),

    NEO("NEO", "NEO"),

    NGN("NGN", "Nigerian Naira"),

    NIO("NIO", "Nicaraguan Córdoba"),

    NMC("NMC", "Namecoin"),

    NOK("NOK", "Norwegian Krone"),

    NPR("NPR", "Nepalese Rupee"),

    NVC("NVC", "Novacoin"),

    NXT("NXT", "Nextcoin"),

    NZD("NZD", "New Zealand Dollar"),

    OMG("OMG", "OmiseGO"),

    OMR("OMR", "Omani Rial"),

    PAB("PAB", "Panamanian Balboa"),

    PEN("PEN", "Peruvian Nuevo Sol"),

    PGK("PGK", "Papua New Guinean Kina"),

    PHP("PHP", "Philippine Peso"),

    PKR("PKR", "Pakistani Rupee"),

    PLN("PLN", "Polish Zloty"),

    POT("POT", "PotCoin"),

    PPC("PPC", "Peercoin"),

    PYG("PYG", "Paraguayan Guarani"),

    QAR("QAR", "Qatari Rial"),

    QRK("QRK", "QuarkCoin"),

    QTUM("QTUM", "Qtum"),

    REP("REP", "Augur"),

    RON("RON", "Romanian Leu"),

    RSD("RSD", "Serbian Dinar"),

    RUB("RUB", "Russian Ruble"),

    RUR("RUR", "Old Russian Ruble"),

    RWF("RWF", "Rwandan Franc"),

    SAR("SAR", "Saudi Riyal"),

    SBC("SBC", "Stablecoin"),

    SBD("SBD", "Solomon Islands Dollar"),

    SC("SC", "Siacoin"),

    SCR("SCR", "Seychellois Rupee"),

    SDG("SDG", "Sudanese Pound"),

    SEK("SEK", "Swedish Krona"),

    SGD("SGD", "Singapore Dollar"),

    SHP("SHP", "Saint Helena Pound"),

    SLL("SLL", "Sierra Leonean Leone"),

    SMART("SMART", "SmartCash"),

    SOS("SOS", "Somali Shilling"),

    SRD("SRD", "Surinamese Dollar"),

    START("START", "startcoin"),

    STEEM("STEEM", "Steem"),

    STD("STD", "São Tomé and Príncipe Dobra"),

    STR("STR", "Stellar"),

    STRAT("STRAT", "Stratis"),

    SVC("SVC", "Salvadoran Colón"),

    SYP("SYP", "Syrian Pound"),

    SZL("SZL", "Swazi Lilangeni"),

    THB("THB", "Thai Baht"),

    TJS("TJS", "Tajikistani Somoni"),

    TMT("TMT", "Turkmenistani Manat"),

    TND("TND", "Tunisian Dinar"),

    TOP("TOP", "Tongan Paʻanga"),

    TRC("TRC", "Terracoin"),

    TRY("TRY", "Turkish Lira"),

    TTD("TTD", "Trinidad and Tobago Dollar"),

    TWD("TWD", "New Taiwan Dollar"),

    TZS("TZS", "Tanzanian Shilling"),

    UAH("UAH", "Ukrainian Hryvnia"),

    UGX("UGX", "Ugandan Shilling"),

    USD("USD", "United States Dollar"),

    USDC("USDC", "USD Coin", "UDC"),

    USDT("USDT", "Tether USD Anchor"),

    USDE("USDE", "Unitary Status Dollar eCoin"),

    UTC("UTC", "Ultracoin"),

    UYU("UYU", "Uruguayan Peso"),

    UZS("UZS", "Uzbekistan Som"),

    VEF("VEF", "Venezuelan Bolívar"),

    VET("VET", "Hub Culture's Vet", "VEN"),

    VEN("VEN", "Hub Culture's Ven", "XVN"),

    XTZ("XTZ", "Tezos"),

    VIB("VIB", "Viberate"),

    VND("VND", "Vietnamese Dong"),

    VUV("VUV", "Vanuatu Vatu"),

    WDC("WDC", "WorldCoin"),

    WST("WST", "Samoan Tala"),

    XAF("XAF", "CFA Franc BEAC"),

    XAS("XAS", "Asch"),

    XAUR("XAUR", "Xaurum"),

    XCD("XCD", "East Caribbean Dollar"),

    XDR("XDR", "Special Drawing Rights"),

    XEM("XEM", "NEM"),

    XLM("XLM", "Stellar Lumen"),

    XMR("XMR", "Monero"),

    XRB("XRB", "Rai Blocks"),

    XOF("XOF", "CFA Franc BCEAO"),

    XPF("XPF", "CFP Franc"),

    XPM("XPM", "Primecoin"),

    XRP("XRP", "Ripple"),

    YBC("YBC", "YbCoin"),

    YER("YER", "Yemeni Rial"),

    ZAR("ZAR", "South African Rand"),

    ZEC("ZEC", "Zcash"),

    ZEN("ZEN", "ZenCash"),

    ZMW("ZMW", "Zambian Kwacha", "ZMK"),

    ZRC("ZRC", "ziftrCOIN"),

    ZWL("ZWL", "Zimbabwean Dollar"),

    // Cryptos
    BNK("BNK", "Bankera Coin"),

    BNB("BNB", "Binance Coin"),

    QSP("QSP", "Quantstamp"),

    IOTA("IOTA", "Iota"),

    YOYO("YOYO", "Yoyow"),

    BTS("BTS", "Bitshare"),

    ICX("ICX", "Icon"),

    MCO("MCO", "Monaco"),

    CND("CND", "Cindicator"),

    XVG("XVG", "Verge"),

    POE("POE", "Po.et"),

    TRX("TRX", "Tron"),

    ADA("ADA", "Cardano"),

    FUN("FUN", "FunFair"),

    HSR("HSR", "Hshare"),

    LEND("LEND", "ETHLend"),

    ELF("ELF", "aelf"),

    STORJ("STORJ", "Storj"),

    MOD("MOD", "Modum"),

    // Coinmarketcap top 200
    AE("AE", "Aeternity"),

    FET("FET", "Fetch.ai"),

    BHT("BHT", "BHEX Token"),

    SNX("SNX", "Synthetix Network Token"),

    PNT("PNT", "pNetwork"),

    WIN("WIN", "WINk"),

    ANT("ANT", "Aragon"),

    DX("DX", "DxChain Token"),

    ZB("ZB", "ZB Token"),

    LINK("LINK", "Chainlink"),

    BTT("BTT", "BitTorrent"),

    AVA("AVA", "Travala.com"),

    SYS("SYS", "Syscoin"),

    BNT("BNT", "Bancor"),

    ERD("ERD", "Elrond"),

    SNT("SNT", "Status"),

    ONE("ONE", "Harmony"),

    HPT("HPT", "Huobi Pool Token"),

    NEXO("NEXO", "Nexo"),

    FXC("FXC", "Flexacoin"),

    TOMO("TOMO", "TomoChain"),

    OGN("OGN", "Origin Protocol"),

    NPXS("NPXS", "Pundi X"),

    MIOTA("MIOTA", "IOTA"),

    HEDG("HEDG", "HedgeTrade"),

    HYN("HYN", "Hyperion"),

    DIVI("DIVI", "Divi"),

    AION("AION", "Aion"),

    CRO("CRO", "Crypto.com Coin"),

    ARK("ARK", "Ark"),

    PERL("PERL", "Perlin"),

    HT("HT", "Huobi Token"),

    FSN("FSN", "Fusion"),

    LUNA("LUNA", "Terra"),

    MAID("MAID", "MaidSafeCoin"),

    TFUEL("TFUEL", "Theta Fuel"),

    RLC("RLC", "iExec RLC"),

    DRGN("DRGN", "Dragonchain"),

    LEO("LEO", "UNUS SED LEO"),

    TUSD("TUSD", "TrueUSD"),

    KSM("KSM", "Kusama"),

    MKR("MKR", "Maker"),

    PNK("PNK", "Kleros"),

    UBT("UBT", "Unibright"),

    GRIN("GRIN", "Grin"),

    IPX("IPX", "Tachyon Protocol"),

    MANA("MANA", "Decentraland"),

    CEL("CEL", "Celsius"),

    BAND("BAND", "Band Protocol"),

    BCD("BCD", "Bitcoin Diamond"),

    NRG("NRG", "Energi"),

    LOKI("LOKI", "Loki"),

    OCEAN("OCEAN", "Ocean Protocol"),

    WAN("WAN", "Wanchain"),

    MATIC("MATIC", "Matic Network"),

    POWR("POWR", "Power Ledger"),

    MX("MX", "MX Token"),

    IRIS("IRIS", "IRISnet"),

    ANKR("ANKR", "Ankr"),

    DAI("DAI", "Dai"),

    SEELE("SEELE", "Seele-N"),

    WXT("WXT", "Wirex Token"),

    RVN("RVN", "Ravencoin"),

    KCS("KCS", "KuCoin Shares"),

    SERO("SERO", "Super Zero Protocol"),

    XNS("XNS", "Insolar"),

    ALGO("ALGO", "Algorand"),

    REN("REN", "Ren"),

    WRX("WRX", "WazirX"),

    KMD("KMD", "Komodo"),

    RSR("RSR", "Reserve Rights"),

    MOF("MOF", "Molecular Future"),

    RCN("RCN", "Ripio Credit Network"),

    RDD("RDD", "ReddCoin"),

    LRC("LRC", "Loopring"),

    KAVA("KAVA", "Kava"),

    FTM("FTM", "Fantom"),

    VLX("VLX", "Velas"),

    ENG("ENG", "Enigma"),

    UTK("UTK", "Utrust"),

    ZIL("ZIL", "Zilliqa"),

    TRAC("TRAC", "OriginTrail"),

    MTL("MTL", "Metal"),

    WAVES("WAVES", "Waves"),

    DGTX("DGTX", "Digitex Futures"),

    QNT("QNT", "Quant"),

    SOL("SOL", "Solana"),

    XHV("XHV", "Haven Protocol"),

    AMPL("AMPL", "Ampleforth"),

    ELA("ELA", "Elastos"),

    VTHO("VTHO", "VeThor Token"),

    PAXG("PAXG", "PAX Gold"),

    MONA("MONA", "MonaCoin"),

    CHSB("CHSB", "SwissBorg"),

    ENJ("ENJ", "Enjin Coin"),

    PAI("PAI", "Project Pai"),

    TRUE("TRUE", "TrueChain"),

    ARDR("ARDR", "Ardor"),

    BTM("BTM", "Bytom"),

    STX("STX", "Blockstack"),

    XDCE("XDCE", "XinFin Network"),

    ETN("ETN", "Electroneum"),

    CHZ("CHZ", "Chiliz"),

    CTXC("CTXC", "Cortex"),

    GT("GT", "Gatechain Token"),

    FTT("FTT", "FTX Token"),

    YFI("YFI", "Yearn.Finance Token"),

    CVT("CVT", "CyberVein"),

    WTC("WTC", "Waltonchain"),

    ORBS("ORBS", "Orbs"),

    HIVE("HIVE", "Hive"),

    BSV("BSV", "Bitcoin SV"),

    PAX("PAX", "Paxos Standard"),

    GXC("GXC", "GXChain"),

    KNC("KNC", "Kyber Network"),

    BUSD("BUSD", "Binance USD"),

    CHR("CHR", "Chromia"),

    HC("HC", "HyperCash"),

    TT("TT", "Thunder Token"),

    EURS("EURS", "STASIS EURO"),

    HBAR("HBAR", "Hedera Hashgraph"),

    ONT("ONT", "Ontology"),

    VGX("VGX", "Voyager Token"),

    HOT("HOT", "Holo"),

    XZC("XZC", "Zcoin"),

    CELR("CELR", "Celer Network"),

    CKB("CKB", "Nervos Network"),

    WICC("WICC", "WaykiChain"),

    WAXP("WAXP", "WAX"),

    BEAM("BEAM", "Beam"),

    SXP("SXP", "Swipe"),

    IOTX("IOTX", "IoTeX"),

    VSYS("VSYS", "v.systems"),

    DATA("DATA", "Streamr"),

    NIM("NIM", "Nimiq"),

    REQ("REQ", "Request"),

    RIF("RIF", "RSK Infrastructure Framework"),

    COMP("COMP", "Compound"),

    TMTG("TMTG", "The Midas Touch Gold"),

    ABBC("ABBC", "ABBC Coin"),

    NAS("NAS", "Nebulas"),

    NANO("NANO", "Nano"),

    NMR("NMR", "Numeraire"),

    CRPT("CRPT", "Crypterium"),

    DAD("DAD", "DAD"),

    MXC("MXC", "MXC"),

    TSHP("TSHP", "12Ships"),

    IOST("IOST", "IOST"),

    THETA("THETA", "THETA"),

    HUSD("HUSD", "HUSD"),

    COTI("COTI", "COTI"),

    PIVX("PIVX", "PIVX"),

    NULS("NULS", "NULS"),

    SOLVE("SOLVE", "SOLVE"),

    OKB("OKB", "OKB"),

    ZRX("ZRX", "0x"),

    // ===================================================================
    // Derivatives on FTX
    // ===================================================================
    FTX_DEFI("DEFI", "Buscket for Defi project tokens on FTX"),

    FTX_ALT("ALT", "Buscket for major alt coins on FTX"),

    FTX_MID("MID", "Buscket for middle alt coins on FTX"),

    FTX_SHIT("SHIT", "Buscket for minor alt coins on FTX"),

    // ===================================================================
    // For Test
    // ===================================================================
    UNKNOWN("", "Unknown Currency for test");

    /** The commonly used identical currency code. */
    public final String code;

    /** The immutable set of currency codes. */
    public final Set<String> codes;

    /** The human readable name. */
    public final String name;

    /**
     * Create {@link Currency}.
     * 
     * @param code
     * @param name
     * @param alternativeCodes
     */
    private Currency(String code, String name, String... alternativeCodes) {
        this.name = name;
        this.code = code;
        this.codes = Set.of(I.array(alternativeCodes, code));
    }

    /**
     * Build {@link CurrencySetting} with minimum size.
     * 
     * @param minimumSize
     * @return
     */
    public CurrencySetting minimumSize(double minimumSize) {
        Num min = Num.of(minimumSize);
        return new CurrencySetting(this, min, min.scale());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return code;
    }
}