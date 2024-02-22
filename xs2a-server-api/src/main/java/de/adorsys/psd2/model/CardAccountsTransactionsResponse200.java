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
 * Body of the JSON response for a successful read card account transaction list request. This card account report contains transactions resulting from the query parameters.
 */
@Schema(description = "Body of the JSON response for a successful read card account transaction list request. This card account report contains transactions resulting from the query parameters. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class CardAccountsTransactionsResponse200   {
  @JsonProperty("cardAccount")
  private AccountReference cardAccount = null;

  @JsonProperty("debitAccounting")
  private Boolean debitAccounting = null;

  @JsonProperty("cardTransactions")
  private CardAccountReport cardTransactions = null;

  @JsonProperty("balances")
  private BalanceList balances = null;

  @JsonProperty("_links")
  private Map _links = null;

  public CardAccountsTransactionsResponse200 cardAccount(AccountReference cardAccount) {
    this.cardAccount = cardAccount;
    return this;
  }

    /**
     * Get cardAccount
     *
     * @return cardAccount
     **/
    @Schema(description = "")
    @JsonProperty("cardAccount")

    @Valid
    public AccountReference getCardAccount() {
        return cardAccount;
    }

  public void setCardAccount(AccountReference cardAccount) {
    this.cardAccount = cardAccount;
  }

  public CardAccountsTransactionsResponse200 debitAccounting(Boolean debitAccounting) {
    this.debitAccounting = debitAccounting;
    return this;
  }

    /**
     * If true, the amounts of debits on the reports are quoted positive with the related consequence for balances. If false, the amount of debits on the reports are quoted negative.
     *
     * @return debitAccounting
     **/
    @Schema(description = "If true, the amounts of debits on the reports are quoted positive with the related consequence for balances. If false, the amount of debits on the reports are quoted negative. ")
    @JsonProperty("debitAccounting")

    public Boolean isDebitAccounting() {
        return debitAccounting;
  }

  public void setDebitAccounting(Boolean debitAccounting) {
    this.debitAccounting = debitAccounting;
  }

  public CardAccountsTransactionsResponse200 cardTransactions(CardAccountReport cardTransactions) {
    this.cardTransactions = cardTransactions;
      return this;
  }

    /**
     * Get cardTransactions
     *
     * @return cardTransactions
     **/
    @Schema(description = "")
    @JsonProperty("cardTransactions")

    @Valid
    public CardAccountReport getCardTransactions() {
        return cardTransactions;
  }

  public void setCardTransactions(CardAccountReport cardTransactions) {
    this.cardTransactions = cardTransactions;
  }

  public CardAccountsTransactionsResponse200 balances(BalanceList balances) {
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

    public CardAccountsTransactionsResponse200 _links(Map _links) {
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
    CardAccountsTransactionsResponse200 cardAccountsTransactionsResponse200 = (CardAccountsTransactionsResponse200) o;
    return Objects.equals(this.cardAccount, cardAccountsTransactionsResponse200.cardAccount) &&
        Objects.equals(this.debitAccounting, cardAccountsTransactionsResponse200.debitAccounting) &&
        Objects.equals(this.cardTransactions, cardAccountsTransactionsResponse200.cardTransactions) &&
        Objects.equals(this.balances, cardAccountsTransactionsResponse200.balances) &&
        Objects.equals(this._links, cardAccountsTransactionsResponse200._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardAccount, debitAccounting, cardTransactions, balances, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardAccountsTransactionsResponse200 {\n");

    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
    sb.append("    debitAccounting: ").append(toIndentedString(debitAccounting)).append("\n");
    sb.append("    cardTransactions: ").append(toIndentedString(cardTransactions)).append("\n");
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
