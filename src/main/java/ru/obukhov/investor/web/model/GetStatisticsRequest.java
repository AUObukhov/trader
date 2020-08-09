package ru.obukhov.investor.web.model;

import lombok.Data;
import ru.obukhov.investor.model.TickerType;

@Data
public class GetStatisticsRequest {
    private String token;
    private String ticker;
    private TickerType tickerType;
}