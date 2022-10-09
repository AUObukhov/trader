package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Exchange;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/instruments")
@AllArgsConstructor
@SuppressWarnings("unused")
public class InstrumentsController {

    private final ExtInstrumentsService extInstrumentsService;

    @GetMapping("/shares")
    @ApiOperation("Get shares info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<Share> getShares(@RequestParam @ApiParam(example = "AAPL") final String ticker) {
        return extInstrumentsService.getShares(ticker);
    }

    @GetMapping("/share")
    @ApiOperation("Get single share info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public Share getSingleShare(@RequestParam @ApiParam(example = "AAPL") final String ticker) {
        return extInstrumentsService.getSingleShare(ticker);
    }

    @GetMapping("/etfs")
    @ApiOperation("Get ETFs info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<Etf> getEtfs(@RequestParam @ApiParam(example = "FXIT") final String ticker) {
        return extInstrumentsService.getEtfs(ticker);
    }

    @GetMapping("/etf")
    @ApiOperation("Get single ETF info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public Etf getSingleEtf(@RequestParam @ApiParam(example = "FXIT") final String ticker) {
        return extInstrumentsService.getSingleEtf(ticker);
    }

    @GetMapping("/trading-schedule")
    @ApiOperation("Get trading schedule for exchange")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<TradingDay> getTradingSchedule(
            @RequestParam @ApiParam(example = "SPB") final Exchange exchange,
            @Valid @RequestBody final Interval interval
    ) {
        return extInstrumentsService.getTradingSchedule(exchange, interval);
    }

    @GetMapping("/trading-schedules")
    @ApiOperation("Get trading schedules")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<TradingSchedule> getTradingSchedules(@Valid @RequestBody final Interval interval) {
        return extInstrumentsService.getTradingSchedules(interval);
    }

}