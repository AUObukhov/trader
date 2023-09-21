package ru.obukhov.trader.test.utils.model.account;

import ru.obukhov.trader.market.model.Account;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;

public class TestAccounts {

    private static final Account IIS_ACCOUNT = new Account(
            "2008941383",
            AccountType.ACCOUNT_TYPE_TINKOFF_IIS,
            "ИИС",
            AccountStatus.ACCOUNT_STATUS_OPEN,
            DateTimeTestData.newDateTime(2019, 7, 12, 3),
            DateTimeTestData.newDateTime(1970, 1, 1),
            AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS
    );
    private static final Account TINKOFF_ACCOUNT = new Account(
            "2000124699",
            AccountType.ACCOUNT_TYPE_TINKOFF,
            "Брокерский счёт",
            AccountStatus.ACCOUNT_STATUS_OPEN,
            DateTimeTestData.newDateTime(2018, 5, 25, 3),
            DateTimeTestData.newDateTime(1970, 1, 1),
            AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS
    );

    public static final TestAccount IIS = new TestAccount(IIS_ACCOUNT);
    public static final TestAccount TINKOFF = new TestAccount(TINKOFF_ACCOUNT);

}