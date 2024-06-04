package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;
import org.junit.Assert;

import java.io.IOException;
import java.net.URL;

@UtilityClass
public class ResourceUtils {

    private static final String TEST_DATA_FOLDER = "test-data/";

    public static <T> T getResourceAsObject(final String path, final Class<T> clazz) {
        try {
            final String fullPath = TEST_DATA_FOLDER + path;
            final URL url = ResourceUtils.class.getClassLoader().getResource(fullPath);
            Assert.assertNotNull("resource \"" + fullPath + "\" not found", url);
            return TestUtils.OBJECT_MAPPER.readValue(url, clazz);
        } catch (final IOException ioException) {
            throw new RuntimeException(ioException);
        }
    }

}