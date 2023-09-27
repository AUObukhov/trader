package ru.obukhov.trader.web.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.tinkoff.piapi.core.models.Money;
import ru.tinkoff.piapi.core.models.Position;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/portfolio")
@AllArgsConstructor
@SuppressWarnings("unused")
public class OperationsController {

    private final ExtOperationsService extOperationsService;

    @GetMapping("/positions")
    public List<Position> getPositions(@RequestParam final String accountId) {
        return extOperationsService.getPositions(accountId);
    }

    @GetMapping("/balances")
    public List<Money> getAvailableBalances(@RequestParam final String accountId) {
        return extOperationsService.getAvailableBalances(accountId);
    }

}