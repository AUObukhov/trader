package ru.obukhov.trader.web.client.service.interfaces;

import ru.obukhov.trader.market.model.UserAccount;

import java.io.IOException;
import java.util.List;

public interface UserClient {

    List<UserAccount> getAccounts() throws IOException;

}
