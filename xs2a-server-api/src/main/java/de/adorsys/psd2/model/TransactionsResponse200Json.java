/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response for a successful read transaction list request. This account report contains transactions resulting from the query parameters.
 */
@Schema(description = "Body of the JSON response for a successful read transaction list request. This account report contains transactions resulting from the query parameters. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class TransactionsResponse200Json   {
  @JsonProperty("account")
  private AccountReference account = null;

  @JsonProperty("transactions")
  private AccountReport transactions = null;

  @JsonProperty("balances")
  private BalanceList balances = null;

  @JsonProperty("_links")
  private Map _links = null;

  public TransactionsResponse200Json account(AccountReference account) {
    this.account = account;
    return this;
  }

    /**
     * Get account
     *
     * @return account
     **/
    @Schema(description = "")
    @JsonProperty("account")

    @Valid
    public AccountReference getAccount() {
        return account;
    }

  public void setAccount(AccountReference account) {
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
    @Schema(description = "")
    @JsonProperty("transactions")

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
    @Schema(description = "")
    @JsonProperty("balances")

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
    @Schema(description = "")
    @JsonProperty("_links")

    @Valid
    public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TransactionsResponse200Json transactionsResponse200Json = (TransactionsResponse200Json) o;
    return Objects.equals(this.account, transactionsResponse200Json.account) &&
        Objects.equals(this.transactions, transactionsResponse200Json.transactions) &&
        Objects.equals(this.balances, transactionsResponse200Json.balances) &&
        Objects.equals(this._links, transactionsResponse200Json._links);
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
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}
