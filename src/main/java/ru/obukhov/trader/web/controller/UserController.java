package ru.obukhov.trader.web.controller;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.market.model.UserAccount;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/trader/user")
@SuppressWarnings("unused")
public class UserController {

    private final ExtUsersService extUsersService;

    public UserController(final ExtUsersService extUsersService) {
        this.extUsersService = extUsersService;
    }

    @GetMapping("/accounts")
    @ApiOperation("Get user accounts")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK"),
            @ApiResponse(code = 500, message = "Internal Server Error")
    })
    public List<UserAccount> getAccounts() {
        return extUsersService.getAccounts();
    }

}