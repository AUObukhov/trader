package ru.obukhov.trader.test.utils.model.account;

import ru.obukhov.trader.market.model.Account;
import ru.obukhov.trader.test.utils.ResourceUtils;

public class TestAccounts {

    public static final TestAccount IIS = readAccount("iis.json");
    public static final TestAccount TINKOFF = readAccount("tinkoff.json");

    private static TestAccount readAccount(final String fileName) {
        final Account account = ResourceUtils.getResourceAsObject("accounts/" + fileName, Account.class);
        return new TestAccount(account);
    }

}