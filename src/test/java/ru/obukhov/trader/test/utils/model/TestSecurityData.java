package ru.obukhov.trader.test.utils.model;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.market.model.transform.MoneyMapper;
import ru.obukhov.trader.market.model.transform.QuotationMapper;

public abstract class TestSecurityData {
    protected static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);
    protected static final MoneyMapper MONEY_VALUE_MAPPER = Mappers.getMapper(MoneyMapper.class);
    protected static final QuotationMapper QUOTATION_MAPPER = Mappers.getMapper(QuotationMapper.class);
}