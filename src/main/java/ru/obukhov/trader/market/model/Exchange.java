package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RequiredArgsConstructor
public enum Exchange {

    ATONBONDS("atonbonds"),
    BORSAITALIANA("borsaitaliana"),
    CBOE_C1_OPTIONS("cboe_c1_options"),
    CBOE_RUI("cboe_rui"),
    CBOE_SPX("cboe_spx"),
    CBOEBZX("cboebzx"),
    CME_GROUP("cme_group"),
    DEALER_INT_EXCHANGE("dealer_int_exchange"),
    DEALER_OTC("dealer_otc"),
    EURONEXTAMSTERDAM("euronextamsterdam"),
    EURONEXTBRUSSELS("euronextbrussels"),
    EURONEXTPARIS("euronextparis"),
    FORTS("FORTS"),
    FORTS_EVENING("FORTS_EVENING"),
    FX("FX"),
    FX_MTL("FX_MTL"),
    FX_WEEKEND("FX_WEEKEND"),
    HELSINKI("helsinki"),
    HONGKONG("hongkong"),
    HONGKONG_INDEX("hongkong_index"),
    INVESTORSEXCHANGE("investorsexchange"),
    ISSUANCE("Issuance"),
    ISSUANCE2("Issuance2"),
    LSE("lse"),
    LSE_INDEX("lse_index"),
    LSE_MORNING("LSE_MORNING"),
    madrid("madrid"),
    MOEX("MOEX"),
    MOEX_DEALER_WEEKEND("MOEX_DEALER_WEEKEND"),
    MOEX_EVENING_WEEKEND("MOEX_EVENING_WEEKEND"),
    MOEX_INVESTBOX("MOEX_INVESTBOX"),
    MOEX_MORNING("MOEX_MORNING"),
    MOEX_PLUS("MOEX_PLUS"),
    MOEX_PLUS_WEEKEND("MOEX_PLUS_WEEKEND"),
    MOEX_WEEKEND("MOEX_WEEKEND"),
    NASDAQ("nasdaq"),
    NOTES("notes"),
    NYSE("nyse"),
    OSLO("oslo"),
    OTCUS("otcus"),
    REFINITIV("refinitiv"),
    REFINITIV_CRYPTO("refinitiv_crypto"),
    SBER_OTC("sber_otc"),
    SPB("SPB"),
    SPB_DE("SPB_DE"),
    SPB_DE_MORNING("SPB_DE_MORNING"),
    SPB_EUROBONDS("SPB_EUROBONDS"),
    SPB_HK("SPB_HK"),
    SPB_MORNING("SPB_MORNING"),
    SPB_MORNING_WEEKEND("SPB_MORNING_WEEKEND"),
    SPB_RU_MORNING("SPB_RU_MORNING"),
    SPB_WEEKEND("SPB_WEEKEND"),
    SSE("sse"),
    SWISS("swiss"),
    THEICE("theice"),
    VIENNA("vienna"),
    XETR("XETR"),
    XETRA("xetra");

    private static final Map<String, Exchange> LOOKUP = Stream.of(Exchange.values())
            .collect(Collectors.toMap(Exchange::getValue, orderStatus -> orderStatus));

    @Getter
    @JsonValue
    private final String value;

    @Override
    public String toString() {
        return value;
    }

    public static Exchange fromValue(String text) {
        return LOOKUP.get(text);
    }
}