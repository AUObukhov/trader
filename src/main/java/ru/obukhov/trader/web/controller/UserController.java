package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.impl.UserService;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.web.model.exchange.GetUserAccountsResponse;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/user")
@SuppressWarnings("unused")
public class UserController {

    private final UserService userService;

    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/accounts")
    @ApiOperation("Get user accounts")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public GetUserAccountsResponse getAccounts() throws IOException {
        final List<UserAccount> accounts = userService.getAccounts();

        return new GetUserAccountsResponse(accounts);
    }

}