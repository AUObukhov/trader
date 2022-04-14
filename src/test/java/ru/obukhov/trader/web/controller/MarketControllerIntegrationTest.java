package ru.obukhov.trader.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.model.exchange.GetInstrumentsResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class MarketControllerIntegrationTest extends ControllerIntegrationTest {

    // region getInstruments when instrumentType is null

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_returnsAllInstruments_whenInstrumentTypeIsNull() throws Exception {
        final MarketInstrument etfInstrument1 = new MarketInstrument(
                "etfFigi1",
                "etfTicker1",
                "etfIsin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "etfName1",
                InstrumentType.ETF
        );
        final MarketInstrument etfInstrument2 = new MarketInstrument(
                "etfFigi2",
                "etfTicker2",
                "etfIsin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "etfName2",
                InstrumentType.ETF
        );
        final List<MarketInstrument> etfInstruments = List.of(etfInstrument1, etfInstrument2);

        final MarketInstrument stockInstrument1 = new MarketInstrument(
                "stockFigi1",
                "stockTicker1",
                "stockTicker1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "stockName1",
                InstrumentType.STOCK
        );
        final MarketInstrument stockInstrument2 = new MarketInstrument(
                "stockFigi2",
                "stockTicker2",
                "stockTicker2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "stockName2",
                InstrumentType.STOCK
        );
        final List<MarketInstrument> stockInstruments = List.of(stockInstrument1, stockInstrument2);

        final MarketInstrument bondInstrument1 = new MarketInstrument(
                "bondFigi1",
                "bondTicker1",
                "bondIsin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "bondName1",
                InstrumentType.BOND
        );
        final MarketInstrument bondInstrument2 = new MarketInstrument(
                "bondFigi2",
                "bondTicker2",
                "bondIsin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "bondName2",
                InstrumentType.BOND
        );
        final List<MarketInstrument> bondInstruments = List.of(bondInstrument1, bondInstrument2);

        final MarketInstrument currencyInstrument1 = new MarketInstrument(
                "currencyFigi1",
                "currencyTicker1",
                "currencyIsin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "currencyName1",
                InstrumentType.CURRENCY
        );
        final MarketInstrument currencyInstrument2 = new MarketInstrument(
                "currencyFigi2",
                "currencyTicker2",
                "currencyIsin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "currencyName2",
                InstrumentType.CURRENCY
        );
        final List<MarketInstrument> currencyInstruments = List.of(currencyInstrument1, currencyInstrument2);

        mockInstruments("/openapi/market/etfs", etfInstruments);
        mockInstruments("/openapi/market/stocks", stockInstruments);
        mockInstruments("/openapi/market/bonds", bondInstruments);
        mockInstruments("/openapi/market/currencies", currencyInstruments);

        final List<MarketInstrument> allInstruments = new ArrayList<>();
        allInstruments.addAll(etfInstruments);
        allInstruments.addAll(stockInstruments);
        allInstruments.addAll(bondInstruments);
        allInstruments.addAll(currencyInstruments);

        performGetInstruments(null, allInstruments);
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_AllInstrumentsFromCache_whenInstrumentTypeIsNull() throws Exception {
        final MarketInstrument etfInstrument1 = new MarketInstrument(
                "etfFigi1",
                "etfTicker1",
                "etfIsin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "etfName1",
                InstrumentType.ETF
        );
        final MarketInstrument etfInstrument2 = new MarketInstrument(
                "etfFigi2",
                "etfTicker2",
                "etfIsin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "etfName2",
                InstrumentType.ETF
        );
        final List<MarketInstrument> etfInstruments = List.of(etfInstrument1, etfInstrument2);

        final MarketInstrument stockInstrument1 = new MarketInstrument(
                "stockFigi1",
                "stockTicker1",
                "stockTicker1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "stockName1",
                InstrumentType.STOCK
        );
        final MarketInstrument stockInstrument2 = new MarketInstrument(
                "stockFigi2",
                "stockTicker2",
                "stockTicker2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "stockName2",
                InstrumentType.STOCK
        );
        final List<MarketInstrument> stockInstruments = List.of(stockInstrument1, stockInstrument2);

        final MarketInstrument bondInstrument1 = new MarketInstrument(
                "bondFigi1",
                "bondTicker1",
                "bondIsin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "bondName1",
                InstrumentType.BOND
        );
        final MarketInstrument bondInstrument2 = new MarketInstrument(
                "bondFigi2",
                "bondTicker2",
                "bondIsin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "bondName2",
                InstrumentType.BOND
        );
        final List<MarketInstrument> bondInstruments = List.of(bondInstrument1, bondInstrument2);

        final MarketInstrument currencyInstrument1 = new MarketInstrument(
                "currencyFigi1",
                "currencyTicker1",
                "currencyIsin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "currencyName1",
                InstrumentType.CURRENCY
        );
        final MarketInstrument currencyInstrument2 = new MarketInstrument(
                "currencyFigi2",
                "currencyTicker2",
                "currencyIsin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "currencyName2",
                InstrumentType.CURRENCY
        );
        final List<MarketInstrument> currencyInstruments = List.of(currencyInstrument1, currencyInstrument2);

        mockInstruments("/openapi/market/etfs", etfInstruments);
        mockInstruments("/openapi/market/stocks", stockInstruments);
        mockInstruments("/openapi/market/bonds", bondInstruments);
        mockInstruments("/openapi/market/currencies", currencyInstruments);

        final List<MarketInstrument> allInstruments = new ArrayList<>();
        allInstruments.addAll(etfInstruments);
        allInstruments.addAll(stockInstruments);
        allInstruments.addAll(bondInstruments);
        allInstruments.addAll(currencyInstruments);

        performGetInstruments(null, allInstruments);
        performGetInstruments(null, allInstruments);
    }

    // endregion

    // region getInstruments when instrumentType is Stock

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_returnsStocks_whenInstrumentTypeIsStock() throws Exception {
        final InstrumentType instrumentType = InstrumentType.STOCK;

        final MarketInstrument instrument1 = new MarketInstrument(
                "figi1",
                "ticker1",
                "isin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "name1",
                instrumentType
        );
        final MarketInstrument instrument2 = new MarketInstrument(
                "figi2",
                "ticker2",
                "isin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "name2",
                instrumentType
        );
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/stocks", instruments);

        performGetInstruments(instrumentType, instruments);
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_returnsStocksFromCache_whenInstrumentTypeIsStock() throws Exception {
        final InstrumentType instrumentType = InstrumentType.STOCK;

        final MarketInstrument instrument1 = new MarketInstrument(
                "figi1",
                "ticker1",
                "isin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "name1",
                instrumentType
        );
        final MarketInstrument instrument2 = new MarketInstrument(
                "figi2",
                "ticker2",
                "isin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "name2",
                instrumentType
        );
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/stocks", instruments);

        performGetInstruments(instrumentType, instruments);
        performGetInstruments(instrumentType, instruments);
    }

    // endregion

    // region getInstruments when instrumentType is Etf

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_returnsEtfs_whenInstrumentTypeIsEtf() throws Exception {
        final InstrumentType instrumentType = InstrumentType.ETF;

        final MarketInstrument instrument1 = new MarketInstrument(
                "figi1",
                "ticker1",
                "isin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "name1",
                instrumentType
        );
        final MarketInstrument instrument2 = new MarketInstrument(
                "figi2",
                "ticker2",
                "isin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "name2",
                instrumentType
        );
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/etfs", instruments);

        performGetInstruments(instrumentType, instruments);
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_returnsEtfsFromCache_whenInstrumentTypeIsEtf() throws Exception {
        final InstrumentType instrumentType = InstrumentType.ETF;

        final MarketInstrument instrument1 = new MarketInstrument(
                "figi1",
                "ticker1",
                "isin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "name1",
                instrumentType
        );
        final MarketInstrument instrument2 = new MarketInstrument(
                "figi2",
                "ticker2",
                "isin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "name2",
                instrumentType
        );
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/etfs", instruments);

        performGetInstruments(instrumentType, instruments);
        performGetInstruments(instrumentType, instruments);
    }

    // endregion

    // region getInstruments when instrumentType is Bond

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_returnsBonds_whenInstrumentTypeIsBond() throws Exception {
        final InstrumentType instrumentType = InstrumentType.BOND;

        final MarketInstrument instrument1 = new MarketInstrument(
                "figi1",
                "ticker1",
                "isin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "name1",
                instrumentType
        );
        final MarketInstrument instrument2 = new MarketInstrument(
                "figi2",
                "ticker2",
                "isin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "name2",
                instrumentType
        );
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/bonds", instruments);

        performGetInstruments(instrumentType, instruments);
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_returnsBondsFromCache_whenInstrumentTypeIsBond() throws Exception {
        final InstrumentType instrumentType = InstrumentType.BOND;

        final MarketInstrument instrument1 = new MarketInstrument(
                "figi1",
                "ticker1",
                "isin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "name1",
                instrumentType
        );
        final MarketInstrument instrument2 = new MarketInstrument(
                "figi2",
                "ticker2",
                "isin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "name2",
                instrumentType
        );
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/bonds", instruments);

        performGetInstruments(instrumentType, instruments);
        performGetInstruments(instrumentType, instruments);
    }

    // endregion

    // region getInstruments when instrumentType is Currency

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_returnsCurrencies_whenInstrumentTypeIsCurrency() throws Exception {
        final InstrumentType instrumentType = InstrumentType.CURRENCY;

        final MarketInstrument instrument1 = new MarketInstrument(
                "figi1",
                "ticker1",
                "isin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "name1",
                instrumentType
        );
        final MarketInstrument instrument2 = new MarketInstrument(
                "figi2",
                "ticker2",
                "isin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "name2",
                instrumentType
        );
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/currencies", instruments);

        performGetInstruments(instrumentType, instruments);
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getInstruments_returnsCurrenciesFromCache_whenInstrumentTypeIsCurrency() throws Exception {
        final InstrumentType instrumentType = InstrumentType.CURRENCY;

        final MarketInstrument instrument1 = new MarketInstrument(
                "figi1",
                "ticker1",
                "isin1",
                BigDecimal.valueOf(10),
                1,
                1,
                Currency.RUB,
                "name1",
                instrumentType
        );
        final MarketInstrument instrument2 = new MarketInstrument(
                "figi2",
                "ticker2",
                "isin2",
                BigDecimal.valueOf(20),
                2,
                2,
                Currency.USD,
                "name2",
                instrumentType
        );
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/currencies", instruments);

        performGetInstruments(instrumentType, instruments);
        performGetInstruments(instrumentType, instruments);
    }

    // endregion

    private void mockInstruments(final String path, final List<MarketInstrument> instruments) throws JsonProcessingException {
        mockResponse(HttpMethod.GET, path, TestData.createMarketInstrumentListResponse(instruments));
    }

    private void performGetInstruments(final InstrumentType instrumentType, final List<MarketInstrument> expectedInstruments) throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/market/instruments")
                .contentType(MediaType.APPLICATION_JSON);
        if (instrumentType != null) {
            requestBuilder.param("instrumentType", instrumentType.getValue());
        }

        performAndExpectResponse(requestBuilder, new GetInstrumentsResponse(expectedInstruments));
    }

}