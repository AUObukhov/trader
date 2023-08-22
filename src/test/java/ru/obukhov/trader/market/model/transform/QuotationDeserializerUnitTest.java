package ru.obukhov.trader.market.model.transform;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.io.IOException;

class QuotationDeserializerUnitTest {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();
    private final QuotationDeserializer quotationDeserializer = new QuotationDeserializer();

    @ParameterizedTest
    @CsvSource(value = {
            "0, 0, 0",

            "123, 123, 0",
            "0.456, 0, 456000000",
            "123.456, 123, 456000000",

            "-123, -123, 0",
            "-0.456, 0, -456000000",
            "-123.456, -123, -456000000",
    })
    void test(final String value, final long expectedUnits, final int expectedNano) throws IOException {
        final JsonParser jsonParser = JSON_FACTORY.createParser(value);
        jsonParser.setCodec(new ObjectMapper());

        final Quotation actualResult = quotationDeserializer.deserialize(jsonParser, null);

        Assertions.assertEquals(expectedUnits, actualResult.getUnits());
        Assertions.assertEquals(expectedNano, actualResult.getNano());
    }

}