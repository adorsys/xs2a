package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Standardised definition of reporting error information according to [RFC7807]  in case of a HTTP error code 401 for PIS.
 */
@Schema(description = "Standardised definition of reporting error information according to [RFC7807]  in case of a HTTP error code 401 for PIS. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class Error401PIS   {
  @JsonProperty("type")
  private String type = null;

  @JsonProperty("title")
  private String title = null;

  @JsonProperty("detail")
  private String detail = null;

  @JsonProperty("code")
  private String code = null;

  @JsonProperty("additionalErrors")
  @Valid
  private List<Error401PISAdditionalErrors> additionalErrors = null;

  @JsonProperty("_links")
  private LinksAll _links = null;

  public Error401PIS type(String type) {
    this.type = type;
    return this;
  }

  /**
   * A URI reference [RFC3986] that identifies the problem type.  Remark For Future: These URI will be provided by NextGenPSD2 in future.
   * @return type
   **/
  @Schema(required = true, description = "A URI reference [RFC3986] that identifies the problem type.  Remark For Future: These URI will be provided by NextGenPSD2 in future. ")
      @NotNull

  @Size(max=70)   public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Error401PIS title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Short human readable description of error type.  Could be in local language.  To be provided by ASPSPs.
   * @return title
   **/
  @Schema(description = "Short human readable description of error type.  Could be in local language.  To be provided by ASPSPs. ")

  @Size(max=70)   public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Error401PIS detail(String detail) {
    this.detail = detail;
    return this;
  }

  /**
   * Detailed human readable text specific to this instance of the error.  XPath might be used to point to the issue generating the error in addition. Remark for Future: In future, a dedicated field might be introduced for the XPath.
   * @return detail
   **/
  @Schema(description = "Detailed human readable text specific to this instance of the error.  XPath might be used to point to the issue generating the error in addition. Remark for Future: In future, a dedicated field might be introduced for the XPath. ")

  @Size(max=500)   public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public Error401PIS code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Message codes defined for PIS for HTTP Error code 401 (UNAUTHORIZED).
   * @return code
   **/
  @Schema(required = true, description = "Message codes defined for PIS for HTTP Error code 401 (UNAUTHORIZED).")
      @NotNull

    public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Error401PIS additionalErrors(List<Error401PISAdditionalErrors> additionalErrors) {
    this.additionalErrors = additionalErrors;
    return this;
  }

  public Error401PIS addAdditionalErrorsItem(Error401PISAdditionalErrors additionalErrorsItem) {
    if (this.additionalErrors == null) {
      this.additionalErrors = new ArrayList<>();
    }
    this.additionalErrors.add(additionalErrorsItem);
    return this;
  }

  /**
   * Array of Error Information Blocks.  Might be used if more than one error is to be communicated
   * @return additionalErrors
   **/
  @Schema(description = "Array of Error Information Blocks.  Might be used if more than one error is to be communicated ")
      @Valid
    public List<Error401PISAdditionalErrors> getAdditionalErrors() {
    return additionalErrors;
  }

  public void setAdditionalErrors(List<Error401PISAdditionalErrors> additionalErrors) {
    this.additionalErrors = additionalErrors;
  }

  public Error401PIS _links(LinksAll _links) {
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
    Error401PIS error401PIS = (Error401PIS) o;
    return Objects.equals(this.type, error401PIS.type) &&
        Objects.equals(this.title, error401PIS.title) &&
        Objects.equals(this.detail, error401PIS.detail) &&
        Objects.equals(this.code, error401PIS.code) &&
        Objects.equals(this.additionalErrors, error401PIS.additionalErrors) &&
        Objects.equals(this._links, error401PIS._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, detail, code, additionalErrors, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error401PIS {\n");

    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    title: ").append(toIndentedString(title)).append("\n");
    sb.append("    detail: ").append(toIndentedString(detail)).append("\n");
    sb.append("    code: ").append(toIndentedString(code)).append("\n");
    sb.append("    additionalErrors: ").append(toIndentedString(additionalErrors)).append("\n");
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
