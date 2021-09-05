package ru.obukhov.trader.web.controller;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import ru.obukhov.trader.market.interfaces.UserService;
import ru.obukhov.trader.test.utils.ResourceUtils;
import ru.tinkoff.invest.openapi.model.rest.BrokerAccountType;
import ru.tinkoff.invest.openapi.model.rest.UserAccount;

import java.util.List;

class UserControllerWebTest extends ControllerWebTest {

    @MockBean
    private UserService userService;

    @Test
    void getAccounts() throws Exception {
        final UserAccount userAccount1 = new UserAccount();
        userAccount1.setBrokerAccountType(BrokerAccountType.TINKOFFIIS);
        userAccount1.setBrokerAccountId("2008941383");

        final UserAccount userAccount2 = new UserAccount();
        userAccount2.setBrokerAccountType(BrokerAccountType.TINKOFF);
        userAccount2.setBrokerAccountId("2000124699");

        Mockito.when(userService.getAccounts()).thenReturn(List.of(userAccount1, userAccount2));

        final String expectedResponse = ResourceUtils.getTestDataAsString("GetUserAccountsResponse.json");

        mockMvc.perform(MockMvcRequestBuilders.get("/trader/user/accounts")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content().json(expectedResponse));

        Mockito.verify(userService, Mockito.times(1)).getAccounts();
    }

}