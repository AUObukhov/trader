package ru.obukhov.trader.web.controller;

import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.test.utils.TestUtils;
import ru.tinkoff.piapi.contract.v1.CandleInterval;
import ru.tinkoff.piapi.contract.v1.HistoricCandle;
import ru.tinkoff.piapi.contract.v1.Share;
import ru.tinkoff.piapi.core.InstrumentsService;
import ru.tinkoff.piapi.core.MarketDataService;

import java.time.Instant;
import java.util.List;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
abstract class ControllerIntegrationTest {

    protected static final JsonPathResultMatchers RESULT_MESSAGE_MATCHER = MockMvcResultMatchers.jsonPath("$.message");
    protected static final JsonPathResultMatchers ERRORS_MATCHER = MockMvcResultMatchers.jsonPath("$.errors");
    protected static final ResultMatcher JSON_CONTENT_MATCHER = MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON);

    @MockBean
    protected InstrumentsService instrumentsService;
    @MockBean
    protected MarketDataService marketDataService;

    @Autowired
    protected MockMvc mockMvc;

    protected void performAndExpectResponse(final MockHttpServletRequestBuilder builder, final Object expectedResponse) throws Exception {
        final String expectedResponseString = TestUtils.OBJECT_MAPPER.writeValueAsString(expectedResponse);
        mockMvc.perform(builder)
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(JSON_CONTENT_MATCHER)
                .andExpect(MockMvcResultMatchers.content().json(expectedResponseString));
    }

    protected void performAndExpectBadRequestResult(final MockHttpServletRequestBuilder requestBuilder, final String expectedResultMessage)
            throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(RESULT_MESSAGE_MATCHER.value(expectedResultMessage))
                .andExpect(JSON_CONTENT_MATCHER);
    }

    protected void performAndExpectBadRequestError(final String urlTemplate, final Object request, final String expectedError) throws Exception {
        final String requestString = TestUtils.OBJECT_MAPPER.writeValueAsString(request);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.post(urlTemplate)
                .content(requestString)
                .contentType(MediaType.APPLICATION_JSON);
        mockMvc.perform(requestBuilder)
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(RESULT_MESSAGE_MATCHER.value("Invalid request"))
                .andExpect(ERRORS_MATCHER.value(expectedError))
                .andExpect(JSON_CONTENT_MATCHER);
    }

    protected void mockShare(final String figi, final String ticker, final Currency currency, final int lotSize) {
        final Share share = Share.newBuilder()
                .setFigi(figi)
                .setTicker(ticker)
                .setCurrency(currency.name())
                .setLot(lotSize)
                .build();
        Mockito.when(instrumentsService.getAllSharesSync()).thenReturn(List.of(share));
    }

    protected void mockHistoricCandles(final String figi, final List<HistoricCandle> historicCandles) {
        Mockito.when(marketDataService.getCandlesSync(
                Mockito.eq(figi),
                Mockito.any(Instant.class),
                Mockito.any(Instant.class),
                Mockito.eq(CandleInterval.CANDLE_INTERVAL_1_MIN)
        )).thenAnswer(args -> historicCandles.stream()
                .filter(candle -> DateUtils.timestampIsInInterval(candle.getTime(), args.getArgument(1), args.getArgument(2)))
                .toList()
        );
    }

}