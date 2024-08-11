package ru.obukhov.trader.test.utils.matchers;

import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.mockito.ArgumentMatcher;

import java.util.Collection;

@RequiredArgsConstructor
public class AnyOrderMatcher<T> implements ArgumentMatcher<Collection<T>> {

    private final Collection<?> expected;

    @Override
    public boolean matches(final Collection<T> actual) {
        return (expected == null && actual == null) || expected != null && actual != null && CollectionUtils.isEqualCollection(expected, actual);
    }

}