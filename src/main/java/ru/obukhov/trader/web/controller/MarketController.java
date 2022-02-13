package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.impl.MarketService;
import ru.obukhov.trader.market.model.InstrumentType;
import ru.obukhov.trader.market.model.MarketInstrument;
import ru.obukhov.trader.web.model.exchange.GetInstrumentsResponse;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/market")
@RequiredArgsConstructor
public class MarketController {

    private final MarketService marketService;

    @GetMapping("/instruments")
    @ApiOperation("Get instruments by given criteria")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public GetInstrumentsResponse getInstruments(
            @ApiParam(value = "Instrument type. If null all instruments are returned", example = "Stock") InstrumentType instrumentType
    ) throws IOException {
        final List<MarketInstrument> instruments = marketService.getInstruments(instrumentType);

        return new GetInstrumentsResponse(instruments);
    }

}