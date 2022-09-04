package ru.obukhov.trader.market.util;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.Tolerate;
import ru.obukhov.trader.common.util.DecimalUtils;
import ru.obukhov.trader.market.model.Currency;
import ru.tinkoff.piapi.contract.v1.MoneyValue;
import ru.tinkoff.piapi.contract.v1.OrderDirection;
import ru.tinkoff.piapi.contract.v1.OrderExecutionReportStatus;
import ru.tinkoff.piapi.contract.v1.OrderType;
import ru.tinkoff.piapi.contract.v1.PostOrderResponse;

import java.math.BigDecimal;

@Setter
@Accessors(chain = true)
public class PostOrderResponseBuilder {

    private String currency;
    private BigDecimal totalOrderAmount;
    private BigDecimal totalCommissionAmount;
    private BigDecimal initialSecurityPrice;
    private long quantityLots;
    private String figi;
    private OrderDirection direction;
    private OrderType type;
    private String orderId;

    @Tolerate
    public PostOrderResponseBuilder setCurrency(final Currency currency) {
        return setCurrency(currency.name());
    }

    @Tolerate
    public PostOrderResponseBuilder setTotalOrderAmount(final double totalOrderAmount) {
        return setTotalOrderAmount(DecimalUtils.setDefaultScale(totalOrderAmount));
    }

    @Tolerate
    public PostOrderResponseBuilder setTotalCommissionAmount(final double totalCommissionAmount) {
        return setTotalCommissionAmount(DecimalUtils.setDefaultScale(totalCommissionAmount));
    }

    @Tolerate
    public PostOrderResponseBuilder setInitialSecurityPrice(final double initialSecurityPrice) {
        return setInitialSecurityPrice(DecimalUtils.setDefaultScale(initialSecurityPrice));
    }

    public PostOrderResponse build() {
        final MoneyValue orderPrice = DataStructsHelper.createMoneyValue(currency, totalOrderAmount.add(totalCommissionAmount));
        final MoneyValue commission = DataStructsHelper.createMoneyValue(Currency.valueOfIgnoreCase(currency), totalCommissionAmount);
        final PostOrderResponse.Builder builder = PostOrderResponse.newBuilder()
                .setExecutionReportStatus(OrderExecutionReportStatus.EXECUTION_REPORT_STATUS_FILL)
                .setLotsRequested(quantityLots)
                .setLotsExecuted(quantityLots)
                .setInitialOrderPrice(orderPrice)
                .setExecutedOrderPrice(orderPrice)
                .setTotalOrderAmount(DataStructsHelper.createMoneyValue(currency, totalOrderAmount))
                .setInitialCommission(commission)
                .setExecutedCommission(commission)
                .setFigi(figi)
                .setDirection(direction)
                .setInitialSecurityPrice(DataStructsHelper.createMoneyValue(currency, initialSecurityPrice))
                .setOrderType(type);
        if (orderId != null) {
            builder.setOrderId(orderId);
        }
        return builder.build();
    }
}