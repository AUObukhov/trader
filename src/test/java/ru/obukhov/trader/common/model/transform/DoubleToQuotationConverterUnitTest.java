package ru.obukhov.trader.common.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.tinkoff.piapi.contract.v1.Quotation;

class DoubleToQuotationConverterUnitTest {

    private final DoubleToQuotationConverter converter = new DoubleToQuotationConverter();

    @ParameterizedTest
    @CsvSource(value = {
            "0.0, 0, 0",

            "123, 123, 0",
            "123.0, 123, 0",
            "0.456, 0, 456000000",
            "100.000000100, 100, 100",
            "123.0000000004, 123, 0",
            "123.0000000005, 123, 1",

            "-123, -123, 0",
            "-123.0, -123, 0",
            "-0.456, 0, -456000000",
            "-100.000000100, -100, -100",
            "-123.0000000004, -123, 0",
            "-123.0000000005, -123, -1",
    })
    void convert(final double value, final long expectedUnits, final int expectedNano) {
        final Quotation quotation = converter.convert(value);

        Assertions.assertNotNull(quotation);
        Assertions.assertEquals(expectedUnits, quotation.getUnits());
        Assertions.assertEquals(expectedNano, quotation.getNano());
    }

}