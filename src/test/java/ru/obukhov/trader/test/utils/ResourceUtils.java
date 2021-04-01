package ru.obukhov.trader.test.utils;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class ResourceUtils {

    public static String getResourceAsString(String path) throws IOException {
        InputStream inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream(path);
        Assert.assertNotNull("resource not found", inputStream);

        return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    }

}