package ru.obukhov.trader.market.model;

import java.util.List;

public record Portfolio(List<PortfolioPosition> positions) {
}