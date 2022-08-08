package ru.obukhov.trader.market.model;

import ru.tinkoff.piapi.contract.v1.Operation;

import java.util.List;

public record Operations(List<Operation> operations) {
}