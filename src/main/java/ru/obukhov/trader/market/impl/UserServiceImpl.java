package ru.obukhov.trader.market.impl;

import org.springframework.stereotype.Service;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.obukhov.trader.market.interfaces.UserService;
import ru.tinkoff.invest.openapi.model.rest.UserAccount;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final TinkoffService tinkoffService;

    public UserServiceImpl(final TinkoffService tinkoffService) {
        this.tinkoffService = tinkoffService;
    }

    @Override
    public List<UserAccount> getAccounts() {
        return tinkoffService.getAccounts();
    }

}