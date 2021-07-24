package ru.obukhov.trader.common.service.impl;

import org.springframework.util.Assert;
import ru.obukhov.trader.market.model.MovingAverageType;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Abstract class with methods for calculation of moving averages
 *
 * @see <a href=https://www.investopedia.com/terms/m/movingaverage.asp">investopedia</a>
 */
public abstract class MovingAverager {

    private static final EnumMap<MovingAverageType, MovingAverager> INSTANCES = new EnumMap<>(MovingAverageType.class);

    @PostConstruct
    private void postConstruct() {
        INSTANCES.put(getType(), this);
    }

    /**
     * @return Instance of MovingAverager with given {@code type} of moving average
     */
    public static MovingAverager getByType(final MovingAverageType type) {
        Assert.isTrue(
                INSTANCES.containsKey(type),
                "Not found MovingAverager instance for " + type.getValue()
        );
        return INSTANCES.get(type);
    }

    /**
     * @return type of current MovingAverager
     */
    public abstract MovingAverageType getType();

    /**
     * Calculates moving averages
     *
     * @param elements       list of elements, which containing values to calculate averages
     * @param valueExtractor function to get value from item of {@code elements}
     * @param window         period of average, usually a number of values, used to calculate each average.
     *                       Higher values gives more smooth and more lagging averages trend.
     *                       Must be positive
     * @param order          number of consecutive calculations of averages.
     *                       For example, when equals 2, than after calculation of averages by initial values,
     *                       calculation repeated again by previously calculated averages.
     *                       Higher values gives more smooth averages trend.
     *                       Must be positive
     * @param <T>            base type of {@code elements}
     * @return calculated averages
     */
    public <T> List<BigDecimal> getAverages(
            final List<T> elements,
            final Function<T, BigDecimal> valueExtractor,
            final int window,
            final int order
    ) {
        final List<BigDecimal> values = elements.stream().map(valueExtractor).collect(Collectors.toList());
        return getAverages(values, window, order);
    }

    /**
     * Calculates moving averages
     *
     * @param values list of values to calculate averages
     * @param window period of average, usually a number of values, used to calculate each average.
     *               Higher values gives more smooth and more lagging averages trend.
     *               Must be positive
     * @return calculated averages
     */
    public List<BigDecimal> getAverages(final List<BigDecimal> values, final int window) {
        return getAverages(values, window, 1);
    }

    /**
     * Calculates moving averages
     *
     * @param values list of values to calculate averages
     * @param window period of average, usually a number of values, used to calculate each average.
     *               Higher values gives more smooth and more lagging averages trend.
     *               Must be positive
     * @param order  number of consecutive calculations of averages.
     *               For example, when equals 2, than after calculation of averages by initial values,
     *               calculation repeated again by previously calculated averages.
     *               Higher values gives more smooth averages trend.
     *               Must be positive
     * @return calculated averages
     */
    public abstract List<BigDecimal> getAverages(final List<BigDecimal> values, final int window, final int order);

}