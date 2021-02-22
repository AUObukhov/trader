package ru.obukhov.investor.config;

import okhttp3.HttpUrl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class UrlLimitTests {

    // region matchesUrl tests

    @Test
    void matchesUrl_returnsTrue_whenMatches() {
        List<String> segments = Arrays.asList("market", "order");
        UrlLimit urlLimit = new UrlLimit(segments, 100);
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("localhost")
                .addPathSegment("openapi")
                .addPathSegment("v1")
                .addPathSegment("market")
                .addPathSegment("order")
                .build();

        Assertions.assertTrue(urlLimit.matchesUrl(httpUrl));
    }

    @Test
    void matchesUrl_returnsFalse_whenDoesNotMatches() {
        List<String> segments = Arrays.asList("market", "candles");
        UrlLimit urlLimit = new UrlLimit(segments, 100);
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("localhost")
                .addPathSegment("openapi")
                .addPathSegment("v1")
                .addPathSegment("market")
                .addPathSegment("order")
                .build();

        Assertions.assertFalse(urlLimit.matchesUrl(httpUrl));
    }

    // endregion

    // region getUrl tests

    @Test
    void getUrl_returnsProperUrl() {
        List<String> segments = Arrays.asList("market", "candles");
        UrlLimit urlLimit = new UrlLimit(segments, 100);

        Assertions.assertEquals("/market/candles", urlLimit.getUrl());
    }

    // endregion

}