package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.market.model.UserAccount;

import java.util.List;

@Data
@AllArgsConstructor
public class GetUserAccountsResponse {

    private final List<UserAccount> accounts;

}