package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.test.utils.model.etf.TestEtf1;

class EtfMapperUnitTest {

    private final EtfMapper etfMapper = Mappers.getMapper(EtfMapper.class);

    @Test
    void map() {
        final Etf result = etfMapper.map(TestEtf1.createTinkoffEtf());

        Assertions.assertEquals(TestEtf1.createEtf(), result);
    }

    @Test
    void map_whenValueIsNull() {
        final Etf etf = etfMapper.map(null);

        Assertions.assertNull(etf);
    }

}