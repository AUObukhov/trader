package ru.obukhov.investor.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.service.InvestService;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.obukhov.investor.web.model.GetCandlesResponse;
import ru.obukhov.investor.web.model.GetStatisticsRequest;
import ru.tinkoff.invest.openapi.models.market.Candle;

import java.util.List;

@Log
@RestController
@RequestMapping("/investor/v1")
@RequiredArgsConstructor
public class ApiController {

    private final InvestService investService;

    @GetMapping("/candles")
    public GetCandlesResponse getCandles(@RequestBody GetCandlesRequest request) {

        List<Candle> candles = investService.getCandles(request);

        return new GetCandlesResponse(candles);

    }

    @GetMapping("/statistics")
    public void getStatistics(@RequestBody GetStatisticsRequest request) {
        investService.getStatistics(request);
    }
}