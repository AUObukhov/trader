package ru.obukhov.trader.test.utils.model.dividend;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Dividend;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyValueMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;

public record TestDividend(Dividend dividend, ru.tinkoff.piapi.contract.v1.Dividend tinkoffDividend, String jsonString) {

    TestDividend(final Dividend dividend, final String currency) {
        this(dividend, buildTinkoffDividend(dividend, currency), buildJsonString(dividend));
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

    private static String buildJsonString(final Dividend dividend) {
        return "{\"dividendNet\":\"" + DecimalUtils.toPrettyStringSafe(dividend.dividendNet()) + "\"," +
                "\"paymentDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(dividend.paymentDate()) + "\"," +
                "\"declaredDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(dividend.declaredDate()) + "\"," +
                "\"lastBuyDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(dividend.lastBuyDate()) + "\"," +
                "\"dividendType\":" + dividend.dividendType() + "," +
                "\"recordDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(dividend.recordDate()) + "\"," +
                "\"regularity\":" + dividend.regularity() + "," +
                "\"closePrice\":" + DecimalUtils.toPrettyStringSafe(dividend.closePrice()) + "," +
                "\"yieldValue\":" + DecimalUtils.toPrettyStringSafe(dividend.yieldValue()) + "," +
                "\"createdAt\":" + dividend.createdAt() + "}";
    }

}