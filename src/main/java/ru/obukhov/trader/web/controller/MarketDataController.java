package ru.obukhov.trader.web.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.impl.ExtMarketDataService;
import ru.obukhov.trader.web.model.exchange.FigiesListRequest;
import ru.tinkoff.piapi.contract.v1.SecurityTradingStatus;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/trader/market")
@AllArgsConstructor
@SuppressWarnings("unused")
public class MarketDataController {

    private final ExtMarketDataService extMarketDataService;

    @GetMapping("/status")
    public SecurityTradingStatus getTradingStatus(@RequestParam final String figi) {
        return extMarketDataService.getTradingStatus(figi);
    }

    @GetMapping("/convert-currency")
    public BigDecimal convertCurrency(
            @RequestParam final String sourceCurrencyIsoName,
            @RequestParam final String targetCurrencyIsoName,
            @RequestParam final BigDecimal sourceValue
    ) {
        return extMarketDataService.convertCurrency(sourceCurrencyIsoName, targetCurrencyIsoName, sourceValue);
    }

    @GetMapping("/last-prices")
    public Map<String, BigDecimal> getLastPrices(@Valid @RequestBody final FigiesListRequest figiesListRequest) {
        return extMarketDataService.getLastPrices(figiesListRequest.getFigies());
    }

}