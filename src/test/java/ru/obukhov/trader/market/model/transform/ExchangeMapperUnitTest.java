package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Exchange;

import java.util.stream.Stream;

class ExchangeMapperUnitTest {

    private final ExchangeMapper ExchangeMapper = Mappers.getMapper(ExchangeMapper.class);

    @SuppressWarnings("unused")
    static Stream<Arguments> valuesAndExchanges() {
        return Stream.of(
                Arguments.of("atonbonds", Exchange.ATONBONDS),
                Arguments.of("borsaitaliana", Exchange.BORSAITALIANA),
                Arguments.of("cboe_c1_options", Exchange.CBOE_C1_OPTIONS),
                Arguments.of("cboe_rui", Exchange.CBOE_RUI),
                Arguments.of("cboe_spx", Exchange.CBOE_SPX),
                Arguments.of("cboebzx", Exchange.CBOEBZX),
                Arguments.of("cme_group", Exchange.CME_GROUP),
                Arguments.of("dealer_int_exchange", Exchange.DEALER_INT_EXCHANGE),
                Arguments.of("dealer_otc", Exchange.DEALER_OTC),
                Arguments.of("euronextamsterdam", Exchange.EURONEXTAMSTERDAM),
                Arguments.of("euronextbrussels", Exchange.EURONEXTBRUSSELS),
                Arguments.of("euronextparis", Exchange.EURONEXTPARIS),
                Arguments.of("FORTS", Exchange.FORTS),
                Arguments.of("FORTS_EVENING", Exchange.FORTS_EVENING),
                Arguments.of("FX", Exchange.FX),
                Arguments.of("FX_MTL", Exchange.FX_MTL),
                Arguments.of("FX_WEEKEND", Exchange.FX_WEEKEND),
                Arguments.of("helsinki", Exchange.HELSINKI),
                Arguments.of("hongkong", Exchange.HONGKONG),
                Arguments.of("hongkong_index", Exchange.HONGKONG_INDEX),
                Arguments.of("investorsexchange", Exchange.INVESTORSEXCHANGE),
                Arguments.of("Issuance", Exchange.ISSUANCE),
                Arguments.of("Issuance2", Exchange.ISSUANCE2),
                Arguments.of("lse", Exchange.LSE),
                Arguments.of("lse_index", Exchange.LSE_INDEX),
                Arguments.of("LSE_MORNING", Exchange.LSE_MORNING),
                Arguments.of("madrid", Exchange.MADRID),
                Arguments.of("MOEX", Exchange.MOEX),
                Arguments.of("MOEX_DEALER_WEEKEND", Exchange.MOEX_DEALER_WEEKEND),
                Arguments.of("MOEX_EVENING_WEEKEND", Exchange.MOEX_EVENING_WEEKEND),
                Arguments.of("MOEX_INVESTBOX", Exchange.MOEX_INVESTBOX),
                Arguments.of("MOEX_MORNING", Exchange.MOEX_MORNING),
                Arguments.of("MOEX_PLUS", Exchange.MOEX_PLUS),
                Arguments.of("MOEX_PLUS_WEEKEND", Exchange.MOEX_PLUS_WEEKEND),
                Arguments.of("MOEX_WEEKEND", Exchange.MOEX_WEEKEND),
                Arguments.of("nasdaq", Exchange.NASDAQ),
                Arguments.of("notes", Exchange.NOTES),
                Arguments.of("nyse", Exchange.NYSE),
                Arguments.of("oslo", Exchange.OSLO),
                Arguments.of("otcus", Exchange.OTCUS),
                Arguments.of("refinitiv", Exchange.REFINITIV),
                Arguments.of("refinitiv_crypto", Exchange.REFINITIV_CRYPTO),
                Arguments.of("sber_otc", Exchange.SBER_OTC),
                Arguments.of("SPB", Exchange.SPB),
                Arguments.of("SPB_DE", Exchange.SPB_DE),
                Arguments.of("SPB_DE_MORNING", Exchange.SPB_DE_MORNING),
                Arguments.of("SPB_EUROBONDS", Exchange.SPB_EUROBONDS),
                Arguments.of("SPB_HK", Exchange.SPB_HK),
                Arguments.of("SPB_MORNING", Exchange.SPB_MORNING),
                Arguments.of("SPB_MORNING_WEEKEND", Exchange.SPB_MORNING_WEEKEND),
                Arguments.of("SPB_RU_MORNING", Exchange.SPB_RU_MORNING),
                Arguments.of("SPB_WEEKEND", Exchange.SPB_WEEKEND),
                Arguments.of("sse", Exchange.SSE),
                Arguments.of("swiss", Exchange.SWISS),
                Arguments.of("theice", Exchange.THEICE),
                Arguments.of("vienna", Exchange.VIENNA),
                Arguments.of("XETR", Exchange.XETR),
                Arguments.of("xetra", Exchange.XETRA)
        );
    }

    @ParameterizedTest
    @MethodSource("valuesAndExchanges")
    void map(final String value, final Exchange Exchange) {
        Assertions.assertEquals(Exchange, ExchangeMapper.map(value));
    }

}