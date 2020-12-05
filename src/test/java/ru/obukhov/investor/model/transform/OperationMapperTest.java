package ru.obukhov.investor.model.transform;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.investor.util.MathUtils;
import ru.obukhov.investor.web.model.SimulatedOperation;
import ru.tinkoff.invest.openapi.models.Currency;
import ru.tinkoff.invest.openapi.models.MoneyAmount;
import ru.tinkoff.invest.openapi.models.operations.Operation;
import ru.tinkoff.invest.openapi.models.operations.OperationStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class OperationMapperTest {

    private final OperationMapper operationMapper = Mappers.getMapper(OperationMapper.class);

    @Test
    public void mapsDateTime() {
        OffsetDateTime date = OffsetDateTime.now();
        Operation source = createOperation(null, null, null, date);

        SimulatedOperation target = operationMapper.map(source);

        Assert.assertEquals(date, target.getDateTime());
    }

    // region amount mapping tests

    @Test
    public void notMapsAmount_whenPriceIsNull_andQuantityIsNotnull() {
        Operation source = createOperation(null, null, 1, null);

        SimulatedOperation target = operationMapper.map(source);

        Assert.assertNull(target.getAmount());
    }

    @Test
    public void notMapsAmount_whenPriceIsNotNull_andQuantityIsNull() {
        Operation source = createOperation(null, BigDecimal.TEN, null, null);

        SimulatedOperation target = operationMapper.map(source);

        Assert.assertNull(target.getAmount());
    }

    @Test
    public void notMapsAmount_whenPriceAndQuantityAreNull() {
        Operation source = createOperation(null, null, null, null);

        SimulatedOperation target = operationMapper.map(source);

        Assert.assertNull(target.getAmount());
    }

    @Test
    public void mapsAmount_whenPriceAndQuantityAreNotNull() {
        Operation source = createOperation(null, BigDecimal.TEN, 2, null);

        SimulatedOperation target = operationMapper.map(source);

        Assert.assertTrue(MathUtils.numbersEqual(target.getAmount(), 20));
    }

    // endregion

    @Test
    public void mapsCommission() {
        BigDecimal commission = BigDecimal.TEN;
        Operation source = createOperation(commission, null, null, null);

        SimulatedOperation target = operationMapper.map(source);

        Assert.assertEquals(commission, target.getCommission());
    }

    private Operation createOperation(BigDecimal commission, BigDecimal price, Integer quantity, OffsetDateTime date) {
        MoneyAmount commissionMoneyAmount = new MoneyAmount(Currency.RUB, commission);
        return new Operation(StringUtils.EMPTY,
                OperationStatus.Done,
                null,
                commissionMoneyAmount,
                Currency.RUB,
                BigDecimal.ZERO,
                price,
                quantity,
                null,
                null,
                false,
                date,
                null);
    }

}