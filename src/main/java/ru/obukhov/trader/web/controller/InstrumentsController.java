package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.interfaces.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.CurrencyInstrument;
import ru.obukhov.trader.market.model.Etf;
import ru.obukhov.trader.market.model.Instrument;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.market.model.TradingDay;
import ru.obukhov.trader.market.model.TradingSchedule;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/instruments")
@AllArgsConstructor
@SuppressWarnings("unused")
public class InstrumentsController {

    private final ExtInstrumentsService extInstrumentsService;

    @GetMapping("/instrument")
    @ApiOperation("Get instrument info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public Instrument getInstrument(@RequestParam @ApiParam(example = "AAPBBG000B9XRY4L") final String figi) {
        return extInstrumentsService.getInstrument(figi);
    }

    @GetMapping("/share")
    @ApiOperation("Get share info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public Share getShare(@RequestParam @ApiParam(example = "BBG000B9XRY4") final String figi) {
        return extInstrumentsService.getShare(figi);
    }

    @GetMapping("/etf")
    @ApiOperation("Get ETF info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public Etf getEtf(@RequestParam @ApiParam(example = "BBG005HLSZ23") final String figi) {
        return extInstrumentsService.getEtf(figi);
    }

    @GetMapping("/bond")
    @ApiOperation("Get bond info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public Bond getBond(@RequestParam @ApiParam(example = "RU000A0ZYG52") final String figi) {
        return extInstrumentsService.getBond(figi);
    }

    @GetMapping("/currency")
    @ApiOperation("Get currency info")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public CurrencyInstrument getCurrency(@RequestParam @ApiParam(example = "BBG0013HGFT4") final String figi) {
        return extInstrumentsService.getCurrency(figi);
    }

    @GetMapping("/trading-schedule")
    @ApiOperation("Get trading schedule for exchange")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<TradingDay> getTradingSchedule(
            @RequestParam @ApiParam(example = "SPB") final String exchange,
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