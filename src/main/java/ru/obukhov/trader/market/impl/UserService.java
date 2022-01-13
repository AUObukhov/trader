package ru.obukhov.trader.market.impl;

import org.springframework.stereotype.Service;
import ru.obukhov.trader.market.interfaces.TinkoffService;
import ru.tinkoff.invest.openapi.model.rest.UserAccount;

import java.util.List;

/**
 * Service to get information about customer accounts
 */
@Service
public class UserService {

    private final TinkoffService tinkoffService;

    public UserService(final TinkoffService tinkoffService) {
        this.tinkoffService = tinkoffService;
    }

    /**
     * @return list of current customer accounts. Current customer is defined by token passed to Tinkoff.
     */
    public List<UserAccount> getAccounts() {
        return tinkoffService.getAccounts();
    }

}