package ru.obukhov.trader.common.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@UtilityClass
public class MapUtils {

    public static String getRequiredString(Map<String, Object> map, String key) {
        final String value = (String) map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("\"" + key + "\" is mandatory");
        }
        return value;
    }

    public static String getNotBlankString(Map<String, Object> map, String key) {
        final String value = (String) map.get(key);
        if (StringUtils.isBlank(value)) {
            throw new IllegalArgumentException("\"" + key + "\" must be not blank");
        }
        return value;
    }

    public static Integer getRequiredInteger(Map<String, Object> map, String key) {
        final Integer value = (Integer) map.get(key);
        if (value == null) {
            throw new IllegalArgumentException("\"" + key + "\" is mandatory");
        }
        return value;
    }

}