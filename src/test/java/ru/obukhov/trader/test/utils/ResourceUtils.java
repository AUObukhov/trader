package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

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

    public static List<String> getResourceAsStrings(final String path) {
        final String fullPath = TEST_DATA_FOLDER + path;
        final InputStream url = ResourceUtils.class.getClassLoader().getResourceAsStream(fullPath);
        Assert.assertNotNull("resource \"" + fullPath + "\" not found", url);
        return IOUtils.readLines(url, Charset.defaultCharset());
    }

}