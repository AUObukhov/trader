package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.trading.model.BackTestResult;

import java.util.List;

@Data
@AllArgsConstructor
public class BackTestResponse {

    private List<BackTestResult> results;

}