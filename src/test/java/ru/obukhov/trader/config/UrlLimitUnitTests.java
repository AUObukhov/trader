package ru.obukhov.trader.config;

import okhttp3.HttpUrl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class UrlLimitUnitTests {

    // region matchesUrl tests

    @Test
    void matchesUrl_returnsTrue_whenMatches() {
        final List<String> segments = List.of("market", "order");
        final UrlLimit urlLimit = new UrlLimit(segments, 100);
        final HttpUrl httpUrl = new HttpUrl.Builder()
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
        final List<String> segments = List.of("market", "candles");
        final UrlLimit urlLimit = new UrlLimit(segments, 100);
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
        final List<String> segments = List.of("market", "candles");
        final UrlLimit urlLimit = new UrlLimit(segments, 100);

        Assertions.assertEquals("/market/candles", urlLimit.getUrl());
    }

    // endregion

}