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
import de.adorsys.psd2.model.CardAccountDetails;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * List of card accounts with details. 
 */
@ApiModel(description = "List of card accounts with details. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class CardAccountList   {
  @JsonProperty("cardAccounts")
  @Valid
  private List<CardAccountDetails> cardAccounts = new ArrayList<>();

  public CardAccountList cardAccounts(List<CardAccountDetails> cardAccounts) {
    this.cardAccounts = cardAccounts;
    return this;
  }

  public CardAccountList addCardAccountsItem(CardAccountDetails cardAccountsItem) {
    this.cardAccounts.add(cardAccountsItem);
    return this;
  }

  /**
   * Get cardAccounts
   * @return cardAccounts
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid

  public List<CardAccountDetails> getCardAccounts() {
    return cardAccounts;
  }

  public void setCardAccounts(List<CardAccountDetails> cardAccounts) {
    this.cardAccounts = cardAccounts;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CardAccountList cardAccountList = (CardAccountList) o;
    return Objects.equals(this.cardAccounts, cardAccountList.cardAccounts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardAccounts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CardAccountList {\n");
    
    sb.append("    cardAccounts: ").append(toIndentedString(cardAccounts)).append("\n");
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

