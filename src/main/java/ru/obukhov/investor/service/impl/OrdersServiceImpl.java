package ru.obukhov.investor.service.impl;

import org.jetbrains.annotations.NotNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import ru.obukhov.investor.bot.interfaces.TinkoffService;
import ru.obukhov.investor.service.interfaces.MarketService;
import ru.obukhov.investor.service.interfaces.OrdersService;
import ru.tinkoff.invest.openapi.models.orders.LimitOrder;
import ru.tinkoff.invest.openapi.models.orders.MarketOrder;
import ru.tinkoff.invest.openapi.models.orders.Operation;
import ru.tinkoff.invest.openapi.models.orders.Order;
import ru.tinkoff.invest.openapi.models.orders.PlacedOrder;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service("ordersServiceImpl")
public class OrdersServiceImpl implements OrdersService {

    private final TinkoffService tinkoffService;
    private final MarketService marketService;

    public OrdersServiceImpl(TinkoffService tinkoffService, MarketService marketService) {
        this.tinkoffService = tinkoffService;
        this.marketService = marketService;
    }

    @Override
    public List<Order> getOrders(String ticker) {
        String figi = marketService.getFigi(ticker);
        return getOrders().stream()
                .filter(order -> figi.equals(order.figi))
                .collect(Collectors.toList());
    }

    @Override
    public List<Order> getOrders() {
        return tinkoffService.getOrders();
    }

    @Override
    public PlacedOrder placeOrder(@NotNull String ticker,
                                  int lots,
                                  @NotNull Operation operation,
                                  @Nullable BigDecimal price) {
        String figi = marketService.getFigi(ticker);
        if (price == null) {
            MarketOrder marketOrder = new MarketOrder(lots, operation);
            return tinkoffService.placeMarketOrder(figi, marketOrder);
        } else {
            LimitOrder limitOrder = new LimitOrder(lots, operation, price);
            return tinkoffService.placeLimitOrder(figi, limitOrder);
        }
    }

    @Override
    public void cancelOrder(@NotNull String orderId) {
        tinkoffService.cancelOrder(orderId);
    }

}