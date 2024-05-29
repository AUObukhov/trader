package ru.obukhov.trader.common.util;

import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Collector to terminate streams with single value.<br/>
 * A null value is considered a valid value.<br/>
 * Throws {@link IllegalArgumentException} or supplied exception if stream contains no items.<br/>
 * Throws {@link IllegalArgumentException} or supplied exception if stream contains more than 1 item.<br/>
 *
 * @param <T> stream item type
 */
public class SingleItemCollector<T> implements Collector<T, List<T>, T> {

    private static final Set<Characteristics> CHARACTERISTICS = Set.of(Characteristics.UNORDERED);

    private final Supplier<? extends RuntimeException> noItemsExceptionSupplier;
    private final Supplier<? extends RuntimeException> multipleItemsExceptionSupplier;

    public SingleItemCollector() {
        this.noItemsExceptionSupplier = null;
        this.multipleItemsExceptionSupplier = null;
    }

    public SingleItemCollector(final Supplier<? extends RuntimeException> exceptionSupplier) {
        this.noItemsExceptionSupplier = exceptionSupplier;
        this.multipleItemsExceptionSupplier = exceptionSupplier;
    }

    public SingleItemCollector(
            final Supplier<? extends RuntimeException> noItemsExceptionSupplier,
            final Supplier<? extends RuntimeException> multipleItemsExceptionSupplier
    ) {
        this.noItemsExceptionSupplier = noItemsExceptionSupplier;
        this.multipleItemsExceptionSupplier = multipleItemsExceptionSupplier;
    }

    @Override
    public Supplier<List<T>> supplier() {
        return () -> new ArrayList<>(1);
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return (accumulator, t) -> {
            if (accumulator.isEmpty()) {
                accumulator.add(t);
            } else {
                throw multipleItemsExceptionSupplier == null
                        ? new IllegalArgumentException("Expected single item. Multiple items found.")
                        : multipleItemsExceptionSupplier.get();
            }
        };
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (a1, a2) -> {
            throw new NotImplementedException("Parallel streams are not supported");
        };
    }

    @Override
    public Function<List<T>, T> finisher() {
        return accumulator -> {
            if (accumulator.isEmpty()) {
                throw noItemsExceptionSupplier == null
                        ? new IllegalArgumentException("Expected single item. No items found.")
                        : noItemsExceptionSupplier.get();
            } else {
                return accumulator.getFirst();
            }
        };
    }

    @Override
    public Set<Characteristics> characteristics() {
        return CHARACTERISTICS;
    }

}