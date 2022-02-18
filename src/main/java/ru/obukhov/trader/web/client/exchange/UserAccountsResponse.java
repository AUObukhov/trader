package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.UserAccounts;

@Data
public class UserAccountsResponse {

    private String trackingId;

    private String status;

    private UserAccounts payload;

}