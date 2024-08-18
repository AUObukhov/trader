package ru.obukhov.trader.test.utils.model.account;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Account;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;

public record TestAccount(Account account, ru.tinkoff.piapi.contract.v1.Account tAccount) {

    TestAccount(final Account account) {
        this(account, buildTAccount(account));
    }

    private static ru.tinkoff.piapi.contract.v1.Account buildTAccount(final Account account) {
        final DateTimeMapper dateTimeMapper = Mappers.getMapper(DateTimeMapper.class);
        return ru.tinkoff.piapi.contract.v1.Account.newBuilder()
                .setId(account.id())
                .setType(account.type())
                .setName(account.name())
                .setStatus(account.status())
                .setOpenedDate(dateTimeMapper.offsetDateTimeToTimestamp(account.openedDate()))
                .setClosedDate(dateTimeMapper.offsetDateTimeToTimestamp(account.closedDate()))
                .setAccessLevel(account.accessLevel())
                .build();
    }

    public String getId() {
        return account.id();
    }

}