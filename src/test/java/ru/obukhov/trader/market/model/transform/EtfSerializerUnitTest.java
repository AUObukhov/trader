package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.etf.TestEtf1;
import ru.tinkoff.piapi.contract.v1.Etf;

import java.io.IOException;

class EtfSerializerUnitTest extends SerializerAbstractUnitTest<Etf> {

    private final EtfSerializer EtfSerializer = new EtfSerializer();

    @Test
    void test() throws IOException {
        test(EtfSerializer, TestEtf1.ETF, TestEtf1.STRING, new QuotationSerializer(), new MoneyValueSerializer(), new TimestampSerializer());
    }

}