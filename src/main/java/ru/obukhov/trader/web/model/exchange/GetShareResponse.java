package ru.obukhov.trader.web.model.exchange;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.obukhov.trader.market.model.Share;

@Data
@AllArgsConstructor
public class GetShareResponse {

    private Share share;

}
