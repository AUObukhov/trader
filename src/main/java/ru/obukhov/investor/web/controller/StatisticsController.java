package ru.obukhov.investor.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.common.model.Interval;
import ru.obukhov.investor.common.util.DateUtils;
import ru.obukhov.investor.market.interfaces.StatisticsService;
import ru.obukhov.investor.market.model.Candle;
import ru.obukhov.investor.web.model.exchange.GetCandlesRequest;
import ru.obukhov.investor.web.model.exchange.GetCandlesResponse;
import ru.obukhov.investor.web.model.exchange.GetInstrumentsRequest;
import ru.obukhov.investor.web.model.exchange.GetInstrumentsResponse;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/investor/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/candles")
    public GetCandlesResponse getCandles(@RequestBody GetCandlesRequest request) {

        Interval interval = DateUtils.getIntervalWithDefaultOffsets(request.getFrom(), request.getTo());
        List<Candle> candles = statisticsService.getCandles(request.getTicker(), interval, request.getCandleInterval());

        return new GetCandlesResponse(candles);

    }

    @GetMapping("/instruments")
    public GetInstrumentsResponse getInstruments(@Valid @RequestBody GetInstrumentsRequest request) {

        List<Instrument> instruments = statisticsService.getInstruments(request.getTickerType());

        return new GetInstrumentsResponse(instruments);

    }

}