package ru.obukhov.trader.test.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class ResourceUtils {

    private static final String TEST_DATA_FOLDER = "test-data/";

    public static String getTestDataAsString(final String path) throws IOException {
        return getResourceAsString(TEST_DATA_FOLDER + path);
    }

    public static String getResourceAsString(final String path) throws IOException {
        final InputStream inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream(path);
        Assert.assertNotNull("resource not found", inputStream);

        return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    }

}