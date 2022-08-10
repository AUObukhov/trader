package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.test.utils.model.TestData;

import java.io.IOException;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class MarketOrdersServiceUnitTest {

    @Mock
    private TinkoffService tinkoffService;
    @Mock
    private ExtMarketDataService extMarketDataService;

    @InjectMocks
    private MarketOrdersService service;

    @Test
    void getOrders_filtersOrdersByFigi() throws IOException {
        final String accountId = "2000124699";

        final String ticker = "ticker";
        final String figi = "figi";

        Mockito.when(tinkoffService.getFigiByTicker(ticker)).thenReturn(figi);
        mockOrders(
                accountId,
                TestData.createOrder("order0", figi),
                TestData.createOrder("order1", figi),
                TestData.createOrder("order2", "figi3"),
                TestData.createOrder("order3", "figi4"),
                TestData.createOrder("order4", figi)
        );

        final List<Order> orders = service.getOrders(accountId, ticker);

        Assertions.assertEquals(3, orders.size());
        Assertions.assertEquals("order0", orders.get(0).orderId());
        Assertions.assertEquals("order1", orders.get(1).orderId());
        Assertions.assertEquals("order4", orders.get(2).orderId());
    }

    private void mockOrders(final String accountId, final Order... orders) {
        Mockito.when(tinkoffService.getOrders(accountId)).thenReturn(List.of(orders));
    }

}