package ru.obukhov.trader.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import ru.obukhov.trader.TokenValidationStartupListener;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.InvestApi;
import ru.tinkoff.piapi.core.MarketDataService;
import ru.tinkoff.piapi.core.OperationsService;
import ru.tinkoff.piapi.core.OrdersService;
import ru.tinkoff.piapi.core.UsersService;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@SpringBootTest
@ActiveProfiles("test")
public class BeanConfigurationIntegrationTest {

    // following beans creation is tested automatically in each test
    @Autowired
    private InvestApi investApi;
    @Autowired
    private InstrumentsService instrumentsService;
    @Autowired
    private MarketDataService marketDataService;
    @Autowired
    private OperationsService operationsService;
    @Autowired
    private OrdersService ordersService;
    @Autowired
    private UsersService usersService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    @SuppressWarnings("unused")
    public static TokenValidationStartupListener tokenValidationStartupListener;

    @Test
    void beansCreated() {
        Assertions.assertNotNull(investApi);
        Assertions.assertNotNull(instrumentsService);
        Assertions.assertNotNull(marketDataService);
        Assertions.assertNotNull(operationsService);
        Assertions.assertNotNull(ordersService);
        Assertions.assertNotNull(usersService);
    }

    // region objectMapper tests

    @Test
    void objectMapper_serializesDateTime() throws JsonProcessingException {
        final OffsetDateTime dateTime = DateTimeTestData.newDateTime(2023, 9, 15, 11, 34, 45, 123);
        final String actualResult = objectMapper.writeValueAsString(dateTime);

        Assertions.assertEquals("\"2023-09-15T11:34:45.000000123+03:00\"", actualResult);
    }

    @Test
    void objectMapper_deserializesDateTime() throws JsonProcessingException {
        final String stringDateTime = "\"2023-09-15T11:34:45.000000123+03:00\"";
        final OffsetDateTime actualResult = objectMapper.readValue(stringDateTime, OffsetDateTime.class);

        final OffsetDateTime expectedResult = DateTimeTestData.newDateTime(2023, 9, 15, 11, 34, 45, 123);
        Assertions.assertEquals(expectedResult, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            ", null",
            "0, 0",
            "14E1, 140",
            "1000.5, 1000.5",
            "0.123456789123, 0.123456789123",
    },
            nullValues = "")
    void objectMapper_serializesBigDecimal(final BigDecimal value, final String expectedSerializedValue) throws JsonProcessingException {
        final String actualResult = objectMapper.writeValueAsString(value);
        Assertions.assertEquals(expectedSerializedValue, actualResult);
    }

    @ParameterizedTest
    @CsvSource(value = {
            "null, nullValue",
            "0, 0.000000000",
            "14E1, 140.000000000",
            "1000.5, 1000.500000000",
            "0.123456789123, 0.123456789",
    },
            nullValues = "nullValue")
    void objectMapper_deserializesBigDecimal(final String stringValue, final BigDecimal expectedDeserializedValue) throws JsonProcessingException {
        final BigDecimal actualResult = objectMapper.readValue(stringValue, BigDecimal.class);
        Assertions.assertEquals(expectedDeserializedValue, actualResult);
    }

    // endregion

}