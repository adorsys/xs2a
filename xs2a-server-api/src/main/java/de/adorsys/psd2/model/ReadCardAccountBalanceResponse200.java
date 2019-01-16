/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.BalanceList;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Body of the response for a successful read balance for a card account request.
 */
@ApiModel(description = "Body of the response for a successful read balance for a card account request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class ReadCardAccountBalanceResponse200   {
  @JsonProperty("cardAccount")
  private AccountReference cardAccount = null;

  @JsonProperty("balances")
  private BalanceList balances = null;

  public ReadCardAccountBalanceResponse200 cardAccount(AccountReference cardAccount) {
    this.cardAccount = cardAccount;
    return this;
  }

  /**
   * Get cardAccount
   * @return cardAccount
  **/
  @ApiModelProperty(value = "")

  @Valid

  public AccountReference getCardAccount() {
    return cardAccount;
  }

  public void setCardAccount(AccountReference cardAccount) {
    this.cardAccount = cardAccount;
  }

  public ReadCardAccountBalanceResponse200 balances(BalanceList balances) {
    this.balances = balances;
    return this;
  }

  /**
   * Get balances
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ReadCardAccountBalanceResponse200 readCardAccountBalanceResponse200 = (ReadCardAccountBalanceResponse200) o;
    return Objects.equals(this.cardAccount, readCardAccountBalanceResponse200.cardAccount) &&
        Objects.equals(this.balances, readCardAccountBalanceResponse200.balances);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardAccount, balances);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ReadCardAccountBalanceResponse200 {\n");
    
    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
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

