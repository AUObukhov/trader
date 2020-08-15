package ru.obukhov.investor.web.model;

import lombok.Builder;
import lombok.Data;
import ru.obukhov.investor.model.TickerType;

import java.time.OffsetDateTime;

@Data
@Builder
public class GetStatisticsRequest {
    private String token;
    private String ticker;
    private TickerType tickerType;
    private OffsetDateTime from;
    private OffsetDateTime to;
}