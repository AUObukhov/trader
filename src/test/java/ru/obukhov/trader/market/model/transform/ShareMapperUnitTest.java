package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.model.share.TestShare;
import ru.obukhov.trader.test.utils.model.share.TestShares;

class ShareMapperUnitTest {

    private final ShareMapper shareMapper = Mappers.getMapper(ShareMapper.class);

    @Test
    void map() {
        final TestShare testShare = TestShares.APPLE;

        final Share result = shareMapper.map(testShare.tShare());

        Assertions.assertEquals(testShare.share(), result);
    }

    @Test
    void map_whenValueIsNull() {
        final Share share = shareMapper.map(null);

        Assertions.assertNull(share);
    }

}