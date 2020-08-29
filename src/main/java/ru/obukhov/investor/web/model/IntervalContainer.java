package ru.obukhov.investor.web.model;

import ru.obukhov.investor.web.model.validation.constraint.IsConsecutive;

import java.time.OffsetDateTime;

@IsConsecutive
public interface IntervalContainer {

    OffsetDateTime getFrom();

    OffsetDateTime getTo();

}