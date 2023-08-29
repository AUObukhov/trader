package ru.obukhov.trader.test.utils.model.account;

import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Account;
import ru.obukhov.trader.market.model.transform.DateTimeMapper;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;

import java.time.OffsetDateTime;

public class TestAccount1 {

    private static final DateTimeMapper DATE_TIME_MAPPER = Mappers.getMapper(DateTimeMapper.class);

    public static final String ID = "2008941383";
    public static final AccountType TYPE = AccountType.ACCOUNT_TYPE_TINKOFF_IIS;
    public static final String NAME = "ИИС";
    public static final AccountStatus STATUS = AccountStatus.ACCOUNT_STATUS_OPEN;
    public static final OffsetDateTime OPENED_DATE = DateTimeTestData.createDateTime(2019, 7, 12, 3);
    public static final OffsetDateTime CLOSED_DATE = DateTimeTestData.createDateTime(1970, 1, 1);
    public static final AccessLevel ACCESS_LEVEL = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

    public static final Account ACCOUNT = new Account(
            ID,
            TYPE,
            NAME,
            STATUS,
            OPENED_DATE,
            CLOSED_DATE,
            ACCESS_LEVEL
    );

    public static final ru.tinkoff.piapi.contract.v1.Account TINKOFF_ACCOUNT = ru.tinkoff.piapi.contract.v1.Account.newBuilder()
            .setId(ID)
            .setType(TYPE)
            .setName(NAME)
            .setStatus(STATUS)
            .setOpenedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(OPENED_DATE))
            .setClosedDate(DATE_TIME_MAPPER.offsetDateTimeToTimestamp(CLOSED_DATE))
            .setAccessLevel(ACCESS_LEVEL)
            .build();

    public static final String JSON_STRING = "{\"id\":\"2008941383\"," +
            "\"type\":\"ACCOUNT_TYPE_TINKOFF_IIS\"," +
            "\"name\":\"ИИС\"," +
            "\"status\":\"ACCOUNT_STATUS_OPEN\"," +
            "\"openedDate\":\"2019.7.12T03:00:00.000\"," +
            "\"closedDate\":\"1970.1.1T03:00:00.000\"," +
            "\"accessLevel\":\"ACCOUNT_ACCESS_LEVEL_FULL_ACCESS\"}";

}