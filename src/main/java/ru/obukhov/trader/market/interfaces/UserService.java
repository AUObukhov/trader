package ru.obukhov.trader.market.interfaces;

import ru.tinkoff.invest.openapi.model.rest.UserAccount;

import java.util.List;

public interface UserService {

    List<UserAccount> getAccounts();

}