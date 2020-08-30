package ru.obukhov.investor.web;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.investor.model.Candle;
import ru.obukhov.investor.service.InvestService;
import ru.obukhov.investor.web.model.GetCandlesRequest;
import ru.obukhov.investor.web.model.GetCandlesResponse;
import ru.obukhov.investor.web.model.GetDailySaldosRequest;
import ru.obukhov.investor.web.model.GetSaldosResponse;
import ru.obukhov.investor.web.model.GetWeeklySaldosRequest;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Log
@RestController
@RequestMapping("/investor/v1")
@RequiredArgsConstructor
public class ApiController {

    private final InvestService investService;

    @GetMapping("/candles")
    public GetCandlesResponse getCandles(@RequestBody GetCandlesRequest request) {

        List<Candle> candles = investService.getCandles(
                request.getToken(),
                request.getTicker(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval());

        return new GetCandlesResponse(candles);

    }

    @GetMapping("/saldos/daily")
    public GetSaldosResponse getDailySaldos(@Valid @RequestBody GetDailySaldosRequest request) {

        Map<LocalTime, BigDecimal> saldosByTimes = investService.getDailySaldos(request.getToken(),
                request.getTicker(),
                request.getFrom(),
                request.getTo(),
                request.getCandleInterval());

        return new GetSaldosResponse(saldosByTimes);
    }

    @GetMapping("/saldos/weekly")
    public GetSaldosResponse getWeeklySaldos(@Valid @RequestBody GetWeeklySaldosRequest request) {

        Map<DayOfWeek, BigDecimal> saldosByDaysOfWeek = investService.getWeeklySaldos(request.getToken(),
                request.getTicker(),
                request.getFrom(),
                request.getTo());

        return new GetSaldosResponse(saldosByDaysOfWeek);
    }
}