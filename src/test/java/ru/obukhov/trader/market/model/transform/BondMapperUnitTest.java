package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.test.utils.model.bond.TestBond1;

class BondMapperUnitTest {

    private final BondMapper bondMapper = Mappers.getMapper(BondMapper.class);

    @Test
    void map() {
        final Bond bond = bondMapper.map(TestBond1.TINKOFF_BOND);

        Assertions.assertEquals(TestBond1.BOND, bond);
    }

    @Test
    void map_whenValueIsNull() {
        final Bond bond = bondMapper.map(null);

        Assertions.assertNull(bond);
    }

}