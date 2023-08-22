package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.obukhov.trader.common.util.QuotationUtils;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.io.IOException;

class QuotationSerializerUnitTest extends SerializerAbstractUnitTest<Quotation> {

    private final QuotationSerializer quotationSerializer = new QuotationSerializer();

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0",
            "1000, 0, 1000",
            "0, 2000, 0.000002",
            "3000, 4000, 3000.000004",
            "5000, 100000000, 5000.1",
            "6000, 7, 6000.000000007",
            "-8000, 0,-8000",
            "0, -9000, -0.000009",
            "-1100, -1200, -1100.0000012",
            "-1300, -140000000, -1300.14",
            "-1500, -1, -1500.000000001",
    })
    void test(final long units, final int nano, final String expectedResult) throws IOException {
        final Quotation quotation = QuotationUtils.newQuotation(units, nano);

        test(quotationSerializer, quotation, expectedResult);
    }

}