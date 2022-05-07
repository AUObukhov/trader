package ru.obukhov.trader.market.model;

import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;

import java.time.OffsetDateTime;

public record UserAccount(
        String id,
        AccountType type,
        String name,
        AccountStatus status,
        OffsetDateTime openedDate,
        OffsetDateTime closedDate,
        AccessLevel accessLevel
) {
}