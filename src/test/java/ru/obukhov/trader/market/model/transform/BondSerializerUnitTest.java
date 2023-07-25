package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.bond.TestBond1;
import ru.tinkoff.piapi.contract.v1.Bond;

import java.io.IOException;

class BondSerializerUnitTest extends SerializerAbstractUnitTest<Bond> {

    private final BondSerializer bondSerializer = new BondSerializer();

    @Test
    void test() throws IOException {
        test(bondSerializer, TestBond1.BOND, TestBond1.STRING, new QuotationSerializer(), new MoneyValueSerializer(), new TimestampSerializer());
    }

}