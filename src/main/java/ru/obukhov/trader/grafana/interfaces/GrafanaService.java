package ru.obukhov.trader.grafana.interfaces;

import ru.obukhov.trader.grafana.model.GetDataRequest;
import ru.obukhov.trader.grafana.model.QueryResult;

import java.util.List;

public interface GrafanaService {
    List<QueryResult> getData(GetDataRequest request);
}