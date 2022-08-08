package ru.obukhov.trader.market.model.transform;

import org.mapstruct.Mapper;
import ru.obukhov.trader.market.model.UserAccount;
import ru.tinkoff.piapi.contract.v1.Account;

/**
 * Maps {@link Account} to {@link UserAccount}
 */
@Mapper(uses = DateTimeMapper.class)
public interface AccountMapper {

    UserAccount map(final Account source);

}