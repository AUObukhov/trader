package ru.obukhov.trader.trading.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class DecisionsData {

    private BigDecimal commission;
    private List<DecisionData> decisionDatas;

}