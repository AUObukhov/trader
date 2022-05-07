package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.tinkoff.piapi.contract.v1.MoneyValue;

import java.math.BigDecimal;

@Mapper
public interface MoneyValueMapper {

    default BigDecimal map(final MoneyValue moneyValue) {
        return moneyValue == null
                ? null
                : DecimalUtils.createBigDecimal(moneyValue.getUnits(), moneyValue.getNano());
    }

    default MoneyValue map(final BigDecimal bigDecimal) {
        return bigDecimal == null
                ? null
                : MoneyValue.newBuilder()
                .setUnits(bigDecimal.longValue())
                .setNano(DecimalUtils.getNano(bigDecimal))
                .build();
    }

    default MoneyValue map(final BigDecimal bigDecimal, final String currency) {
        return bigDecimal == null
                ? null
                : MoneyValue.newBuilder()
                .setUnits(bigDecimal.longValue())
                .setNano(DecimalUtils.getNano(bigDecimal))
                .setCurrency(currency)
                .build();
    }

    default MoneyValue map(final Double doubleValue) {
        return doubleValue == null
                ? null
                : map(BigDecimal.valueOf(doubleValue));
    }

}