package ru.obukhov.trader.web.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.common.model.Interval;
import ru.obukhov.trader.market.impl.ExtInstrumentsService;
import ru.obukhov.trader.market.model.Bond;
import ru.obukhov.trader.market.model.Currency;
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
    public Instrument getInstrument(@RequestParam final String figi) {
        return extInstrumentsService.getInstrument(figi);
    }

    @GetMapping("/share")
    public Share getShare(@RequestParam final String figi) {
        return extInstrumentsService.getShare(figi);
    }

    @GetMapping("/etf")
    public Etf getEtf(@RequestParam final String figi) {
        return extInstrumentsService.getEtf(figi);
    }

    @GetMapping("/bond")
    public Bond getBond(@RequestParam final String figi) {
        return extInstrumentsService.getBond(figi);
    }

    @GetMapping("/currency")
    public Currency getCurrency(@RequestParam final String figi) {
        return extInstrumentsService.getCurrencyByFigi(figi);
    }

    @GetMapping("/trading-schedule")
    public List<TradingDay> getTradingSchedule(@RequestParam final String exchange, @Valid final Interval interval) {
        return extInstrumentsService.getTradingSchedule(exchange, interval);
    }

    @GetMapping("/trading-schedules")
    public List<TradingSchedule> getTradingSchedules(@Valid final Interval interval) {
        return extInstrumentsService.getTradingSchedules(interval);
    }

}