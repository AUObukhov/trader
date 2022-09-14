package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

import java.util.Map;

@UtilityClass
public class MapUtils {

    public static String getRequiredString(final Map<String, Object> map, final String key) {
        final String value = (String) map.get(key);
        Assert.notNull(value, () -> "\"" + key + "\" is mandatory");
        return value;
    }

    public static String getNotBlankString(final Map<String, Object> map, final String key) {
        final String value = (String) map.get(key);
        Assert.isTrue(StringUtils.isNotBlank(value), () -> "\"" + key + "\" must be not blank");
        return value;
    }

    public static Integer getRequiredInteger(final Map<String, Object> map, final String key) {
        final Integer value = (Integer) map.get(key);
        Assert.notNull(value, () -> "\"" + key + "\" is mandatory");
        return value;
    }

}