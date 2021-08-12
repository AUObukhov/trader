package ru.obukhov.trader.market.impl;

import org.jetbrains.annotations.NotNull;
import ru.obukhov.trader.market.interfaces.MarketService;
import ru.obukhov.trader.market.interfaces.OrdersService;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.model.rest.LimitOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.MarketOrderRequest;
import ru.tinkoff.invest.openapi.model.rest.OperationType;
import ru.tinkoff.invest.openapi.model.rest.Order;
import ru.tinkoff.invest.openapi.model.rest.PlacedLimitOrder;
import ru.tinkoff.invest.openapi.model.rest.PlacedMarketOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class OrdersServiceImpl implements OrdersService {

    private final TinkoffService tinkoffService;
    private final MarketService marketService;

    public OrdersServiceImpl(final TinkoffService tinkoffService, final MarketService marketService) {
        this.tinkoffService = tinkoffService;
        this.marketService = marketService;
    }

    @Override
    public List<Order> getOrders(final String ticker) {
        final String figi = marketService.getFigi(ticker);
        return getOrders().stream()
                .filter(order -> figi.equals(order.getFigi()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrders() {
        return tinkoffService.getOrders();
    }

    @Override
    public PlacedMarketOrder placeMarketOrder(@NotNull final String ticker, final int lots, @NotNull final OperationType operationType) {
        final MarketOrderRequest orderRequest = new MarketOrderRequest()
                .lots(lots)
                .operation(operationType);
        return tinkoffService.placeMarketOrder(ticker, orderRequest);

    }

    @Override
    public PlacedLimitOrder placeLimitOrder(
            @NotNull final String ticker,
            final int lots,
            @NotNull final OperationType operationType,
            final BigDecimal price
    ) {
        final LimitOrderRequest orderRequest = new LimitOrderRequest()
                .lots(lots)
                .operation(operationType)
                .price(price);
        return tinkoffService.placeLimitOrder(ticker, orderRequest);
    }

    @Override
    public void cancelOrder(@NotNull String orderId) {
        tinkoffService.cancelOrder(orderId);
    }

}