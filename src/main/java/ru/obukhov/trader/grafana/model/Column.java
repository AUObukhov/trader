package ru.obukhov.trader.grafana.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@AllArgsConstructor
public class Column {
    private String text;
    private ColumnType type;
}