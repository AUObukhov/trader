package ru.obukhov.trader.test.utils.model.instrument;

import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currencies;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.contract.v1.RealExchange;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

public class TestInstruments {

    private static final Instrument APPLE_INSTRUMENT = Instrument.builder()
            .figi("BBG000B9XRY4")
            .ticker("AAPL")
            .classCode("SPBXM")
            .isin("US0378331005")
            .lot(1)
            .currency(Currencies.USD)
            .klong(DecimalUtils.setDefaultScale(2))
            .kshort(DecimalUtils.setDefaultScale(2))
            .dlong(DecimalUtils.ONE)
            .dshort(DecimalUtils.ONE)
            .dlongMin(DecimalUtils.ONE)
            .dshortMin(DecimalUtils.ONE)
            .shortEnabledFlag(false)
            .name("Apple")
            .exchange("SPB")
            .countryOfRisk("US")
            .countryOfRiskName("Соединенные Штаты Америки")
            .instrumentType("share")
            .tradingStatus(SecurityTradingStatus.SECURITY_TRADING_STATUS_NORMAL_TRADING)
            .otcFlag(false)
            .buyAvailableFlag(true)
            .sellAvailableFlag(true)
            .minPriceIncrement(DecimalUtils.setDefaultScale(0.01))
            .apiTradeAvailableFlag(true)
            .uid("a9eb4238-eba9-488c-b102-b6140fd08e38")
            .realExchange(RealExchange.REAL_EXCHANGE_RTS)
            .positionUid("5c5e6656-c4d3-4391-a7ee-e81a76f1804e")
            .forIisFlag(true)
            .forQualInvestorFlag(true)
            .weekendFlag(false)
            .blockedTcaFlag(false)
            .instrumentKind(InstrumentType.INSTRUMENT_TYPE_SHARE)
            .first1MinCandleDate(DateTimeTestData.createDateTime(2018, 1, 23, 10, 34))
            .first1DayCandleDate(DateTimeTestData.createDateTime(1988, 9, 12, 3))
            .build();

    public static final TestInstrument APPLE = new TestInstrument(APPLE_INSTRUMENT);


}