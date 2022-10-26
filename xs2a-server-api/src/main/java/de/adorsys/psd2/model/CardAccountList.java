/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * List of card accounts with details.
 */
@Schema(description = "List of card accounts with details. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


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
     *
     * @return cardAccounts
     **/
    @Schema(required = true, description = "")
    @JsonProperty("cardAccounts")
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
