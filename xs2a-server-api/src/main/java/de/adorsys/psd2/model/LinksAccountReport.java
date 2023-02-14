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
import java.util.HashMap;
import java.util.Objects;

/**
 * LinksAccountReport
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class LinksAccountReport extends HashMap<String, HrefType>  {
  @JsonProperty("account")
  private HrefType account = null;

  @JsonProperty("first")
  private HrefType first = null;

  @JsonProperty("next")
  private HrefType next = null;

  @JsonProperty("previous")
  private HrefType previous = null;

  @JsonProperty("last")
  private HrefType last = null;

  public LinksAccountReport account(HrefType account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public HrefType getAccount() {
    return account;
  }

  public void setAccount(HrefType account) {
    this.account = account;
  }

  public LinksAccountReport first(HrefType first) {
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

  public LinksAccountReport next(HrefType next) {
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

  public LinksAccountReport previous(HrefType previous) {
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

  public LinksAccountReport last(HrefType last) {
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
    LinksAccountReport _linksAccountReport = (LinksAccountReport) o;
    return Objects.equals(this.account, _linksAccountReport.account) &&
        Objects.equals(this.first, _linksAccountReport.first) &&
        Objects.equals(this.next, _linksAccountReport.next) &&
        Objects.equals(this.previous, _linksAccountReport.previous) &&
        Objects.equals(this.last, _linksAccountReport.last) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(account, first, next, previous, last, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksAccountReport {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
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
