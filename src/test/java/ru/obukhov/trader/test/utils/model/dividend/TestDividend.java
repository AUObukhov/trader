package ru.obukhov.trader.test.utils.model.dividend;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Dividend;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;

public record TestDividend(Dividend dividend, ru.tinkoff.piapi.contract.v1.Dividend tinkoffDividend) {

    TestDividend(final Dividend dividend, final String currency) {
        this(dividend, buildTinkoffDividend(dividend, currency));
    }

    private static ru.tinkoff.piapi.contract.v1.Dividend buildTinkoffDividend(final Dividend dividend, final String currency) {
        final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);
        final MoneyValueMapper moneyValueMapper = Mappers.getMapper(MoneyValueMapper.class);
        final QuotationMapper quotationMapper = Mappers.getMapper(QuotationMapper.class);
        return ru.tinkoff.piapi.contract.v1.Dividend.newBuilder()
                .setDividendNet(moneyValueMapper.map(dividend.dividendNet(), currency))
                .setPaymentDate(dateTimeMapper.offsetDateTimeToTimestamp(dividend.paymentDate()))
                .setDeclaredDate(dateTimeMapper.offsetDateTimeToTimestamp(dividend.declaredDate()))
                .setLastBuyDate(dateTimeMapper.offsetDateTimeToTimestamp(dividend.lastBuyDate()))
                .setDividendType(dividend.dividendType())
                .setRecordDate(dateTimeMapper.offsetDateTimeToTimestamp(dividend.recordDate()))
                .setRegularity(dividend.regularity())
                .setClosePrice(moneyValueMapper.map(dividend.closePrice(), currency))
                .setYieldValue(quotationMapper.fromBigDecimal(dividend.yieldValue()))
                .setCreatedAt(dateTimeMapper.offsetDateTimeToTimestamp(dividend.createdAt()))
                .build();
    }

}