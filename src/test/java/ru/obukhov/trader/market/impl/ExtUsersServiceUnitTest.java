package ru.obukhov.trader.market.impl;

import com.google.protobuf.Timestamp;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.obukhov.trader.market.model.UserAccount;
import ru.obukhov.trader.test.utils.model.DateTimeTestData;
import ru.obukhov.trader.test.utils.model.TestData;
import ru.tinkoff.piapi.contract.v1.AccessLevel;
import ru.tinkoff.piapi.contract.v1.Account;
import ru.tinkoff.piapi.contract.v1.AccountStatus;
import ru.tinkoff.piapi.contract.v1.AccountType;
import ru.tinkoff.piapi.core.UsersService;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ExtUsersServiceUnitTest {

    @Mock
    private UsersService usersService;

    @InjectMocks
    private ExtUsersService extUsersService;

    @Test
    void getAccounts() {
        final String id1 = "2008941383";
        final AccountType accountType1 = AccountType.ACCOUNT_TYPE_TINKOFF_IIS;
        final String name1 = "ИИС";
        final AccountStatus accountStatus1 = AccountStatus.ACCOUNT_STATUS_OPEN;
        final Timestamp openedDate1 = DateTimeTestData.createTimestamp(1562889600);
        final OffsetDateTime openedDateTime1 = DateTimeTestData.createDateTime(2019, 7, 12, 3, ZoneOffset.ofHours(3));
        final Timestamp closedDate1 = DateTimeTestData.createTimestamp(-10800);
        final OffsetDateTime closedDateTime1 = DateTimeTestData.createDateTime(1970, 1, 1, ZoneOffset.ofHours(3));
        final AccessLevel accessLevel1 = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

        Account account1 = Account.newBuilder()
                .setId(id1)
                .setType(accountType1)
                .setName(name1)
                .setStatus(accountStatus1)
                .setOpenedDate(openedDate1)
                .setClosedDate(closedDate1)
                .setAccessLevel(accessLevel1)
                .build();

        final String id2 = TestData.ACCOUNT_ID1;
        final AccountType accountType2 = AccountType.ACCOUNT_TYPE_TINKOFF;
        final String name2 = "Брокерский счёт";
        final AccountStatus accountStatus2 = AccountStatus.ACCOUNT_STATUS_OPEN;
        final Timestamp openedDate2 = DateTimeTestData.createTimestamp(1527206400);
        final OffsetDateTime openedDateTime2 = DateTimeTestData.createDateTime(2018, 5, 25, 3, ZoneOffset.ofHours(3));
        final Timestamp closedDate2 = DateTimeTestData.createTimestamp(-10800);
        final OffsetDateTime closedDateTime2 = DateTimeTestData.createDateTime(1970, 1, 1, ZoneOffset.ofHours(3));
        final AccessLevel accessLevel2 = AccessLevel.ACCOUNT_ACCESS_LEVEL_FULL_ACCESS;

        Account account2 = Account.newBuilder()
                .setId(id2)
                .setType(accountType2)
                .setName(name2)
                .setStatus(accountStatus2)
                .setOpenedDate(openedDate2)
                .setClosedDate(closedDate2)
                .setAccessLevel(accessLevel2)
                .build();

        Mockito.when(usersService.getAccountsSync())
                .thenReturn(List.of(account1, account2));

        final UserAccount userAccount1 = new UserAccount(id1, accountType1, name1, accountStatus1, openedDateTime1, closedDateTime1, accessLevel1);
        final UserAccount userAccount2 = new UserAccount(id2, accountType2, name2, accountStatus2, openedDateTime2, closedDateTime2, accessLevel2);
        final List<UserAccount> expectedUserAccounts = List.of(userAccount1, userAccount2);

        Mockito.when(usersService.getAccountsSync()).thenReturn(List.of(account1, account2));

        final List<UserAccount> result = extUsersService.getAccounts();

        Assertions.assertEquals(expectedUserAccounts, result);
    }

}