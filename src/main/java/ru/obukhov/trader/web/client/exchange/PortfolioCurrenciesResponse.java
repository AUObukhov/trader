package ru.obukhov.trader.web.client.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import ru.obukhov.trader.market.model.CurrencyPosition;

import java.util.List;

@Data
public class PortfolioCurrenciesResponse {

    private String trackingId;

    private String status;

    @JsonProperty("payload.currencies")
    private List<CurrencyPosition> currencies;

}