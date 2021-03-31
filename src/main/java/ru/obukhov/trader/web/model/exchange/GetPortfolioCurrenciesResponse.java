package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.invest.openapi.model.rest.CurrencyPosition;

import java.util.List;

@Data
@AllArgsConstructor
public class GetPortfolioCurrenciesResponse {

    private List<CurrencyPosition> currencies;

}