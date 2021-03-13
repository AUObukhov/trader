package ru.obukhov.trader.web.model.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.TickerType;

@Data
public class GetInstrumentsRequest {

    private TickerType tickerType;

}
