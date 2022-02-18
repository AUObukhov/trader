package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.Currencies;

@Data
public class PortfolioCurrenciesResponse {

    private String trackingId;

    private String status;

    private Currencies payload;

}