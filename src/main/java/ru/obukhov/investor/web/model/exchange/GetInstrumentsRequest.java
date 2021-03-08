package ru.obukhov.investor.web.model.exchange;

import lombok.Data;
import ru.obukhov.investor.market.model.TickerType;

@Data
public class GetInstrumentsRequest {

    private TickerType tickerType;

}
