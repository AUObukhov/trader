package ru.obukhov.trader.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.obukhov.trader.market.impl.ExtUsersService;
import ru.obukhov.trader.market.model.Account;

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
    public List<Account> getAccounts() {
        return extUsersService.getAccounts();
    }

}