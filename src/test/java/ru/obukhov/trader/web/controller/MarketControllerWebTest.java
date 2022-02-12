package ru.obukhov.trader.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.model.Currency;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.web.model.exchange.GetInstrumentsResponse;

import java.math.BigDecimal;
import java.util.List;

class MarketControllerWebTest extends ControllerWebTest {

    @MockBean
    private MarketService marketService;

    @Test
    void get() throws Exception {
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

        Mockito.when(marketService.getInstruments(instrumentType))
                .thenReturn(instruments);

        final String expectedResponse = new ObjectMapper()
                .writeValueAsString(new GetInstrumentsResponse(instruments));

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/market/instruments")
                        .param("instrumentType", instrumentType.getValue())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(marketService, Mockito.times(1))
                .getInstruments(instrumentType);
    }

}