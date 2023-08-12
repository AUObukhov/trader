package ru.obukhov.trader.market.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.test.utils.model.account.TestAccount1;
import ru.obukhov.trader.test.utils.model.account.TestAccount2;
import ru.tinkoff.piapi.contract.v1.Account;
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
        final List<Account> accounts = List.of(TestAccount1.ACCOUNT, TestAccount2.ACCOUNT);
        Mockito.when(usersService.getAccountsSync())
                .thenReturn(accounts);

        Mockito.when(usersService.getAccountsSync()).thenReturn(accounts);

        final List<Account> result = extUsersService.getAccounts();

        Assertions.assertEquals(accounts, result);
    }

}