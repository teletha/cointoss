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

/**
 * @version 2017/08/31 19:10:34
 */
public enum BitTrexType {

    //@formatter:off
    BTC("BTC", false), ETH("ETH", true), LTC("LTC",false), DOGE("DOGE",true), VTC("VTC",true), PPC("PPC",true), FTC("FTC",true), RDD("RDD",true), NXT("NXT",true), DASH("DASH",false), POT("POT",true), BLK("BLK",true), EMC2("EMC2",true), XMY("XMY",true), AUR("AUR",true), UTC("UTC",true), MZC("MZC",true), EFL("EFL",true), GLD("GLD",true), FAIR("FAIR",true), SLR("SLR",true), PTC("PTC",true), GRS("GRS",true), NLG("NLG",true), RBY("RBY",true), XWC("XWC",true), MONA("MONA",true), BITS("BITS",true), OC("OC",true), THC("THC",true), ENRG("ENRG",true), SFR("SFR",true), ERC("ERC",true), NAUT("NAUT",true), VRC("VRC",true), CURE("CURE",true), BLC("BLC",true), XC("XC",true), XDQ("XDQ",true), XBB("XBB",true), HYPER("HYPER",true), CCN("CCN",true), XMR("XMR",false), CLOAK("CLOAK",true), BSD("BSD",true), CRYPT("CRYPT",true), START("START",true), KORE("KORE",true), XDN("XDN",true), TRK("TRK",true), TRUST("TRUST",true), NAV("NAV",true), XST("XST",true), APEX("APEX",true), BTCD("BTCD",true), VIA("VIA",true), TRI("TRI",true), UNO("UNO",true), PINK("PINK",true), IOC("IOC",true), MAX("MAX",true), LXC("LXC",true), BOB("BOB",true), CANN("CANN",true), FC2("FC2",true), SSD("SSD",true), J("J",true), SYS("SYS",true), NEOS("NEOS",true), DGB("DGB",false), ROOT("ROOT",true), BTS("BTS",false), BURST("BURST",true), TIT("TIT",true), BSTY("BSTY",true), PXI("PXI",true), DGC("DGC",true), SLG("SLG",true), STV("STV",true), EXCL("EXCL",true), SWIFT("SWIFT",true), NET("NET",true), GHC("GHC",true), DOPE("DOPE",true), BLOCK("BLOCK",true), ABY("ABY",true), VIOR("VIOR",true), BYC("BYC",true), UFO("UFO",true), XMG("XMG",true), XQN("XQN",true), BLITZ("BLITZ",true), VPN("VPN",true), BAY("BAY",true), DTC("DTC",true), AM("AM",true), METAL("METAL",true), SPR("SPR",true), VTR("VTR",true), XPY("XPY",true), XRP("XRP",false), GAME("GAME",true), GP("GP",true), NXS("NXS",true), COVAL("COVAL",true), FSC2("FSC2",true), SOON("SOON",true), HZ("HZ",true), XCP("XCP",true), BITB("BITB",true), XTC("XTC",true), XVG("XVG",true), GEO("GEO",true), FLDC("FLDC",true), GEMZ("GEMZ",true), GRC("GRC",true), XCO("XCO",true), MTR("MTR",true), FLO("FLO",true), U("U",true), NBT("NBT",true), XEM("XEM",false), MUE("MUE",true), XVC("XVC",true), BIT("8BIT",true), CLAM("CLAM",true), XSEED("XSEED",true), NTRN("NTRN",true), SLING("SLING",true), DMD("DMD",true), GAM("GAM",true), UNIT("UNIT",true), GRT("GRT",true), VIRAL("VIRAL",true), SPHR("SPHR",true), ARB("ARB",true), OK("OK",true), ADC("ADC",true), SNRG("SNRG",true), PKB("PKB",true), TES("TES",true), CPC("CPC",true), AEON("AEON",true), BITZ("BITZ",true), GCR("GCR",true), TX("TX",true), BCY("BCY",true), PRIME("PRIME",true), EXP("EXP",true), NEU("NEU",true), SWING("SWING",true), INFX("INFX",true), OMNI("OMNI",true), USDT("USDT",true), AMP("AMP",true), AGRS("AGRS",true), XLM("XLM",false), SPRTS("SPRTS",true), YBC("YBC",true), BTA("BTA",true), MEC("MEC",true), BITCNY("BITCNY",true), AMS("AMS",true), SCRT("SCRT",true), SCOT("SCOT",true), CLUB("CLUB",true), VOX("VOX",true), MND("MND",true), EMC("EMC",true), FCT("FCT",false), MAID("MAID",true), FRK("FRK",true), EGC("EGC",true), SLS("SLS",true), ORB("ORB",true), STEPS("STEPS",true), RADS("RADS",true), DCR("DCR",true), SAFEX("SAFEX",true), PIVX("PIVX",true), WARP("WARP",true), CRBIT("CRBIT",true), MEME("MEME",true), STEEM("STEEM",true), GIVE("2GIVE",true), LSK("LSK",true), KR("KR",true), PDC("PDC",true), DGD("DGD",false), BRK("BRK",true), WAVES("WAVES",false), RISE("RISE",true), LBC("LBC",true), SBD("SBD",true), BRX("BRX",true), DRACO("DRACO",true), ETC("ETC",false), UNIQ("UNIQ",true), STRAT("STRAT",false), UNB("UNB",true), SYNX("SYNX",true), TRIG("TRIG",true), EBST("EBST",true), VRM("VRM",true), XAUR("XAUR",true), SEQ("SEQ",true), SNGLS("SNGLS",false), REP("REP",false), SHIFT("SHIFT",true), ARDR("ARDR",true), XZC("XZC",true), NEO("NEO",false), ZEC("ZEC",false), ZCL("ZCL",true), IOP("IOP",true), DAR("DAR",true), GOLOS("GOLOS",true), GBG("GBG",true), UBQ("UBQ",true), HKG("HKG",true), KMD("KMD",true), SIB("SIB",true), ION("ION",true), LMC("LMC",true), QWARK("QWARK",true), CRW("CRW",true), SWT("SWT",true), TIME("TIME",false), MLN("MLN",true), TKS("TKS",true), ARK("ARK",true), DYN("DYN",true), MUSIC("MUSIC",true), DTB("DTB",true), INCNT("INCNT",true), GBYTE("GBYTE",true), GNT("GNT",false), NXC("NXC",true), EDG("EDG",true), LGD("LGD",false), TRST("TRST",false), WINGS("WINGS",false), RLC("RLC",false), GNO("GNO",false), GUP("GUP",false), LUN("LUN",false), APX("APX",true), TKN("TKN",false), HMQ("HMQ",false), ANT("ANT",false), ZEN("ZEN",true), SC("SC",false), BAT("BAT",false), FST("1ST",false), QRL("QRL",false), CRB("CRB",false), TROLL("TROLL",true), PTOY("PTOY",false), MYST("MYST",false), CFI("CFI",false), BNT("BNT",false), NMR("NMR",false), SNT("SNT",false), DCT("DCT",true), XEL("XEL",true), MCO("MCO",false), ADT("ADT",false), FUN("FUN",false), PAY("PAY",false), MTL("MTL",false), STORJ("STORJ",false), ADX("ADX",false), OMG("OMG",false), CVC("CVC",false), PART("PART",true), QTUM("QTUM",false), BCC("BCC",false), DNT("DNT",true);
    //@formatter:on

    public final String id;

    public final boolean marketETH;

    /**
     * @param name
     */
    private BitTrexType(String name, boolean eth) {
        this.id = name;
        this.marketETH = !eth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return id;
    }
}
