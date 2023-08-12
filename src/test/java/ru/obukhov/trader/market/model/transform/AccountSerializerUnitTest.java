package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.account.TestAccount1;
import ru.obukhov.trader.test.utils.model.account.TestAccount2;
import ru.tinkoff.piapi.contract.v1.Account;

import java.io.IOException;

class AccountSerializerUnitTest extends SerializerAbstractUnitTest<Account> {

    private final AccountSerializer accountSerializer = new AccountSerializer();

    @Test
    void test1() throws IOException {
        test(accountSerializer, TestAccount1.ACCOUNT, TestAccount1.STRING, new TimestampSerializer());
    }

    @Test
    void test2() throws IOException {
        test(accountSerializer, TestAccount2.ACCOUNT, TestAccount2.STRING, new TimestampSerializer());
    }

}