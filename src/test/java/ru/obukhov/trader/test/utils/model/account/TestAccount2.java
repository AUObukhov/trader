package ru.obukhov.trader.test.utils.model.account;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;

public class TestAccount2 {

    public static final String ID = "2000124699";
    public static final AccountType TYPE = AccountType.ACCOUNT_TYPE_TINKOFF;
    public static final String NAME = "Брокерский счёт";
    public static final AccountStatus STATUS = AccountStatus.ACCOUNT_STATUS_OPEN;
    public static final Timestamp OPENED_DATE = TimestampUtils.newTimestamp(2018, 5, 25, 3);
    public static final Timestamp CLOSED_DATE = TimestampUtils.newTimestamp(0L, 0);
    public static final AccessLevel ACCESS_LEVEL = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

    public static final Account ACCOUNT = Account.newBuilder()
            .setId(ID)
            .setType(TYPE)
            .setName(NAME)
            .setStatus(STATUS)
            .setOpenedDate(OPENED_DATE)
            .setClosedDate(CLOSED_DATE)
            .setAccessLevel(ACCESS_LEVEL)
            .build();

    public static final String STRING = "{\"id\":\"2000124699\"," +
            "\"type\":\"ACCOUNT_TYPE_TINKOFF\"," +
            "\"name\":\"Брокерский счёт\"," +
            "\"status\":\"ACCOUNT_STATUS_OPEN\"," +
            "\"openedDate\":{\"seconds\":1527206400,\"nanos\":0}," +
            "\"closedDate\":{\"seconds\":0,\"nanos\":0}," +
            "\"accessLevel\":\"ACCOUNT_ACCESS_LEVEL_FULL_ACCESS\"}";

}