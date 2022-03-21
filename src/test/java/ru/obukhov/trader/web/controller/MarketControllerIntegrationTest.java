package ru.obukhov.trader.web.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.mockserver.model.HttpRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.test.utils.TestData;
import ru.obukhov.trader.web.client.exchange.MarketInstrumentListResponse;
import ru.obukhov.trader.web.model.exchange.GetInstrumentsResponse;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

class MarketControllerIntegrationTest extends ControllerIntegrationTest {

    // region getInstruments when instrumentType is null

    @Test
    @DirtiesContext
    void getInstruments_returnsAllInstruments_whenInstrumentTypeIsNull() throws Exception {
        final MarketInstrument etfInstrument1 = new MarketInstrument()
                .figi("etfFigi1")
                .ticker("etfTicker1")
                .isin("etfIsin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("etfName1")
                .type(InstrumentType.STOCK);
        final MarketInstrument etfInstrument2 = new MarketInstrument()
                .figi("etfFigi2")
                .ticker("etfTicker2")
                .isin("etfIsin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("etfName2")
                .type(InstrumentType.STOCK);
        final List<MarketInstrument> etfInstruments = List.of(etfInstrument1, etfInstrument2);

        final MarketInstrument stockInstrument1 = new MarketInstrument()
                .figi("stockFigi1")
                .ticker("stockTicker1")
                .isin("stockIsin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("stockName1")
                .type(InstrumentType.STOCK);
        final MarketInstrument stockInstrument2 = new MarketInstrument()
                .figi("stockFigi2")
                .ticker("stockTicker2")
                .isin("stockIsin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("stockName2")
                .type(InstrumentType.STOCK);
        final List<MarketInstrument> stockInstruments = List.of(stockInstrument1, stockInstrument2);

        final MarketInstrument bondInstrument1 = new MarketInstrument()
                .figi("bondFigi1")
                .ticker("bondTicker1")
                .isin("bondIsin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("bondName1")
                .type(InstrumentType.STOCK);
        final MarketInstrument bondInstrument2 = new MarketInstrument()
                .figi("bondFigi2")
                .ticker("bondTicker2")
                .isin("bondIsin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("bondName2")
                .type(InstrumentType.STOCK);
        final List<MarketInstrument> bondInstruments = List.of(bondInstrument1, bondInstrument2);

        final MarketInstrument currencyInstrument1 = new MarketInstrument()
                .figi("currencyFigi1")
                .ticker("currencyTicker1")
                .isin("currencyIsin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("currencyName1")
                .type(InstrumentType.STOCK);
        final MarketInstrument currencyInstrument2 = new MarketInstrument()
                .figi("currencyFigi2")
                .ticker("currencyTicker2")
                .isin("currencyIsin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("currencyName2")
                .type(InstrumentType.STOCK);
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
    void getInstruments_AllInstrumentsFromCache_whenInstrumentTypeIsNull() throws Exception {
        final MarketInstrument etfInstrument1 = new MarketInstrument()
                .figi("etfFigi1")
                .ticker("etfTicker1")
                .isin("etfIsin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("etfName1")
                .type(InstrumentType.STOCK);
        final MarketInstrument etfInstrument2 = new MarketInstrument()
                .figi("etfFigi2")
                .ticker("etfTicker2")
                .isin("etfIsin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("etfName2")
                .type(InstrumentType.STOCK);
        final List<MarketInstrument> etfInstruments = List.of(etfInstrument1, etfInstrument2);

        final MarketInstrument stockInstrument1 = new MarketInstrument()
                .figi("stockFigi1")
                .ticker("stockTicker1")
                .isin("stockIsin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("stockName1")
                .type(InstrumentType.STOCK);
        final MarketInstrument stockInstrument2 = new MarketInstrument()
                .figi("stockFigi2")
                .ticker("stockTicker2")
                .isin("stockIsin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("stockName2")
                .type(InstrumentType.STOCK);
        final List<MarketInstrument> stockInstruments = List.of(stockInstrument1, stockInstrument2);

        final MarketInstrument bondInstrument1 = new MarketInstrument()
                .figi("bondFigi1")
                .ticker("bondTicker1")
                .isin("bondIsin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("bondName1")
                .type(InstrumentType.STOCK);
        final MarketInstrument bondInstrument2 = new MarketInstrument()
                .figi("bondFigi2")
                .ticker("bondTicker2")
                .isin("bondIsin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("bondName2")
                .type(InstrumentType.STOCK);
        final List<MarketInstrument> bondInstruments = List.of(bondInstrument1, bondInstrument2);

        final MarketInstrument currencyInstrument1 = new MarketInstrument()
                .figi("currencyFigi1")
                .ticker("currencyTicker1")
                .isin("currencyIsin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("currencyName1")
                .type(InstrumentType.STOCK);
        final MarketInstrument currencyInstrument2 = new MarketInstrument()
                .figi("currencyFigi2")
                .ticker("currencyTicker2")
                .isin("currencyIsin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("currencyName2")
                .type(InstrumentType.STOCK);
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
    void getInstruments_returnsStocks_whenInstrumentTypeIsStock() throws Exception {
        final InstrumentType instrumentType = InstrumentType.STOCK;

        final MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(instrumentType);
        final MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(instrumentType);
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/stocks", instruments);

        performGetInstruments(instrumentType, instruments);
    }

    @Test
    @DirtiesContext
    void getInstruments_returnsStocksFromCache_whenInstrumentTypeIsStock() throws Exception {
        final InstrumentType instrumentType = InstrumentType.STOCK;

        final MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(instrumentType);
        final MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(instrumentType);
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/stocks", instruments);

        performGetInstruments(instrumentType, instruments);
        performGetInstruments(instrumentType, instruments);
    }

    // endregion

    // region getInstruments when instrumentType is Etf

    @Test
    @DirtiesContext
    void getInstruments_returnsEtfs_whenInstrumentTypeIsEtf() throws Exception {
        final InstrumentType instrumentType = InstrumentType.ETF;

        final MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(instrumentType);
        final MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(instrumentType);
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/etfs", instruments);

        performGetInstruments(instrumentType, instruments);
    }

    @Test
    @DirtiesContext
    void getInstruments_returnsEtfsFromCache_whenInstrumentTypeIsEtf() throws Exception {
        final InstrumentType instrumentType = InstrumentType.ETF;

        final MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(instrumentType);
        final MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(instrumentType);
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/etfs", instruments);

        performGetInstruments(instrumentType, instruments);
        performGetInstruments(instrumentType, instruments);
    }

    // endregion

    // region getInstruments when instrumentType is Bond

    @Test
    @DirtiesContext
    void getInstruments_returnsBonds_whenInstrumentTypeIsBond() throws Exception {
        final InstrumentType instrumentType = InstrumentType.BOND;

        final MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(instrumentType);
        final MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(instrumentType);
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/bonds", instruments);

        performGetInstruments(instrumentType, instruments);
    }

    @Test
    @DirtiesContext
    void getInstruments_returnsBondsFromCache_whenInstrumentTypeIsBond() throws Exception {
        final InstrumentType instrumentType = InstrumentType.BOND;

        final MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(instrumentType);
        final MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(instrumentType);
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/bonds", instruments);

        performGetInstruments(instrumentType, instruments);
        performGetInstruments(instrumentType, instruments);
    }

    // endregion

    // region getInstruments when instrumentType is Currency

    @Test
    @DirtiesContext
    void getInstruments_returnsCurrencies_whenInstrumentTypeIsCurrency() throws Exception {
        final InstrumentType instrumentType = InstrumentType.CURRENCY;

        final MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(instrumentType);
        final MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(instrumentType);
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/currencies", instruments);

        performGetInstruments(instrumentType, instruments);
    }

    @Test
    @DirtiesContext
    void getInstruments_returnsCurrenciesFromCache_whenInstrumentTypeIsCurrency() throws Exception {
        final InstrumentType instrumentType = InstrumentType.CURRENCY;

        final MarketInstrument instrument1 = new MarketInstrument()
                .figi("figi1")
                .ticker("ticker1")
                .isin("isin1")
                .minPriceIncrement(BigDecimal.valueOf(10))
                .lot(1)
                .minQuantity(1)
                .currency(Currency.RUB)
                .name("name1")
                .type(instrumentType);
        final MarketInstrument instrument2 = new MarketInstrument()
                .figi("figi2")
                .ticker("ticker2")
                .isin("isin2")
                .minPriceIncrement(BigDecimal.valueOf(20))
                .lot(2)
                .minQuantity(2)
                .currency(Currency.USD)
                .name("name2")
                .type(instrumentType);
        final List<MarketInstrument> instruments = List.of(instrument1, instrument2);

        mockInstruments("/openapi/market/currencies", instruments);

        performGetInstruments(instrumentType, instruments);
        performGetInstruments(instrumentType, instruments);
    }

    // endregion

    private void mockInstruments(final String path, final List<MarketInstrument> instruments) throws JsonProcessingException {
        final HttpRequest apiRequest = HttpRequest.request()
                .withHeader(HttpHeaders.AUTHORIZATION, getAuthorizationHeader())
                .withMethod(HttpMethod.GET.name())
                .withPath(path);

        final MarketInstrumentListResponse response = TestData.createMarketInstrumentListResponse(instruments);
        mockResponse(apiRequest, response);
    }

    private void performGetInstruments(final InstrumentType instrumentType, final List<MarketInstrument> expectedInstruments) throws Exception {
        final String expectedControllerResponse = objectMapper.writeValueAsString(new GetInstrumentsResponse(expectedInstruments));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/market/instruments")
                .contentType(MediaType.APPLICATION_JSON);
        if (instrumentType != null) {
            requestBuilder.param("instrumentType", instrumentType.getValue());
        }

        performAndVerifyResponse(requestBuilder, expectedControllerResponse);
    }

}