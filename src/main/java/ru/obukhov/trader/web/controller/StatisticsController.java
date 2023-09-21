package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.common.service.interfaces.ExcelService;
import ru.obukhov.trader.market.impl.StatisticsService;
import ru.obukhov.trader.market.model.MovingAverageType;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.obukhov.trader.web.model.exchange.GetWeightsRequest;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/trader/statistics")
@RequiredArgsConstructor
@SuppressWarnings("unused")
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
            @NotNull(message = "figi is mandatory")
            @ApiParam(value = "Financial Instrument Global Identifier", required = true, example = "BBG000B9XRY4") final String figi,

            @Validated
            @NotNull(message = "interval is mandatory") final Interval interval,

            @NotNull(message = "candleInterval is mandatory")
            @ApiParam(value = "Candle interval", required = true, example = "CANDLE_INTERVAL_1_MIN") final CandleInterval candleInterval,

            @NotNull(message = "movingAverageType is mandatory")
            @ApiParam(value = "Moving average algorithm type", required = true, example = "LWMA") final MovingAverageType movingAverageType,

            @NotNull(message = "smallWindow is mandatory")
            @ApiParam(value = "Window of short-term moving average", required = true, example = "50") final Integer smallWindow,

            @NotNull(message = "bigWindow is mandatory")
            @ApiParam(value = "Window of long-term moving average", required = true, example = "200") final Integer bigWindow,

            @ApiParam(value = "Flag of saving the back test result to a file. Default value is false", example = "true") final boolean saveToFile
    ) {
        final GetCandlesResponse response =
                statisticsService.getExtendedCandles(figi, interval, candleInterval, movingAverageType, smallWindow, bigWindow);

        if (saveToFile) {
            saveCandlesSafe(figi, interval, response);
        }

        return response;
    }

    private void saveCandlesSafe(final String figi, final Interval interval, final GetCandlesResponse response) {
        try {
            log.debug("Saving candles for FIGI {} to file", figi);
            excelService.saveCandles(figi, interval, response);
        } catch (RuntimeException exception) {
            log.error("Failed to save candles for FIGI {} to file", figi, exception);
        }
    }

    @GetMapping("/capitalization-weights")
    @ApiOperation("Get improvised index weights proportional to shares capitalizations")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public Map<String, BigDecimal> getCapitalizationWeights(@Valid @RequestBody final GetWeightsRequest getWeightsRequest) {
        return statisticsService.getCapitalizationWeights(getWeightsRequest.getShareFigies());
    }

}