package ru.obukhov.trader.test.utils.model.account;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.common.util.DateUtils;
import ru.obukhov.trader.market.model.Account;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;

public record TestAccount(Account account, ru.tinkoff.piapi.contract.v1.Account tinkoffAccount, String jsonString) {

    TestAccount(final Account account) {
        this(account, buildTinkoffAccount(account), buildJsonString(account));
    }

    private static ru.tinkoff.piapi.contract.v1.Account buildTinkoffAccount(final Account account) {
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

    private static String buildJsonString(final Account account) {
        return "{\"id\":\"" + account.id() + "\"," +
                "\"type\":\"" + account.type() + "\"," +
                "\"name\":\"" + account.name() + "\"," +
                "\"status\":\"" + account.status() + "\"," +
                "\"openedDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(account.openedDate()) + "\"," +
                "\"closedDate\":\"" + DateUtils.OFFSET_DATE_TIME_FORMATTER.format(account.closedDate()) + "\"," +
                "\"accessLevel\":\"" + account.accessLevel() + "\"}";
    }

}