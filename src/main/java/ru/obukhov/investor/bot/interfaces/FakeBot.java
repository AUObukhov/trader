package ru.obukhov.investor.bot.interfaces;

import ru.obukhov.investor.service.impl.FakeTinkoffService;

public interface FakeBot extends Bot {

    String getName();

    FakeTinkoffService getFakeTinkoffService();

}