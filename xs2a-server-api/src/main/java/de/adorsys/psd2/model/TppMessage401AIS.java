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
 * TppMessage401AIS
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class TppMessage401AIS   {
  @JsonProperty("category")
  private TppMessageCategory category = null;

  @JsonProperty("code")
  private String code = null;

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("text")
  private String text = null;

  public TppMessage401AIS category(TppMessageCategory category) {
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

  public TppMessage401AIS code(String code) {
    this.code = code;
    return this;
  }

    /**
     * Message codes defined for AIS for HTTP Error code 401 (UNAUTHORIZED).
     *
     * @return code
     **/
    @Schema(required = true, description = "Message codes defined for AIS for HTTP Error code 401 (UNAUTHORIZED).")
    @JsonProperty("code")
    @NotNull

    public String getCode() {
        return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public TppMessage401AIS path(String path) {
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

  public TppMessage401AIS text(String text) {
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
    TppMessage401AIS tppMessage401AIS = (TppMessage401AIS) o;
    return Objects.equals(this.category, tppMessage401AIS.category) &&
        Objects.equals(this.code, tppMessage401AIS.code) &&
        Objects.equals(this.path, tppMessage401AIS.path) &&
        Objects.equals(this.text, tppMessage401AIS.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, code, path, text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TppMessage401AIS {\n");

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
