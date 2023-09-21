package ru.obukhov.trader.common.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@UtilityClass
public class ReflectionUtils {

    /**
     * @return value of field with given {@code fieldName} from given {@code object} if it has read method
     */
    @SneakyThrows
    public static Object getFieldValueByReadMethod(final Object object, final String fieldName) {
        final PropertyDescriptor propertyDescriptor = new PropertyDescriptor(fieldName, object.getClass());
        return propertyDescriptor.getReadMethod().invoke(object);
    }

    public static boolean fieldIsNotStatic(final Field field) {
        return (field.getModifiers() & Modifier.STATIC) == 0;
    }

}