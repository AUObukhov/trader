package ru.obukhov.trader.web.controller;

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
import ru.obukhov.trader.web.model.SharesFiltrationOptions;
import ru.obukhov.trader.web.model.exchange.FigiesListRequest;
import ru.obukhov.trader.web.model.exchange.GetCandlesResponse;
import ru.tinkoff.piapi.contract.v1.CandleInterval;

import java.math.BigDecimal;
import java.util.Map;
import java.util.SequencedMap;

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
    public GetCandlesResponse getCandles(
            @NotNull(message = "figi is mandatory") final String figi,

            @Validated
            @NotNull(message = "interval is mandatory") final Interval interval,

            @NotNull(message = "candleInterval is mandatory") final CandleInterval candleInterval,

            @NotNull(message = "movingAverageType is mandatory") final MovingAverageType movingAverageType,

            @NotNull(message = "smallWindow is mandatory") final Integer smallWindow,

            @NotNull(message = "bigWindow is mandatory") final Integer bigWindow,

            final boolean saveToFile
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
    public Map<String, BigDecimal> getCapitalizationWeights(@Valid @RequestBody final FigiesListRequest figiesListRequest) {
        return statisticsService.getCapitalizationWeights(figiesListRequest.getFigies());
    }

    @GetMapping("/most-profitable-shares")
    public SequencedMap<String, Double> getMostProfitableShares(@RequestBody final SharesFiltrationOptions filtrationOptions) {
        return statisticsService.getMostProfitableShares(filtrationOptions);
    }
}