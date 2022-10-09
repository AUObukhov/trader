package ru.obukhov.trader.market.model.transform;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.obukhov.trader.market.model.Share;
import ru.obukhov.trader.test.utils.model.share.TestShare1;

class ShareMapperUnitTest {

    private final ShareMapper shareMapper = Mappers.getMapper(ShareMapper.class);

    @Test
    void map() {
        final Share result = shareMapper.map(TestShare1.TINKOFF_SHARE);

        Assertions.assertEquals(TestShare1.SHARE, result);
    }

    @Test
    void map_whenValueIsNull() {
        final Share share = shareMapper.map(null);

        Assertions.assertNull(share);
    }

}