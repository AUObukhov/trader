package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.market.model.Account;
import ru.obukhov.trader.test.utils.model.account.TestAccount;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;
import ru.tinkoff.piapi.core.UsersService;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExtUsersServiceUnitTest {

    @Mock
    private UsersService usersService;

    @InjectMocks
    private ExtUsersService extUsersService;

    @Test
    void getAccounts() {
        final TestAccount testAccount1 = TestAccounts.IIS;
        final TestAccount testAccount2 = TestAccounts.TINKOFF;

        final List<ru.tinkoff.piapi.contract.v1.Account> accounts = List.of(testAccount1.tinkoffAccount(), testAccount2.tinkoffAccount());
        Mockito.when(usersService.getAccountsSync())
                .thenReturn(accounts);

        Mockito.when(usersService.getAccountsSync()).thenReturn(accounts);

        final List<Account> actualResult = extUsersService.getAccounts();

        final List<Account> expectedResult = List.of(testAccount1.account(), testAccount2.account());
        Assertions.assertEquals(expectedResult, actualResult);
    }

}