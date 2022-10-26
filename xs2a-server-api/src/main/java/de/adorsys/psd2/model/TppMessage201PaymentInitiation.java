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
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * TppMessage201PaymentInitiation
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class TppMessage201PaymentInitiation   {
  @JsonProperty("category")
  private TppMessageCategory category = null;

  @JsonProperty("code")
  private MessageCode201PaymentInitiation code = null;

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("text")
  private String text = null;

  public TppMessage201PaymentInitiation category(TppMessageCategory category) {
    this.category = category;
    return this;
  }

    /**
     * Get category
     *
     * @return category
     **/
    @Schema(required = true, description = "")
    @JsonProperty("category")
    @NotNull

    @Valid
    public TppMessageCategory getCategory() {
        return category;
    }

  public void setCategory(TppMessageCategory category) {
    this.category = category;
  }

  public TppMessage201PaymentInitiation code(MessageCode201PaymentInitiation code) {
    this.code = code;
    return this;
  }

    /**
     * Get code
     *
     * @return code
     **/
    @Schema(required = true, description = "")
    @JsonProperty("code")
    @NotNull

    @Valid
    public MessageCode201PaymentInitiation getCode() {
        return code;
  }

  public void setCode(MessageCode201PaymentInitiation code) {
    this.code = code;
  }

  public TppMessage201PaymentInitiation path(String path) {
    this.path = path;
      return this;
  }

    /**
     * Get path
     *
     * @return path
     **/
    @Schema(description = "")
    @JsonProperty("path")

    public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public TppMessage201PaymentInitiation text(String text) {
    this.text = text;
      return this;
  }

    /**
     * Additional explaining text to the TPP.
     *
     * @return text
     **/
    @Schema(description = "Additional explaining text to the TPP.")
    @JsonProperty("text")

    @Size(max = 500)
    public String getText() {
    return text;
  }

  public void setText(String text) {
    this.text = text;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TppMessage201PaymentInitiation tppMessage201PaymentInitiation = (TppMessage201PaymentInitiation) o;
    return Objects.equals(this.category, tppMessage201PaymentInitiation.category) &&
        Objects.equals(this.code, tppMessage201PaymentInitiation.code) &&
        Objects.equals(this.path, tppMessage201PaymentInitiation.path) &&
        Objects.equals(this.text, tppMessage201PaymentInitiation.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, code, path, text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TppMessage201PaymentInitiation {\n");

    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    text: ").append(toIndentedString(text)).append("\n");
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
