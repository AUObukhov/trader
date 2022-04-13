package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.obukhov.trader.market.model.BrokerAccountType;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.web.model.exchange.GetUserAccountsResponse;

import java.util.List;

class UserControllerIntegrationTest extends ControllerIntegrationTest {

    @Test
    @SuppressWarnings("java:S2699")
        // Sonar warning "Tests should include assertions"
    void getAccounts() throws Exception {
        final UserAccount userAccount1 = new UserAccount();
        userAccount1.setBrokerAccountType(BrokerAccountType.TINKOFF_IIS);
        userAccount1.setBrokerAccountId("2008941383");

        final UserAccount userAccount2 = new UserAccount();
        userAccount2.setBrokerAccountType(BrokerAccountType.TINKOFF);
        userAccount2.setBrokerAccountId("2000124699");

        final List<UserAccount> userAccounts = List.of(userAccount1, userAccount2);

        mockUserAccounts(userAccounts);

        final GetUserAccountsResponse getUserAccountsResponse = new GetUserAccountsResponse(userAccounts);

        final MockHttpServletRequestBuilder requestBuilder = MockMvcRequestBuilders.get("/trader/user/accounts")
                .contentType(MediaType.APPLICATION_JSON);
        performAndExpectResponse(requestBuilder, getUserAccountsResponse);
    }

}