package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.market.model.Candle;
import ru.tinkoff.piapi.contract.v1.Quotation;

import java.util.List;

@Data
@AllArgsConstructor
public class GetCandlesResponse {

    private List<Candle> candles;

    private List<Quotation> averages1;

    private List<Quotation> averages2;

}