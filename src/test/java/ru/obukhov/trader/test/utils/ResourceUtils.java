package ru.obukhov.trader.test.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResourceUtils {

    public static String getResourceAsString(final String path) throws IOException {
        final InputStream inputStream = ResourceUtils.class.getClassLoader().getResourceAsStream(path);
        Assert.assertNotNull("resource not found", inputStream);

        return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
    }

}