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
import ru.obukhov.trader.market.model.OperationType;
import ru.obukhov.trader.market.model.Order;
import ru.obukhov.trader.market.model.OrderStatus;
import ru.obukhov.trader.market.model.OrderType;
import ru.obukhov.trader.test.utils.TestData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class OrdersServiceUnitTest {

    @Mock
    private TinkoffService tinkoffService;
    @Mock
    private MarketService marketService;

    @InjectMocks
    private OrdersService service;

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
                createOrder("order0", figi),
                createOrder("order1", figi),
                createOrder("order2", "figi3"),
                createOrder("order3", "figi4"),
                createOrder("order4", figi)
        );

        final List<Order> orders = service.getOrders(brokerAccountId, ticker);

        Assertions.assertEquals(3, orders.size());
        Assertions.assertEquals("order0", orders.get(0).getOrderId());
        Assertions.assertEquals("order1", orders.get(1).getOrderId());
        Assertions.assertEquals("order4", orders.get(2).getOrderId());
    }

    private void mockOrders(@Nullable final String brokerAccountId, final Order... orders) throws IOException {
        Mockito.when(tinkoffService.getOrders(brokerAccountId)).thenReturn(List.of(orders));
    }

    private Order createOrder(String id, String figi) {
        return new Order()
                .orderId(id)
                .figi(figi)
                .operation(OperationType.BUY)
                .status(OrderStatus.FILL)
                .requestedLots(1)
                .executedLots(1)
                .type(OrderType.MARKET)
                .price(BigDecimal.TEN);
    }

}