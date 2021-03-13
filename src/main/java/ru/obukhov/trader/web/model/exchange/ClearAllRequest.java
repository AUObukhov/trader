package ru.obukhov.trader.web.model.exchange;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class ClearAllRequest {

    @Nullable
    private String brokerAccountId;

}