package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.test.utils.TestData;

import java.io.IOException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class MarketOrdersServiceUnitTest {

    @Mock
    private TinkoffService tinkoffService;
    @Mock
    private MarketService marketService;

    @InjectMocks
    private MarketOrdersService service;

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = "2000124699")
    void getOrders_filtersOrdersByFigi(@Nullable final String brokerAccountId) throws IOException {
        final String ticker = "ticker";
        final String figi = "figi";

        final MarketInstrument instrument = TestData.createMarketInstrument(ticker, figi);
        Mockito.when(marketService.getInstrument(ticker)).thenReturn(instrument);
        mockOrders(
                brokerAccountId,
                TestData.createOrder("order0", figi),
                TestData.createOrder("order1", figi),
                TestData.createOrder("order2", "figi3"),
                TestData.createOrder("order3", "figi4"),
                TestData.createOrder("order4", figi)
        );

        final List<Order> orders = service.getOrders(brokerAccountId, ticker);

        Assertions.assertEquals(3, orders.size());
        Assertions.assertEquals("order0", orders.get(0).orderId());
        Assertions.assertEquals("order1", orders.get(1).orderId());
        Assertions.assertEquals("order4", orders.get(2).orderId());
    }

    private void mockOrders(@Nullable final String brokerAccountId, final Order... orders) throws IOException {
        Mockito.when(tinkoffService.getOrders(brokerAccountId)).thenReturn(List.of(orders));
    }

}