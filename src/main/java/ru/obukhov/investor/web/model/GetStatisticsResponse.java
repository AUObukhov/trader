package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Map;

@Data
@AllArgsConstructor
public class GetStatisticsResponse {
    private Map<LocalTime, BigDecimal> saldosByTimes;
}
