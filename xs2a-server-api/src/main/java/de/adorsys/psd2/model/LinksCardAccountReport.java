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
import java.util.HashMap;
import java.util.Objects;

/**
 * LinksCardAccountReport
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class LinksCardAccountReport extends HashMap<String, HrefType>  {
  @JsonProperty("cardAccount")
  private HrefType cardAccount = null;

  @JsonProperty("card")
  private HrefType card = null;

  @JsonProperty("first")
  private HrefType first = null;

  @JsonProperty("next")
  private HrefType next = null;

  @JsonProperty("previous")
  private HrefType previous = null;

  @JsonProperty("last")
  private HrefType last = null;

  public LinksCardAccountReport cardAccount(HrefType cardAccount) {
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

  public LinksCardAccountReport card(HrefType card) {
    this.card = card;
    return this;
  }

  /**
   * Get card
   * @return card
   **/
  @Schema(description = "")

    @Valid
    public HrefType getCard() {
    return card;
  }

  public void setCard(HrefType card) {
    this.card = card;
  }

  public LinksCardAccountReport first(HrefType first) {
    this.first = first;
    return this;
  }

  /**
   * Get first
   * @return first
   **/
  @Schema(description = "")

    @Valid
    public HrefType getFirst() {
    return first;
  }

  public void setFirst(HrefType first) {
    this.first = first;
  }

  public LinksCardAccountReport next(HrefType next) {
    this.next = next;
    return this;
  }

  /**
   * Get next
   * @return next
   **/
  @Schema(description = "")

    @Valid
    public HrefType getNext() {
    return next;
  }

  public void setNext(HrefType next) {
    this.next = next;
  }

  public LinksCardAccountReport previous(HrefType previous) {
    this.previous = previous;
    return this;
  }

  /**
   * Get previous
   * @return previous
   **/
  @Schema(description = "")

    @Valid
    public HrefType getPrevious() {
    return previous;
  }

  public void setPrevious(HrefType previous) {
    this.previous = previous;
  }

  public LinksCardAccountReport last(HrefType last) {
    this.last = last;
    return this;
  }

  /**
   * Get last
   * @return last
   **/
  @Schema(description = "")

    @Valid
    public HrefType getLast() {
    return last;
  }

  public void setLast(HrefType last) {
    this.last = last;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksCardAccountReport _linksCardAccountReport = (LinksCardAccountReport) o;
    return Objects.equals(this.cardAccount, _linksCardAccountReport.cardAccount) &&
        Objects.equals(this.card, _linksCardAccountReport.card) &&
        Objects.equals(this.first, _linksCardAccountReport.first) &&
        Objects.equals(this.next, _linksCardAccountReport.next) &&
        Objects.equals(this.previous, _linksCardAccountReport.previous) &&
        Objects.equals(this.last, _linksCardAccountReport.last) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cardAccount, card, first, next, previous, last, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksCardAccountReport {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
    sb.append("    card: ").append(toIndentedString(card)).append("\n");
    sb.append("    first: ").append(toIndentedString(first)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("    last: ").append(toIndentedString(last)).append("\n");
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
