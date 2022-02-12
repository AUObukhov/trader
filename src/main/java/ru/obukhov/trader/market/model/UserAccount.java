package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class UserAccount {

    @JsonProperty("brokerAccountType")
    private BrokerAccountType brokerAccountType = null;

    @JsonProperty("brokerAccountId")
    private String brokerAccountId = null;

    public UserAccount brokerAccountType(BrokerAccountType brokerAccountType) {
        this.brokerAccountType = brokerAccountType;
        return this;
    }

    /**
     * Get brokerAccountType
     *
     * @return brokerAccountType
     **/
    @Schema(required = true, description = "")
    public BrokerAccountType getBrokerAccountType() {
        return brokerAccountType;
    }

    public void setBrokerAccountType(BrokerAccountType brokerAccountType) {
        this.brokerAccountType = brokerAccountType;
    }

    public UserAccount brokerAccountId(String brokerAccountId) {
        this.brokerAccountId = brokerAccountId;
        return this;
    }

    /**
     * Get brokerAccountId
     *
     * @return brokerAccountId
     **/
    @Schema(required = true, description = "")
    public String getBrokerAccountId() {
        return brokerAccountId;
    }

    public void setBrokerAccountId(String brokerAccountId) {
        this.brokerAccountId = brokerAccountId;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAccount userAccount = (UserAccount) o;
        return Objects.equals(this.brokerAccountType, userAccount.brokerAccountType) &&
                Objects.equals(this.brokerAccountId, userAccount.brokerAccountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brokerAccountType, brokerAccountId);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserAccount {\n");

        sb.append("    brokerAccountType: ").append(toIndentedString(brokerAccountType)).append("\n");
        sb.append("    brokerAccountId: ").append(toIndentedString(brokerAccountId)).append("\n");
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
