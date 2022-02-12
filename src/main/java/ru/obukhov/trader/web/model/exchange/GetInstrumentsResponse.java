package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.market.model.MarketInstrument;

import java.util.List;

@Data
@AllArgsConstructor
public class GetInstrumentsResponse {

    private List<MarketInstrument> instruments;

}
