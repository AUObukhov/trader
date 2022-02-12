package ru.obukhov.trader.market.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserAccounts {
    @JsonProperty("accounts")
    private List<UserAccount> accounts = new ArrayList<>();

    public UserAccounts accounts(List<UserAccount> accounts) {
        this.accounts = accounts;
        return this;
    }

    public UserAccounts addAccountsItem(UserAccount accountsItem) {
        this.accounts.add(accountsItem);
        return this;
    }

    /**
     * Get accounts
     *
     * @return accounts
     **/
    @Schema(required = true, description = "")
    public List<UserAccount> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<UserAccount> accounts) {
        this.accounts = accounts;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserAccounts userAccounts = (UserAccounts) o;
        return Objects.equals(this.accounts, userAccounts.accounts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accounts);
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UserAccounts {\n");

        sb.append("    accounts: ").append(toIndentedString(accounts)).append("\n");
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
