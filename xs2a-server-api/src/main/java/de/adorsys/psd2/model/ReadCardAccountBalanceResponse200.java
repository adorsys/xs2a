/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body of the response for a successful read balance for a card account request.
 */
@Schema(description = "Body of the response for a successful read balance for a card account request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class ReadCardAccountBalanceResponse200   {
  @JsonProperty("cardAccount")
  private AccountReference cardAccount = null;

  @JsonProperty("debitAccounting")
  private Boolean debitAccounting = null;

  @JsonProperty("balances")
  private BalanceList balances = null;

  public ReadCardAccountBalanceResponse200 cardAccount(AccountReference cardAccount) {
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

  public ReadCardAccountBalanceResponse200 debitAccounting(Boolean debitAccounting) {
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

  public ReadCardAccountBalanceResponse200 balances(BalanceList balances) {
    this.balances = balances;
      return this;
  }

    /**
     * Get balances
     *
     * @return balances
     **/
    @Schema(required = true, description = "")
    @JsonProperty("balances")
    @NotNull

    @Valid
    public BalanceList getBalances() {
    return balances;
  }

  public void setBalances(BalanceList balances) {
    this.balances = balances;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReadCardAccountBalanceResponse200 readCardAccountBalanceResponse200 = (ReadCardAccountBalanceResponse200) o;
    return Objects.equals(this.cardAccount, readCardAccountBalanceResponse200.cardAccount) &&
        Objects.equals(this.debitAccounting, readCardAccountBalanceResponse200.debitAccounting) &&
        Objects.equals(this.balances, readCardAccountBalanceResponse200.balances);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardAccount, debitAccounting, balances);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReadCardAccountBalanceResponse200 {\n");

    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
    sb.append("    debitAccounting: ").append(toIndentedString(debitAccounting)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
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
