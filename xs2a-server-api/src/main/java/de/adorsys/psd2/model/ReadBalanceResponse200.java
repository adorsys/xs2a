package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body of the response for a successful read balance request.
 */
@ApiModel(description = "Body of the response for a successful read balance request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class ReadBalanceResponse200 {
    @JsonProperty("account")
    private Object account = null;
    @JsonProperty("balances")
    private BalanceList balances = null;

    public ReadBalanceResponse200 account(Object account) {
        this.account = account;
        return this;
    }

    /**
     * Get account
     *
     * @return account
     **/
    @ApiModelProperty(value = "")
    public Object getAccount() {
        return account;
    }

    public void setAccount(Object account) {
        this.account = account;
    }

    public ReadBalanceResponse200 balances(BalanceList balances) {
        this.balances = balances;
        return this;
    }

    /**
     * Get balances
     *
     * @return balances
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public BalanceList getBalances() {
        return balances;
    }

    public void setBalances(BalanceList balances) {
        this.balances = balances;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ReadBalanceResponse200 readBalanceResponse200 = (ReadBalanceResponse200) o;
        return Objects.equals(this.account, readBalanceResponse200.account) && Objects.equals(this.balances,
            readBalanceResponse200.balances);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, balances);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ReadBalanceResponse200 {\n");
        sb.append("    account: ").append(toIndentedString(account)).append("\n");
        sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

