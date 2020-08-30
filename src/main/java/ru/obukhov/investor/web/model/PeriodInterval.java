package ru.obukhov.investor.web.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum PeriodInterval {

    @JsonProperty("day")
    DAY,

    @JsonProperty("week")
    WEEK,

    @JsonProperty("month")
    MONTH,

    @JsonProperty("year")
    YEAR

}