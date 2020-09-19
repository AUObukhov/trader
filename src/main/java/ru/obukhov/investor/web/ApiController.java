package ru.obukhov.investor.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.StatisticsService;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.obukhov.investor.web.model.GetCandlesResponse;
import ru.obukhov.investor.web.model.GetDailySaldosRequest;
import ru.obukhov.investor.web.model.GetInstrumentsRequest;
import ru.obukhov.investor.web.model.GetInstrumentsResponse;
import ru.obukhov.investor.web.model.GetSaldosRequest;
import ru.obukhov.investor.web.model.GetSaldosResponse;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/investor/v1")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;

    @GetMapping("/candles")
    public GetCandlesResponse getCandles(@RequestBody GetCandlesRequest request) {

        List<Candle> candles = statisticsService.getCandles(
                request.getTicker(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval());

        return new GetCandlesResponse(candles);

    }

    @GetMapping("/saldos/daily")
    public GetSaldosResponse getDailySaldos(@Valid @RequestBody GetDailySaldosRequest request) {

        Map<Object, BigDecimal> saldosByTimes = statisticsService.getDailySaldos(
                request.getTicker(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval());

        return new GetSaldosResponse(saldosByTimes);
    }

    @GetMapping("/saldos/weekly")
    public GetSaldosResponse getWeeklySaldos(@Valid @RequestBody GetSaldosRequest request) {

        Map<Object, BigDecimal> saldosByDaysOfWeek = statisticsService.getWeeklySaldos(
                request.getTicker(),
                request.getFrom(),
                request.getTo());

        return new GetSaldosResponse(saldosByDaysOfWeek);
    }

    @GetMapping("/saldos/monthly")
    public GetSaldosResponse getMonthlySaldos(@Valid @RequestBody GetSaldosRequest request) {

        Map<Object, BigDecimal> saldosByDaysOfMonth = statisticsService.getMonthlySaldos(
                request.getTicker(),
                request.getFrom(),
                request.getTo());

        return new GetSaldosResponse(saldosByDaysOfMonth);
    }

    @GetMapping("/saldos/yearly")
    public GetSaldosResponse getYearlySaldos(@Valid @RequestBody GetSaldosRequest request) {

        Map<Object, BigDecimal> saldosByYear = statisticsService.getYearlySaldos(
                request.getTicker(),
                request.getFrom(),
                request.getTo());

        return new GetSaldosResponse(saldosByYear);
    }

    @GetMapping("/instruments")
    public GetInstrumentsResponse getInstruments(@Valid @RequestBody GetInstrumentsRequest request) {
        List<Instrument> instruments = statisticsService.getInstruments(request.getTickerType());

        return new GetInstrumentsResponse(instruments);
    }
}