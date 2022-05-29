package ru.obukhov.trader.market.model.transform;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class AccountMapperUnitTest {

    private final AccountMapper accountMapper = Mappers.getMapper(AccountMapper.class);

    @Test
    void map() {
        final String id = "2008941383";
        final AccountType accountType = AccountType.ACCOUNT_TYPE_TINKOFF_IIS;
        final String name = "ИИС";
        final AccountStatus accountStatus = AccountStatus.ACCOUNT_STATUS_OPEN;
        final Timestamp openedDate = DateTimeTestData.createTimestamp(1562889600);
        final OffsetDateTime openedDateTime = DateTimeTestData.createDateTime(2019, 7, 12, 3, ZoneOffset.ofHours(3));
        final Timestamp closedDate = DateTimeTestData.createTimestamp(-10800);
        final OffsetDateTime closedDateTime = DateTimeTestData.createDateTime(1970, 1, 1, ZoneOffset.ofHours(3));
        final AccessLevel accessLevel = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

        final Account account = Account.newBuilder()
                .setId(id)
                .setType(accountType)
                .setName(name)
                .setStatus(accountStatus)
                .setOpenedDate(openedDate)
                .setClosedDate(closedDate)
                .setAccessLevel(accessLevel)
                .build();

        final UserAccount expectedUserAccount = new UserAccount(id, accountType, name, accountStatus, openedDateTime, closedDateTime, accessLevel);

        final UserAccount userAccount = accountMapper.map(account);

        Assertions.assertEquals(expectedUserAccount, userAccount);
    }


}