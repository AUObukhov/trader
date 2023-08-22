package ru.obukhov.trader.common.service.impl;

import ru.tinkoff.piapi.contract.v1.Quotation;

import java.util.List;

/**
 * Abstract class with methods for calculation of moving averages
 *
 * @see <a href=https://www.investopedia.com/terms/m/movingaverage.asp">investopedia</a>
 */
public interface MovingAverager {

    /**
     * Calculates moving averages
     *
     * @param values list of values to calculate averages
     * @param window period of average, usually a number of values, used to calculate each average.
     *               Higher values gives more smooth and more lagging averages trend.
     *               Must be positive
     * @return calculated averages
     */
    default List<Quotation> getAverages(final List<Quotation> values, final int window) {
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
     *               calculation repeated by previously calculated averages.
     *               Higher values gives more smooth averages trend.
     *               Must be positive
     * @return calculated averages
     */
    List<Quotation> getAverages(final List<Quotation> values, final int window, final int order);

}