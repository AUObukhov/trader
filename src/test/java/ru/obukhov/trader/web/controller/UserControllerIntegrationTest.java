package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.test.utils.model.account.TestAccount1;
import ru.obukhov.trader.test.utils.model.account.TestAccount2;
import ru.tinkoff.piapi.contract.v1.Account;

import java.util.List;

class UserControllerIntegrationTest extends ControllerIntegrationTest {

    @BeforeEach
    void init() {
        Mockito.reset(usersService);
    }

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getAccounts() throws Exception {
        final List<Account> accounts = List.of(TestAccount1.ACCOUNT, TestAccount2.ACCOUNT);
        Mockito.when(usersService.getAccountsSync())
                .thenReturn(accounts);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/user/accounts")
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder, accounts);
    }

}