package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * ClientMessageInformation400VOP
 */

@JsonTypeName("clientMessageInformation_400_VOP")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class ClientMessageInformation400VOP {

  private String category;

  private MessageCodeServiceUnspecific400 code;

  private Optional<String> path = Optional.empty();

  private Optional<@Size(max = 500) String> text = Optional.empty();

  public ClientMessageInformation400VOP() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ClientMessageInformation400VOP(String category, MessageCodeServiceUnspecific400 code) {
    this.category = category;
    this.code = code;
  }

  public ClientMessageInformation400VOP category(String category) {
    this.category = category;
    return this;
  }

  /**
   * Only \\\"ERROR\\\" or \\\"WARNING\\\" permitted
   * @return category
   */
  @NotNull
  @Schema(name = "category", description = "Only \\\"ERROR\\\" or \\\"WARNING\\\" permitted ", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public ClientMessageInformation400VOP code(MessageCodeServiceUnspecific400 code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
   */
  @NotNull @Valid
  @Schema(name = "code", requiredMode = Schema.RequiredMode.REQUIRED)
  @JsonProperty("code")
  public MessageCodeServiceUnspecific400 getCode() {
    return code;
  }

  public void setCode(MessageCodeServiceUnspecific400 code) {
    this.code = code;
  }

  public ClientMessageInformation400VOP path(String path) {
    this.path = Optional.ofNullable(path);
    return this;
  }

  /**
   * Get path
   * @return path
   */

  @Schema(name = "path", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("path")
  public Optional<String> getPath() {
    return path;
  }

  public void setPath(Optional<String> path) {
    this.path = path;
  }

  public ClientMessageInformation400VOP text(String text) {
    this.text = Optional.ofNullable(text);
    return this;
  }

  /**
   * Get text
   * @return text
   */

  @Schema(name = "text", example = "Text, maximum of 500 characters.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("text")
  public Optional<@Size(max = 500) String> getText() {
    return text;
  }

  public void setText(Optional<String> text) {
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
    ClientMessageInformation400VOP clientMessageInformation400VOP = (ClientMessageInformation400VOP) o;
    return Objects.equals(this.category, clientMessageInformation400VOP.category) &&
        Objects.equals(this.code, clientMessageInformation400VOP.code) &&
        Objects.equals(this.path, clientMessageInformation400VOP.path) &&
        Objects.equals(this.text, clientMessageInformation400VOP.text);
  }

  @Override
  public int hashCode() {
    return Objects.hash(category, code, path, text);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ClientMessageInformation400VOP {\n");
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

