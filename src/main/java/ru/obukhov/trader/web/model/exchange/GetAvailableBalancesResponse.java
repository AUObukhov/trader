package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.piapi.core.models.Money;

import java.util.List;

@Data
@AllArgsConstructor
public class GetAvailableBalancesResponse {

    private List<Money> moneys;

}