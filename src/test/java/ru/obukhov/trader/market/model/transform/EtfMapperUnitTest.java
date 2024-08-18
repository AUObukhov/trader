package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.test.utils.model.etf.TestEtf;
import ru.obukhov.trader.test.utils.model.etf.TestEtfs;

class EtfMapperUnitTest {

    private final EtfMapper etfMapper = Mappers.getMapper(EtfMapper.class);

    @Test
    void map() {
        final TestEtf testEtf = TestEtfs.FXIT;

        final Etf result = etfMapper.map(testEtf.tEtf());

        Assertions.assertEquals(testEtf.etf(), result);
    }

    @Test
    void map_whenValueIsNull() {
        final Etf etf = etfMapper.map(null);

        Assertions.assertNull(etf);
    }

}