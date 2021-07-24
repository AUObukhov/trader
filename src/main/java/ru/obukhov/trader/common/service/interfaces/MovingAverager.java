package ru.obukhov.trader.common.service.interfaces;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Interface with methods for calculation of moving averages
 *
 * @see <a href=https://www.investopedia.com/terms/m/movingaverage.asp#:~:text=Key%20Takeaways-,A%20moving%20average%20(MA)%20is%20a%20stock%20indicator%20that%20is,commonly%20used%20in%20technical%20analysis.&text=A%20simple%20moving%20average%20(SMA,%2C%20100%2C%20or%20200%20days.">investopedia</a>
 */
public interface MovingAverager {

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
    default <T> List<BigDecimal> getAverages(
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
    default List<BigDecimal> getAverages(final List<BigDecimal> values, final int window) {
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
    List<BigDecimal> getAverages(final List<BigDecimal> values, final int window, final int order);

}