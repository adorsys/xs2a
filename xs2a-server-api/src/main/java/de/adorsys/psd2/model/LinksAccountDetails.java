package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Objects;

/**
 * Links to the account, which can be directly used for retrieving account information from this dedicated account.
 * Links to \&quot;balances\&quot; and/or \&quot;transactions\&quot;  These links are only supported, when the
 * corresponding consent has been already granted.
 */
@ApiModel(description = "Links to the account, which can be directly used for retrieving account information from " +
    "this dedicated account.  Links to \"balances\" and/or \"transactions\"  These links are only supported, when the" +
    " corresponding consent has been already granted. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class LinksAccountDetails extends HashMap<String, String> {
    @JsonProperty("balances")
    private String balances = null;
    @JsonProperty("transactions")
    private String transactions = null;

    public LinksAccountDetails balances(String balances) {
        this.balances = balances;
        return this;
    }

    /**
     * Get balances
     *
     * @return balances
     **/
    @ApiModelProperty(value = "")
    public String getBalances() {
        return balances;
    }

    public void setBalances(String balances) {
        this.balances = balances;
    }

    public LinksAccountDetails transactions(String transactions) {
        this.transactions = transactions;
        return this;
    }

    /**
     * Get transactions
     *
     * @return transactions
     **/
    @ApiModelProperty(value = "")
    public String getTransactions() {
        return transactions;
    }

    public void setTransactions(String transactions) {
        this.transactions = transactions;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinksAccountDetails _linksAccountDetails = (LinksAccountDetails) o;
        return Objects.equals(this.balances, _linksAccountDetails.balances) && Objects.equals(this.transactions,
            _linksAccountDetails.transactions) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(balances, transactions, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinksAccountDetails {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
        sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
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

