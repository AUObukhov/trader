package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.test.utils.model.bond.TestBond;
import ru.obukhov.trader.test.utils.model.bond.TestBonds;

class BondMapperUnitTest {

    private final BondMapper bondMapper = Mappers.getMapper(BondMapper.class);

    @Test
    void map() {
        final TestBond testBond = TestBonds.ROSTELECOM;

        final Bond bond = bondMapper.map(testBond.tBond());

        Assertions.assertEquals(testBond.bond(), bond);
    }

    @Test
    void map_whenValueIsNull() {
        final Bond bond = bondMapper.map(null);

        Assertions.assertNull(bond);
    }

}