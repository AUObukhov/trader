package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.tinkoff.piapi.core.models.Portfolio;

@Data
public class PortfolioResponse {

    private String trackingId;

    private String status;

    private Portfolio payload;

}