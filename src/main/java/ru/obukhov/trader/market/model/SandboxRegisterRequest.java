package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Objects;

public class SandboxRegisterRequest {
    @JsonProperty("brokerAccountType")
    private BrokerAccountType brokerAccountType = null;

    public SandboxRegisterRequest brokerAccountType(BrokerAccountType brokerAccountType) {
        this.brokerAccountType = brokerAccountType;
        return this;
    }

    /**
     * Get brokerAccountType
     *
     * @return brokerAccountType
     **/
    @Schema(description = "")
    public BrokerAccountType getBrokerAccountType() {
        return brokerAccountType;
    }

    public void setBrokerAccountType(BrokerAccountType brokerAccountType) {
        this.brokerAccountType = brokerAccountType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SandboxRegisterRequest sandboxRegisterRequest = (SandboxRegisterRequest) o;
        return Objects.equals(this.brokerAccountType, sandboxRegisterRequest.brokerAccountType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brokerAccountType);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SandboxRegisterRequest {\n");

        sb.append("    brokerAccountType: ").append(toIndentedString(brokerAccountType)).append("\n");
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
