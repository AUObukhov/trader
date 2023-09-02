package ru.obukhov.trader.market.model;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.util.DataStructsHelper;
import ru.tinkoff.piapi.contract.v1.InstrumentType;
import ru.tinkoff.piapi.core.models.Position;

import java.math.BigDecimal;

@Setter
@Accessors(chain = true)
public class PositionBuilder {

    private String currency;
    private String figi;
    private String instrumentType;
    private BigDecimal quantity;
    private BigDecimal averagePositionPrice;
    private BigDecimal expectedYield;
    private BigDecimal currentNkd;
    private BigDecimal currentPrice;
    private BigDecimal averagePositionPriceFifo;

    @Tolerate
    public PositionBuilder setInstrumentType(final InstrumentType instrumentType) {
        return setInstrumentType(instrumentType.toString());
    }

    @Tolerate
    public PositionBuilder setQuantity(final long quantity) {
        return setQuantity(BigDecimal.valueOf(quantity));
    }

    @Tolerate
    public PositionBuilder setAveragePositionPrice(final double averagePositionPrice) {
        return setAveragePositionPrice(DecimalUtils.setDefaultScale(averagePositionPrice));
    }

    @Tolerate
    public PositionBuilder setAveragePositionPrice(final ru.tinkoff.piapi.core.models.Money averagePositionPrice) {
        return setAveragePositionPrice(averagePositionPrice.getValue());
    }

    @Tolerate
    public PositionBuilder setExpectedYield(final double expectedYield) {
        return setExpectedYield(DecimalUtils.setDefaultScale(expectedYield));
    }

    @Tolerate
    public PositionBuilder setCurrentNkd(final double currentNkd) {
        return setCurrentNkd(DecimalUtils.setDefaultScale(currentNkd));
    }

    @Tolerate
    public PositionBuilder setCurrentNkd(final ru.tinkoff.piapi.core.models.Money currentNkd) {
        return setCurrentNkd(currentNkd.getValue());
    }

    @Tolerate
    public PositionBuilder setCurrentPrice(final double currentPrice) {
        return setCurrentPrice(DecimalUtils.setDefaultScale(currentPrice));
    }

    @Tolerate
    public PositionBuilder setCurrentPrice(final ru.tinkoff.piapi.core.models.Money currentPrice) {
        return setCurrentPrice(currentPrice.getValue());
    }

    @Tolerate
    public PositionBuilder setAveragePositionPriceFifo(final double averagePositionPriceFifo) {
        return setAveragePositionPriceFifo(DecimalUtils.setDefaultScale(averagePositionPriceFifo));
    }

    @Tolerate
    public PositionBuilder setAveragePositionPriceFifo(final ru.tinkoff.piapi.core.models.Money averagePositionPriceFifo) {
        return setAveragePositionPriceFifo(averagePositionPriceFifo.getValue());
    }

    public Position build() {
        return Position.builder()
                .figi(figi)
                .instrumentType(instrumentType)
                .quantity(quantity)
                .averagePositionPrice(DataStructsHelper.createMoney(averagePositionPrice, currency))
                .expectedYield(expectedYield)
                .currentNkd(DataStructsHelper.createMoney(currentNkd, currency))
                .averagePositionPricePt(BigDecimal.ZERO)
                .currentPrice(DataStructsHelper.createMoney(currentPrice, currency))
                .averagePositionPriceFifo(DataStructsHelper.createMoney(averagePositionPriceFifo, currency))
                .quantityLots(BigDecimal.ZERO)
                .build();
    }
}