package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.interfaces.ExtOperationsService;
import ru.obukhov.trader.market.model.PortfolioPosition;
import ru.tinkoff.piapi.core.models.Money;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/portfolio")
@AllArgsConstructor
@SuppressWarnings("unused")
public class OperationsController {

    private final ExtOperationsService extOperationsService;

    @GetMapping("/positions")
    @ApiOperation("Get positions of portfolio")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<PortfolioPosition> getPositions(@RequestParam @ApiParam(example = "2008941383") final String accountId) {
        return extOperationsService.getPositions(accountId);
    }

    @GetMapping("/balances")
    @ApiOperation("Get balances of portfolio")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 400, message = "Bad Request"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<Money> getAvailableBalances(@RequestParam @ApiParam(example = "2008941383") final String accountId) {
        return extOperationsService.getAvailableBalances(accountId);
    }

}