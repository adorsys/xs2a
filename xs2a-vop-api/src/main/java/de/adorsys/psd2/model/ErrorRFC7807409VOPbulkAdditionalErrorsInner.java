package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * This is a data element to support the declaration of additional errors in the context of [RFC7807].
 */

@Schema(name = "error_RFC7807_409_VOPbulk_additionalErrors_inner", description = "This is a data element to support the declaration of additional errors in the context of [RFC7807].")
@JsonTypeName("error_RFC7807_409_VOPbulk_additionalErrors_inner")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class ErrorRFC7807409VOPbulkAdditionalErrorsInner {

  private Optional<@Size(max = 70) String> title = Optional.empty();

  private Optional<@Size(max = 500) String> detail = Optional.empty();

  private MessageCodeServiceUnspecific409 code;

  public ErrorRFC7807409VOPbulkAdditionalErrorsInner() {
    super();
  }

  /**
   * Constructor with only required parameters
   */
  public ErrorRFC7807409VOPbulkAdditionalErrorsInner(MessageCodeServiceUnspecific409 code) {
    this.code = code;
  }

  public ErrorRFC7807409VOPbulkAdditionalErrorsInner title(String title) {
    this.title = Optional.ofNullable(title);
    return this;
  }

  /**
   * Get title
   * @return title
   */

  @Schema(name = "title", example = "Text, maximum of 70 characters.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("title")
  public Optional<@Size(max = 70) String> getTitle() {
    return title;
  }

  public void setTitle(Optional<String> title) {
    this.title = title;
  }

  public ErrorRFC7807409VOPbulkAdditionalErrorsInner detail(String detail) {
    this.detail = Optional.ofNullable(detail);
    return this;
  }

  /**
   * Get detail
   * @return detail
   */

  @Schema(name = "detail", example = "Text, maximum of 500 characters.", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("detail")
  public Optional<@Size(max = 500) String> getDetail() {
    return detail;
  }

  public void setDetail(Optional<String> detail) {
    this.detail = detail;
  }

  public ErrorRFC7807409VOPbulkAdditionalErrorsInner code(MessageCodeServiceUnspecific409 code) {
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
  public MessageCodeServiceUnspecific409 getCode() {
    return code;
  }

  public void setCode(MessageCodeServiceUnspecific409 code) {
    this.code = code;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ErrorRFC7807409VOPbulkAdditionalErrorsInner errorRFC7807409VOPbulkAdditionalErrorsInner = (ErrorRFC7807409VOPbulkAdditionalErrorsInner) o;
    return Objects.equals(this.title, errorRFC7807409VOPbulkAdditionalErrorsInner.title) &&
        Objects.equals(this.detail, errorRFC7807409VOPbulkAdditionalErrorsInner.detail) &&
        Objects.equals(this.code, errorRFC7807409VOPbulkAdditionalErrorsInner.code);
  }

  @Override
  public int hashCode() {
    return Objects.hash(title, detail, code);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ErrorRFC7807409VOPbulkAdditionalErrorsInner {\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
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

