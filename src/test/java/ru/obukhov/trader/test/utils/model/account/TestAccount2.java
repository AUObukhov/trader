package ru.obukhov.trader.test.utils.model.account;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;

import java.time.OffsetDateTime;

public class TestAccount2 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

    public static final String ID = "2000124699";
    public static final AccountType TYPE = AccountType.ACCOUNT_TYPE_TINKOFF;
    public static final String NAME = "Брокерский счёт";
    public static final AccountStatus STATUS = AccountStatus.ACCOUNT_STATUS_OPEN;
    public static final OffsetDateTime OPENED_DATE = DateTimeTestData.createDateTime(2018, 5, 25, 3);
    public static final OffsetDateTime CLOSED_DATE = DateTimeTestData.createDateTime(1970, 1, 1);
    public static final AccessLevel ACCESS_LEVEL = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

    public static final ru.obukhov.trader.market.model.Account ACCOUNT = new ru.obukhov.trader.market.model.Account(
            ID,
            TYPE,
            NAME,
            STATUS,
            OPENED_DATE,
            CLOSED_DATE,
            ACCESS_LEVEL
    );

    public static final Account TINKOFF_ACCOUNT = Account.newBuilder()
            .setId(ID)
            .setType(TYPE)
            .setName(NAME)
            .setStatus(STATUS)
            .setOpenedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(OPENED_DATE))
            .setClosedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(CLOSED_DATE))
            .setAccessLevel(ACCESS_LEVEL)
            .build();

    public static final String JSON_STRING = "{\"id\":\"2000124699\"," +
            "\"type\":\"ACCOUNT_TYPE_TINKOFF\"," +
            "\"name\":\"Брокерский счёт\"," +
            "\"status\":\"ACCOUNT_STATUS_OPEN\"," +
            "\"openedDate\":{\"seconds\":1527206400,\"nanos\":0}," +
            "\"closedDate\":{\"seconds\":0,\"nanos\":0}," +
            "\"accessLevel\":\"ACCOUNT_ACCESS_LEVEL_FULL_ACCESS\"}";

}