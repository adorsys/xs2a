package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.Error400LTBAdditionalErrors;
import de.adorsys.psd2.model.MessageCode400LTB;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Standardised definition of reporting error information according to [RFC7807] in case of a HTTP error code 400 for Trusted Beneficiaries.
 */
@ApiModel(description = "Standardised definition of reporting error information according to [RFC7807] in case of a HTTP error code 400 for Trusted Beneficiaries. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-11-12T17:35:11.808068+02:00[Europe/Kiev]")

public class Error400LTB   {
  @JsonProperty("type")
  private String type = null;

  @JsonProperty("title")
  private String title = null;

  @JsonProperty("detail")
  private String detail = null;

  @JsonProperty("code")
  private MessageCode400LTB code = null;

  @JsonProperty("additionalErrors")
  @Valid
  private List<Error400LTBAdditionalErrors> additionalErrors = null;

  @JsonProperty("_links")
  private Map _links = null;

  public Error400LTB type(String type) {
    this.type = type;
    return this;
  }

  /**
   * A URI reference [RFC3986] that identifies the problem type. Remark For Future: These URI will be provided by NextGenPSD2 in future.
   * @return type
  **/
  @ApiModelProperty(required = true, value = "A URI reference [RFC3986] that identifies the problem type. Remark For Future: These URI will be provided by NextGenPSD2 in future. ")
  @NotNull

@Size(max=70)

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public Error400LTB title(String title) {
    this.title = title;
    return this;
  }

  /**
   * Short human readable description of error type. Could be in local language. To be provided by ASPSPs.
   * @return title
  **/
  @ApiModelProperty(value = "Short human readable description of error type. Could be in local language. To be provided by ASPSPs. ")

@Size(max=70)

  @JsonProperty("title")
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public Error400LTB detail(String detail) {
    this.detail = detail;
    return this;
  }

  /**
   * Detailed human readable text specific to this instance of the error. XPath might be used to point to the issue generating the error in addition. Remark for Future: In future, a dedicated field might be introduced for the XPath.
   * @return detail
  **/
  @ApiModelProperty(value = "Detailed human readable text specific to this instance of the error. XPath might be used to point to the issue generating the error in addition. Remark for Future: In future, a dedicated field might be introduced for the XPath. ")

@Size(max=500)

  @JsonProperty("detail")
  public String getDetail() {
    return detail;
  }

  public void setDetail(String detail) {
    this.detail = detail;
  }

  public Error400LTB code(MessageCode400LTB code) {
    this.code = code;
    return this;
  }

  /**
   * Get code
   * @return code
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("code")
  public MessageCode400LTB getCode() {
    return code;
  }

  public void setCode(MessageCode400LTB code) {
    this.code = code;
  }

  public Error400LTB additionalErrors(List<Error400LTBAdditionalErrors> additionalErrors) {
    this.additionalErrors = additionalErrors;
    return this;
  }

  public Error400LTB addAdditionalErrorsItem(Error400LTBAdditionalErrors additionalErrorsItem) {
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
  @ApiModelProperty(value = "Array of Error Information Blocks.  Might be used if more than one error is to be communicated ")

  @Valid


  @JsonProperty("additionalErrors")
  public List<Error400LTBAdditionalErrors> getAdditionalErrors() {
    return additionalErrors;
  }

  public void setAdditionalErrors(List<Error400LTBAdditionalErrors> additionalErrors) {
    this.additionalErrors = additionalErrors;
  }

  public Error400LTB _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("_links")
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    Error400LTB error400LTB = (Error400LTB) o;
    return Objects.equals(this.type, error400LTB.type) &&
    Objects.equals(this.title, error400LTB.title) &&
    Objects.equals(this.detail, error400LTB.detail) &&
    Objects.equals(this.code, error400LTB.code) &&
    Objects.equals(this.additionalErrors, error400LTB.additionalErrors) &&
    Objects.equals(this._links, error400LTB._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, title, detail, code, additionalErrors, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Error400LTB {\n");

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

