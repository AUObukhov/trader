package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.market.model.CurrencyPosition;

import java.util.List;

@Data
@AllArgsConstructor
public class GetPortfolioCurrenciesResponse {

    private List<CurrencyPosition> currencies;

}