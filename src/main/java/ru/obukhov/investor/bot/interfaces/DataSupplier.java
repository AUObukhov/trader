package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.bot.model.DecisionData;

/**
 * Interface for getting data for bot
 */
public interface DataSupplier {

    DecisionData getData(String ticker);

}