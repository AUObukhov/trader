package ru.obukhov.investor.web.model;

import lombok.Data;
import ru.obukhov.investor.model.TickerType;

@Data
public class GetInstrumentsRequest {

    private TickerType tickerType;

}
