package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.market.model.CandleResolution;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;

import java.io.IOException;
import java.time.OffsetDateTime;

@Slf4j
@RestController
@RequestMapping("/trader/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final ExcelService excelService;

    @GetMapping("/candles")
    @ApiOperation("Get candles by given criteria")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public GetCandlesResponse getCandles(
            @RequestParam
            @ApiParam(example = "FXIT", required = true) final String ticker,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @ApiParam(value = "Start date time of candle search interval", example = "2019-01-01T00:00:00+03:00", required = true)
            final OffsetDateTime from,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @ApiParam(value = "End date time of candle search interval", example = "2020-01-01T00:00:00+03:00", required = true)
            final OffsetDateTime to,

            @RequestParam
            @ApiParam(value = "Candle interval", example = "1min", required = true) final CandleResolution candleResolution,

            @RequestParam
            @ApiParam(value = "Moving average algorithm type", example = "LWMA", required = true) final MovingAverageType movingAverageType,

            @RequestParam
            @ApiParam(value = "Window of short-term moving average", example = "50", required = true) final Integer smallWindow,

            @RequestParam
            @ApiParam(value = "Window of long-term moving average", example = "200", required = true) final Integer bigWindow,

            @RequestParam(required = false, defaultValue = "false")
            @ApiParam(value = "Flag indicating to save the back test result to a file. Default value is false", example = "true") final boolean saveToFile
    ) throws IOException {
        final Interval interval = DateUtils.getIntervalWithDefaultOffsets(from, to);
        final GetCandlesResponse response = statisticsService.getExtendedCandles(
                ticker,
                interval,
                candleResolution,
                movingAverageType,
                smallWindow,
                bigWindow
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

}