package ru.obukhov.investor.web.model;

import lombok.Data;
import org.jetbrains.annotations.Nullable;

@Data
public class ClearAllRequest {

    @Nullable
    private String brokerAccountId;

}