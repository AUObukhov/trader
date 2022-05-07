package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.tinkoff.piapi.contract.v1.Account;

import java.util.List;

@Data
public class UserAccountsResponse {

    private List<Account> accounts;

}