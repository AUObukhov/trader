package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.obukhov.trader.test.utils.model.share.TestShare2;
import ru.obukhov.trader.test.utils.model.share.TestShare3;
import ru.obukhov.trader.test.utils.model.share.TestShare4;
import ru.tinkoff.piapi.contract.v1.Asset;
import ru.tinkoff.piapi.contract.v1.AssetInstrument;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.util.Collections;
import java.util.List;

class MarketDataControllerIntegrationTest extends ControllerIntegrationTest {

    // region getTradingStatus tests

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getTradingStatus_returnsBadRequest_whenTickerIsNull() throws Exception {
        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/status")
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Required request parameter 'ticker' for method parameter type String is not present";
        performAndExpectBadRequestResult(requestBuilder, expectedMessage);
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getTradingStatus_returnsServerError_whenNoAsset() throws Exception {
        final String figi = TestShare1.FIGI;
        final String ticker = TestShare1.TICKER;

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);
        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(Collections.emptyList());

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/status")
                .param("ticker", ticker)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single instrument with ticker '" + ticker + "'. Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getTradingStatus_returnsServerError_whenNoInstrument() throws Exception {
        final String figi = TestShare4.FIGI;
        final String ticker = TestShare4.TICKER;

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);

        final AssetInstrument assetInstrument11 = TestData.createAssetInstrument(TestShare1.FIGI, TestShare1.TICKER);
        final AssetInstrument assetInstrument21 = TestData.createAssetInstrument(TestShare2.FIGI, TestShare2.TICKER);
        final AssetInstrument assetInstrument22 = TestData.createAssetInstrument(TestShare3.FIGI, TestShare3.TICKER);

        final Asset asset1 = Asset.newBuilder()
                .addInstruments(assetInstrument11)
                .build();
        final Asset asset2 = Asset.newBuilder()
                .addInstruments(assetInstrument21)
                .addInstruments(assetInstrument22)
                .build();

        Mockito.when(instrumentsService.getAssetsSync()).thenReturn(List.of(asset1, asset2));

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/status")
                .param("ticker", ticker)
                .contentType(MediaType.APPLICATION_JSON);

        final String expectedMessage = "Expected single instrument with ticker '" + ticker + "'. Found 0";
        performAndExpectServerError(requestBuilder, expectedMessage);
    }

    @Test
    @DirtiesContext
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getTradingStatus_returnsStatus() throws Exception {
        final String figi = TestShare1.FIGI;
        final String ticker = TestShare1.TICKER;

        Mocker.mockFigiByTicker(instrumentsService, figi, ticker);
        final SecurityTradingStatus status = SecurityTradingStatus.SECURITY_TRADING_STATUS_OPENING_PERIOD;
        Mocker.mockTradingStatus(marketDataService, figi, status);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/trader/market/status")
                .param("ticker", ticker)
                .contentType(MediaType.APPLICATION_JSON);

        performAndExpectResponse(requestBuilder, status);
    }

    // endregion

}