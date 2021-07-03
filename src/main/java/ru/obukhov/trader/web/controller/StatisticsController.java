package ru.obukhov.trader.web.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.interfaces.StatisticsService;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.obukhov.trader.web.model.exchange.GetInstrumentsRequest;
import ru.obukhov.trader.web.model.exchange.GetInstrumentsResponse;
import ru.tinkoff.invest.openapi.model.rest.CandleResolution;
import ru.tinkoff.invest.openapi.model.rest.MarketInstrument;

import javax.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final ExcelService excelService;

    @GetMapping("/candles")
    public GetCandlesResponse getCandles(
            @RequestParam final String ticker,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) final OffsetDateTime to,
            @RequestParam final CandleResolution candleResolution,
            @RequestParam(required = false, defaultValue = "false") final boolean saveToFile
    ) {
        final Interval interval = DateUtils.getIntervalWithDefaultOffsets(from, to);
        final GetCandlesResponse response = statisticsService.getExtendedCandles(
                ticker,
                interval,
                candleResolution
        );

        if (saveToFile) {
            saveCandlesSafe(ticker, interval, response);
        }

        return response;
    }

    private void saveCandlesSafe(final String ticker, final Interval interval, final GetCandlesResponse response) {
        try {
            log.debug("Saving candles for ticker {} to file", ticker);
            excelService.saveCandles(ticker, interval, response);
        } catch (RuntimeException exception) {
            log.error("Failed to save candles for ticker {} to file", ticker, exception);
        }
    }

    @GetMapping("/instruments")
    public GetInstrumentsResponse getInstruments(@Valid @RequestBody final GetInstrumentsRequest request) {
        List<MarketInstrument> instruments = statisticsService.getInstruments(request.getTickerType());

        return new GetInstrumentsResponse(instruments);
    }

}