package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.Account;

/**
 * Maps {@link ru.tinkoff.piapi.contract.v1.Account} to {@link Account}
 */
@Mapper(uses = DateTimeMapper.class)
public interface AccountMapper {

    Account map(final ru.tinkoff.piapi.contract.v1.Account source);

}