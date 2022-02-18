package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.Portfolio;

@Data
public class PortfolioResponse {

    private String trackingId;

    private String status;

    private Portfolio payload;

}