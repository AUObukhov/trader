package ru.obukhov.trader.web.model;

import ru.obukhov.trader.web.model.validation.constraint.IsConsecutive;

import java.time.OffsetDateTime;

@IsConsecutive
public interface IntervalContainer {

    OffsetDateTime getFrom();

    OffsetDateTime getTo();

}