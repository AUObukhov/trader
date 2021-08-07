package ru.obukhov.trader.grafana.model;

import lombok.Data;

import java.util.List;

@Data
public class QueryTableResult implements QueryResult {
    private TableType type = TableType.TABLE;
    private List<Column> columns;
    private List<List<Object>> rows;
}