package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.web.model.exchange.GetCandlesRequest;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;

import javax.validation.Valid;

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
    public GetCandlesResponse getCandles(@Valid @RequestBody final GetCandlesRequest getCandlesRequest) {
        final GetCandlesResponse response = statisticsService.getExtendedCandles(
                getCandlesRequest.getTicker(),
                getCandlesRequest.getInterval(),
                getCandlesRequest.getCandleInterval(),
                getCandlesRequest.getMovingAverageType(),
                getCandlesRequest.getSmallWindow(),
                getCandlesRequest.getBigWindow()
        );

        if (getCandlesRequest.isSaveToFile()) {
            saveCandlesSafe(getCandlesRequest.getTicker(), getCandlesRequest.getInterval(), response);
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