package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Currencies {
    @JsonProperty("currencies")
    private List<CurrencyPosition> currencies = new ArrayList<>();

    public Currencies currencies(List<CurrencyPosition> currencies) {
        this.currencies = currencies;
        return this;
    }

    public Currencies addCurrenciesItem(CurrencyPosition currenciesItem) {
        this.currencies.add(currenciesItem);
        return this;
    }

    /**
     * Get currencies
     *
     * @return currencies
     **/
    @Schema(required = true, description = "")
    public List<CurrencyPosition> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<CurrencyPosition> currencies) {
        this.currencies = currencies;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Currencies currencies = (Currencies) o;
        return Objects.equals(this.currencies, currencies.currencies);
    }

    @Override
    public int hashCode() {
        return Objects.hash(currencies);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Currencies {\n");

        sb.append("    currencies: ").append(toIndentedString(currencies)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

}
