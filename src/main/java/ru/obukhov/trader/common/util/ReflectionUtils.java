package ru.obukhov.trader.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.beans.PropertyDescriptor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils {

    /**
     * @return value of field with given {@code fieldName} from given {@code object} if it has read method
     */
    @SneakyThrows
    public static Object getFieldValueByReadMethod(final Object object, final String fieldName) {
        final PropertyDescriptor propertyDescriptor = new PropertyDescriptor(fieldName, object.getClass());
        return propertyDescriptor.getReadMethod().invoke(object);
    }

}