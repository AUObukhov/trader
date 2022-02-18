package ru.obukhov.trader.web.client.exchange;

import lombok.Data;
import ru.obukhov.trader.market.model.SandboxAccount;

@Data
public class SandboxRegisterResponse {

    private String trackingId;

    private String status;

    private SandboxAccount payload;

}