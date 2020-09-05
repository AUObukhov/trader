package ru.obukhov.investor.web.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.tinkoff.invest.openapi.models.market.Instrument;

import java.util.List;

@Data
@AllArgsConstructor
public class GetInstrumentsResponse {

    private List<Instrument> instruments;

}
