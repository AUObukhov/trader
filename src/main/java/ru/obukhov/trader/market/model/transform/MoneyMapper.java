package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.Money;
import ru.tinkoff.piapi.contract.v1.MoneyValue;

import java.math.BigDecimal;

@Mapper
public interface MoneyMapper {

    default Money moneyValueToMoney(final MoneyValue moneyValue) {
        if (moneyValue == null) {
            return null;
        }

        final Currency currency = Currency.valueOfIgnoreCase(moneyValue.getCurrency());
        final BigDecimal value = DecimalUtils.createBigDecimal(moneyValue.getUnits(), moneyValue.getNano());
        return Money.of(currency, value);
    }

    default BigDecimal moneyValueToBigDecimal(final MoneyValue moneyValue) {
        return moneyValue == null
                ? null
                : DecimalUtils.createBigDecimal(moneyValue.getUnits(), moneyValue.getNano());
    }

    default MoneyValue bigDecimalToMoneyValue(final BigDecimal bigDecimal) {
        return bigDecimal == null
                ? null
                : MoneyValue.newBuilder()
                .setUnits(bigDecimal.longValue())
                .setNano(DecimalUtils.getNano(bigDecimal))
                .build();
    }

    default MoneyValue doubleToMoneyValue(final Double doubleValue) {
        return doubleValue == null
                ? null
                : bigDecimalToMoneyValue(BigDecimal.valueOf(doubleValue));
    }

}