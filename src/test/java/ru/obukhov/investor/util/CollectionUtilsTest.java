package ru.obukhov.investor.util;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static ru.obukhov.investor.util.MathUtils.numbersEqual;

public class CollectionUtilsTest {

    @Test
    public void reduceMultimap_average() {
        Multimap<String, BigDecimal> multimap = MultimapBuilder.hashKeys().arrayListValues().build();

        multimap.put("key1", BigDecimal.valueOf(10));

        multimap.put("key2", BigDecimal.valueOf(1));
        multimap.put("key2", BigDecimal.valueOf(2));
        multimap.put("key2", BigDecimal.valueOf(3));
        multimap.put("key2", BigDecimal.valueOf(4));

        Map<String, BigDecimal> result = CollectionUtils.reduceMultimap(multimap, MathUtils::getAverageMoney);

        assertTrue(numbersEqual(result.get("key1"), 10));
        assertTrue(numbersEqual(result.get("key2"), 2.5));
    }

}
