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
 * Standardised definition of reporting error information according to [RFC7807]  in case of a HTTP error code 405 for signing baskets.
 */
@Schema(description = "Standardised definition of reporting error information according to [RFC7807]  in case of a HTTP error code 405 for signing baskets. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class Error405SBS   {
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
  private List<Error405SBSAdditionalErrors> additionalErrors = null;

  @JsonProperty("_links")
  private LinksAll _links = null;

  public Error405SBS type(String type) {
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

  public Error405SBS title(String title) {
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

  public Error405SBS detail(String detail) {
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

  public Error405SBS code(String code) {
    this.code = code;
    return this;
  }

  /**
   * Message codes defined for SBS for HTTP Error code 405 (METHOD NOT ALLOWED).
   * @return code
   **/
  @Schema(required = true, description = "Message codes defined for SBS for HTTP Error code 405 (METHOD NOT ALLOWED).")
      @NotNull

    public String getCode() {
    return code;
  }

  public void setCode(String code) {
    this.code = code;
  }

  public Error405SBS additionalErrors(List<Error405SBSAdditionalErrors> additionalErrors) {
    this.additionalErrors = additionalErrors;
    return this;
  }

  public Error405SBS addAdditionalErrorsItem(Error405SBSAdditionalErrors additionalErrorsItem) {
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
    public List<Error405SBSAdditionalErrors> getAdditionalErrors() {
    return additionalErrors;
  }

  public void setAdditionalErrors(List<Error405SBSAdditionalErrors> additionalErrors) {
    this.additionalErrors = additionalErrors;
  }

  public Error405SBS _links(LinksAll _links) {
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
    Error405SBS error405SBS = (Error405SBS) o;
    return Objects.equals(this.type, error405SBS.type) &&
        Objects.equals(this.title, error405SBS.title) &&
        Objects.equals(this.detail, error405SBS.detail) &&
        Objects.equals(this.code, error405SBS.code) &&
        Objects.equals(this.additionalErrors, error405SBS.additionalErrors) &&
        Objects.equals(this._links, error405SBS._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, detail, code, additionalErrors, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error405SBS {\n");

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
