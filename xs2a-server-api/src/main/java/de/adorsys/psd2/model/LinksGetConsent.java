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
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP.  Links of type \&quot;account\&quot; and/or \&quot;cardAccount\&quot;, depending on the nature of the consent.
 */
@Schema(description = "A list of hyperlinks to be recognised by the TPP.  Links of type \"account\" and/or \"cardAccount\", depending on the nature of the consent. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class LinksGetConsent extends HashMap<String, HrefType>  {
  @JsonProperty("account")
  private HrefType account = null;

  @JsonProperty("card-account")
  private HrefType cardAccount = null;

  public LinksGetConsent account(HrefType account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
   **/
  @Schema(description = "")

    @Valid
    public HrefType getAccount() {
    return account;
  }

  public void setAccount(HrefType account) {
    this.account = account;
  }

  public LinksGetConsent cardAccount(HrefType cardAccount) {
    this.cardAccount = cardAccount;
    return this;
  }

  /**
   * Get cardAccount
   * @return cardAccount
   **/
  @Schema(description = "")

    @Valid
    public HrefType getCardAccount() {
    return cardAccount;
  }

  public void setCardAccount(HrefType cardAccount) {
    this.cardAccount = cardAccount;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksGetConsent _linksGetConsent = (LinksGetConsent) o;
    return Objects.equals(this.account, _linksGetConsent.account) &&
        Objects.equals(this.cardAccount, _linksGetConsent.cardAccount) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, cardAccount, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksGetConsent {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
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
