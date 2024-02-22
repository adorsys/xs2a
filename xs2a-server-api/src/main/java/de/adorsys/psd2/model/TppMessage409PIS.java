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
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * TppMessage409PIS
 */
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class TppMessage409PIS   {
  @JsonProperty("category")
  private TppMessageCategory category = null;

  @JsonProperty("code")
  private String code = null;

  @JsonProperty("path")
  private String path = null;

  @JsonProperty("text")
  private String text = null;

  public TppMessage409PIS category(TppMessageCategory category) {
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

  public TppMessage409PIS code(String code) {
    this.code = code;
    return this;
  }

    /**
     * Message codes defined for PIS for HTTP Error code 409 (CONFLICT).
     *
     * @return code
     **/
    @Schema(required = true, description = "Message codes defined for PIS for HTTP Error code 409 (CONFLICT).")
    @JsonProperty("code")
    @NotNull

    public String getCode() {
        return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public TppMessage409PIS path(String path) {
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

  public TppMessage409PIS text(String text) {
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
    TppMessage409PIS tppMessage409PIS = (TppMessage409PIS) o;
    return Objects.equals(this.category, tppMessage409PIS.category) &&
        Objects.equals(this.code, tppMessage409PIS.code) &&
        Objects.equals(this.path, tppMessage409PIS.path) &&
        Objects.equals(this.text, tppMessage409PIS.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, code, path, text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TppMessage409PIS {\n");

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
