package ru.tinkoff.invest.openapi.okhttp;

import ru.obukhov.trader.market.model.UserAccount;

import java.io.IOException;
import java.util.List;

public interface UserContext extends Context {

    List<UserAccount> getAccounts() throws IOException;

}
