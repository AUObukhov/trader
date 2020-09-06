package ru.obukhov.investor.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TokenHolder {

    @Getter
    @Setter
    private static String token;
}