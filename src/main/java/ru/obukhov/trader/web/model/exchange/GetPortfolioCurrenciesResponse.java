package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.portfolio.PortfolioCurrencies;

import java.util.List;

@Data
@AllArgsConstructor
public class GetPortfolioCurrenciesResponse {

    private List<PortfolioCurrencies.PortfolioCurrency> currencies;

}