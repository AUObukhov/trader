package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.Account;
import ru.obukhov.trader.test.utils.Mocker;
import ru.obukhov.trader.test.utils.model.account.TestAccount;
import ru.obukhov.trader.test.utils.model.account.TestAccounts;

import java.util.List;

class UserControllerIntegrationTest extends ControllerIntegrationTest {

    @BeforeEach
    void init() {
        Mockito.reset(usersService);
    }

    @Test
    void getAccounts() throws Exception {
        final TestAccount testAccount1 = TestAccounts.IIS;
        final TestAccount testAccount2 = TestAccounts.TINKOFF;

        Mocker.mockAccounts(usersService, testAccount1, testAccount2);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/user/accounts")
                .contentType(MediaType.APPLICATION_JSON);
        final List<Account> expectedAccounts = List.of(testAccount1.account(), testAccount2.account());
        assertResponse(requestBuilder, expectedAccounts);
    }

}