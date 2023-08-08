package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Test;
import ru.obukhov.trader.test.utils.model.share.TestShare1;
import ru.tinkoff.piapi.contract.v1.Share;

import java.io.IOException;

class ShareSerializerUnitTest extends SerializerAbstractUnitTest<Share> {

    private final ShareSerializer shareSerializer = new ShareSerializer();

    @Test
    void test() throws IOException {
        test(shareSerializer, TestShare1.SHARE, TestShare1.STRING, new QuotationSerializer(), new MoneyValueSerializer(), new TimestampSerializer());
    }

}