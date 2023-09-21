package ru.obukhov.trader.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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

    @Test
    void objectMapper() throws JsonProcessingException {
        final OffsetDateTime initialDateTime = DateTimeTestData.newDateTime(2023, 9, 15, 11, 34, 45, 123);
        final String stringDateTime = objectMapper.writeValueAsString(initialDateTime);
        final OffsetDateTime parsedDateTime = objectMapper.readValue(stringDateTime, OffsetDateTime.class);

        Assertions.assertEquals("\"2023-09-15T11:34:45.000000123+03:00\"", stringDateTime);
        Assertions.assertEquals(initialDateTime, parsedDateTime);
    }

}