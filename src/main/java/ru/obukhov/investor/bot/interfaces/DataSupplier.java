package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.bot.model.DecisionData;

public interface DataSupplier {
    DecisionData getData(String ticker);
}