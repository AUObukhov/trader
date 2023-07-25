package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.io.IOException;

class QuotationSerializerUnitTest extends SerializerAbstractUnitTest<Quotation> {

    private final QuotationSerializer quotationSerializer = new QuotationSerializer();

    @Test
    void test() throws IOException {
        final Quotation quotation = Quotation.newBuilder()
                .setUnits(-10)
                .setNano(40000000)
                .build();
        final String expectedResult = "{\"units\":-10,\"nano\":40000000}";
        test(quotationSerializer, quotation, expectedResult);
    }

}