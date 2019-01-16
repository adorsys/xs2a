package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response for a successful read transaction list request.
 */
@ApiModel(description = "Body of the JSON response for a successful read transaction list request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class TransactionsResponse200Json {
    @JsonProperty("account")
    private Object account = null;
    @JsonProperty("transactions")
    private AccountReport transactions = null;
    @JsonProperty("balances")
    private BalanceList balances = null;
    @JsonProperty("_links")
    private Map _links = null;

    public TransactionsResponse200Json account(Object account) {
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

    public TransactionsResponse200Json transactions(AccountReport transactions) {
        this.transactions = transactions;
        return this;
    }

    /**
     * Get transactions
     *
     * @return transactions
     **/
    @ApiModelProperty(value = "")
    @Valid
    public AccountReport getTransactions() {
        return transactions;
    }

    public void setTransactions(AccountReport transactions) {
        this.transactions = transactions;
    }

    public TransactionsResponse200Json balances(BalanceList balances) {
        this.balances = balances;
        return this;
    }

    /**
     * Get balances
     *
     * @return balances
     **/
    @ApiModelProperty(value = "")
    @Valid
    public BalanceList getBalances() {
        return balances;
    }

    public void setBalances(BalanceList balances) {
        this.balances = balances;
    }

    public TransactionsResponse200Json _links(Map _links) {
        this._links = _links;
        return this;
    }

    /**
     * Get _links
     *
     * @return _links
     **/
    @ApiModelProperty(value = "")
    @Valid
    public Map getLinks() {
        return _links;
    }

    public void setLinks(Map _links) {
        this._links = _links;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TransactionsResponse200Json transactionsResponse200Json = (TransactionsResponse200Json) o;
        return Objects.equals(this.account, transactionsResponse200Json.account) && Objects.equals(this.transactions,
            transactionsResponse200Json.transactions) && Objects.equals(this.balances,
            transactionsResponse200Json.balances) && Objects.equals(this._links, transactionsResponse200Json._links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(account, transactions, balances, _links);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TransactionsResponse200Json {\n");
        sb.append("    account: ").append(toIndentedString(account)).append("\n");
        sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
        sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
        sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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

