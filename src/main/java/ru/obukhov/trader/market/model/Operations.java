package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Operations {
    @JsonProperty("operations")
    private List<Operation> operations = new ArrayList<>();

    public Operations operations(List<Operation> operations) {
        this.operations = operations;
        return this;
    }

    public Operations addOperationsItem(Operation operationsItem) {
        this.operations.add(operationsItem);
        return this;
    }

    /**
     * Get operations
     *
     * @return operations
     **/
    @Schema(required = true, description = "")
    public List<Operation> getOperations() {
        return operations;
    }

    public void setOperations(List<Operation> operations) {
        this.operations = operations;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Operations operations = (Operations) o;
        return Objects.equals(this.operations, operations.operations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operations);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class Operations {\n");

        sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
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
