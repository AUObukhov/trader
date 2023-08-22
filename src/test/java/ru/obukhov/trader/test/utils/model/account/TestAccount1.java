package ru.obukhov.trader.test.utils.model.account;

import com.google.protobuf.Timestamp;
import ru.obukhov.trader.common.util.TimestampUtils;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;

public class TestAccount1 {

    public static final String ID = "2008941383";
    public static final AccountType TYPE = AccountType.ACCOUNT_TYPE_TINKOFF_IIS;
    public static final String NAME = "ИИС";
    public static final AccountStatus STATUS = AccountStatus.ACCOUNT_STATUS_OPEN;
    public static final Timestamp OPENED_DATE = TimestampUtils.newTimestamp(2019, 7, 12, 3);
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

    public static final String JSON_STRING = "{\"id\":\"2008941383\"," +
            "\"type\":\"ACCOUNT_TYPE_TINKOFF_IIS\"," +
            "\"name\":\"ИИС\"," +
            "\"status\":\"ACCOUNT_STATUS_OPEN\"," +
            "\"openedDate\":{\"seconds\":1562889600,\"nanos\":0}," +
            "\"closedDate\":{\"seconds\":0,\"nanos\":0}," +
            "\"accessLevel\":\"ACCOUNT_ACCESS_LEVEL_FULL_ACCESS\"}";

}