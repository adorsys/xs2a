package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP  defined extensions):  - &#39;scaStatus&#39;: The link to retrieve the scaStatus of the corresponding authorisation sub-resource. - &#39;selectAuthenticationMethod&#39;: This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there are several available authentication methods and if the PSU is already sufficiently authenticated.. If this link is contained, then there is also the data element \&quot;scaMethods\&quot; contained in the response body.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP  defined extensions):  - 'scaStatus': The link to retrieve the scaStatus of the corresponding authorisation sub-resource. - 'selectAuthenticationMethod': This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there are several available authentication methods and if the PSU is already sufficiently authenticated.. If this link is contained, then there is also the data element \"scaMethods\" contained in the response body. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class LinksUpdatePsuIdentification extends HashMap<String, HrefType>  {
  @JsonProperty("scaStatus")
  private HrefType scaStatus = null;

  @JsonProperty("selectAuthenticationMethod")
  private HrefType selectAuthenticationMethod = null;

  public LinksUpdatePsuIdentification scaStatus(HrefType scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("scaStatus")
  public HrefType getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(HrefType scaStatus) {
    this.scaStatus = scaStatus;
  }

  public LinksUpdatePsuIdentification selectAuthenticationMethod(HrefType selectAuthenticationMethod) {
    this.selectAuthenticationMethod = selectAuthenticationMethod;
    return this;
  }

  /**
   * Get selectAuthenticationMethod
   * @return selectAuthenticationMethod
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("selectAuthenticationMethod")
  public HrefType getSelectAuthenticationMethod() {
    return selectAuthenticationMethod;
  }

  public void setSelectAuthenticationMethod(HrefType selectAuthenticationMethod) {
    this.selectAuthenticationMethod = selectAuthenticationMethod;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}
    if (!super.equals(o)) {
    return false;
    }
    LinksUpdatePsuIdentification _linksUpdatePsuIdentification = (LinksUpdatePsuIdentification) o;
    return Objects.equals(this.scaStatus, _linksUpdatePsuIdentification.scaStatus) &&
    Objects.equals(this.selectAuthenticationMethod, _linksUpdatePsuIdentification.selectAuthenticationMethod);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaStatus, selectAuthenticationMethod, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksUpdatePsuIdentification {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    selectAuthenticationMethod: ").append(toIndentedString(selectAuthenticationMethod)).append("\n");
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

