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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 400.
 */
@Schema(description = "NextGenPSD2 specific definition of reporting error information in case of a HTTP error code 400. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T12:59:08.054254+03:00[Europe/Kiev]")


public class Error400NGLTB   {
  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessage400LTB> tppMessages = null;

  @JsonProperty("_links")
  private LinksAll _links = null;

  public Error400NGLTB tppMessages(List<TppMessage400LTB> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public Error400NGLTB addTppMessagesItem(TppMessage400LTB tppMessagesItem) {
    if (this.tppMessages == null) {
      this.tppMessages = new ArrayList<>();
    }
    this.tppMessages.add(tppMessagesItem);
    return this;
  }

  /**
   * Get tppMessages
   * @return tppMessages
   **/
  @Schema(description = "")
      @Valid
    public List<TppMessage400LTB> getTppMessages() {
    return tppMessages;
  }

  public void setTppMessages(List<TppMessage400LTB> tppMessages) {
    this.tppMessages = tppMessages;
  }

  public Error400NGLTB _links(LinksAll _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
   **/
  @Schema(description = "")

    @Valid
    public LinksAll getLinks() {
    return _links;
  }

  public void setLinks(LinksAll _links) {
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
    Error400NGLTB error400NGLTB = (Error400NGLTB) o;
    return Objects.equals(this.tppMessages, error400NGLTB.tppMessages) &&
        Objects.equals(this._links, error400NGLTB._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(tppMessages, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error400NGLTB {\n");

    sb.append("    tppMessages: ").append(toIndentedString(tppMessages)).append("\n");
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
