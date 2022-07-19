package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP  defined extensions):  - &#x27;scaStatus&#x27;: The link to retrieve the scaStatus of the corresponding authorisation sub-resource. - &#x27;selectAuthenticationMethod&#x27;: This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there are several available authentication methods and if the PSU is already sufficiently authenticated.. If this link is contained, then there is also the data element \&quot;scaMethods\&quot; contained in the response body.
 */
@Schema(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP  defined extensions):  - 'scaStatus': The link to retrieve the scaStatus of the corresponding authorisation sub-resource. - 'selectAuthenticationMethod': This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there are several available authentication methods and if the PSU is already sufficiently authenticated.. If this link is contained, then there is also the data element \"scaMethods\" contained in the response body. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


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
  @Schema(description = "")

    @Valid
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
  @Schema(description = "")

    @Valid
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
    LinksUpdatePsuIdentification _linksUpdatePsuIdentification = (LinksUpdatePsuIdentification) o;
    return Objects.equals(this.scaStatus, _linksUpdatePsuIdentification.scaStatus) &&
        Objects.equals(this.selectAuthenticationMethod, _linksUpdatePsuIdentification.selectAuthenticationMethod) &&
        super.equals(o);
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
