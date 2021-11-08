package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. Might be contained, if several authentication methods  are available for the PSU. Type of links admitted in this response:   * &#39;updateAdditionalPsuAuthentication&#39;:     The link to the payment initiation or account information resource,      which needs to be updated by an additional PSU password.      This link is only contained in rare cases,      where such additional passwords are needed for PSU authentications.   * &#39;updateAdditionalEncryptedPsuAuthentication&#39;:      The link to the payment initiation or account information resource,      which needs to be updated by an additional encrypted PSU password.      This link is only contained in rare cases, where such additional passwords are needed for PSU authentications.   * &#39;selectAuthenticationMethod&#39;:      This is a link to a resource, where the TPP can select the applicable second factor authentication      methods for the PSU, if there were several available authentication methods.      This link is only contained, if the PSU is already identified or authenticated with the first relevant      factor or alternatively an access token, if SCA is required and if the PSU has a choice between different      authentication methods.      If this link is contained, then there is also the data element &#39;scaMethods&#39; contained in the response body.   * &#39;authoriseTransaction&#39;:      The link to the resource, where the \&quot;Transaction authorisation request\&quot; is sent to.      This is the link to the resource which will authorise the transaction by checking the SCA authentication      data within the Embedded SCA approach.   * &#39;scaStatus&#39;:      The link to retrieve the scaStatus of the corresponding authorisation sub-resource.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. Might be contained, if several authentication methods  are available for the PSU. Type of links admitted in this response:   * 'updateAdditionalPsuAuthentication':     The link to the payment initiation or account information resource,      which needs to be updated by an additional PSU password.      This link is only contained in rare cases,      where such additional passwords are needed for PSU authentications.   * 'updateAdditionalEncryptedPsuAuthentication':      The link to the payment initiation or account information resource,      which needs to be updated by an additional encrypted PSU password.      This link is only contained in rare cases, where such additional passwords are needed for PSU authentications.   * 'selectAuthenticationMethod':      This is a link to a resource, where the TPP can select the applicable second factor authentication      methods for the PSU, if there were several available authentication methods.      This link is only contained, if the PSU is already identified or authenticated with the first relevant      factor or alternatively an access token, if SCA is required and if the PSU has a choice between different      authentication methods.      If this link is contained, then there is also the data element 'scaMethods' contained in the response body.   * 'authoriseTransaction':      The link to the resource, where the \"Transaction authorisation request\" is sent to.      This is the link to the resource which will authorise the transaction by checking the SCA authentication      data within the Embedded SCA approach.   * 'scaStatus':      The link to retrieve the scaStatus of the corresponding authorisation sub-resource. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class LinksUpdatePsuAuthentication extends HashMap<String, HrefType>  {
  @JsonProperty("updateAdditionalPsuAuthentication")
  private HrefType updateAdditionalPsuAuthentication = null;

  @JsonProperty("updateAdditionalEncryptedPsuAuthentication")
  private HrefType updateAdditionalEncryptedPsuAuthentication = null;

  @JsonProperty("selectAuthenticationMethod")
  private HrefType selectAuthenticationMethod = null;

  @JsonProperty("authoriseTransaction")
  private HrefType authoriseTransaction = null;

  @JsonProperty("scaStatus")
  private HrefType scaStatus = null;

  public LinksUpdatePsuAuthentication updateAdditionalPsuAuthentication(HrefType updateAdditionalPsuAuthentication) {
    this.updateAdditionalPsuAuthentication = updateAdditionalPsuAuthentication;
    return this;
  }

  /**
   * Get updateAdditionalPsuAuthentication
   * @return updateAdditionalPsuAuthentication
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("updateAdditionalPsuAuthentication")
  public HrefType getUpdateAdditionalPsuAuthentication() {
    return updateAdditionalPsuAuthentication;
  }

  public void setUpdateAdditionalPsuAuthentication(HrefType updateAdditionalPsuAuthentication) {
    this.updateAdditionalPsuAuthentication = updateAdditionalPsuAuthentication;
  }

  public LinksUpdatePsuAuthentication updateAdditionalEncryptedPsuAuthentication(HrefType updateAdditionalEncryptedPsuAuthentication) {
    this.updateAdditionalEncryptedPsuAuthentication = updateAdditionalEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get updateAdditionalEncryptedPsuAuthentication
   * @return updateAdditionalEncryptedPsuAuthentication
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("updateAdditionalEncryptedPsuAuthentication")
  public HrefType getUpdateAdditionalEncryptedPsuAuthentication() {
    return updateAdditionalEncryptedPsuAuthentication;
  }

  public void setUpdateAdditionalEncryptedPsuAuthentication(HrefType updateAdditionalEncryptedPsuAuthentication) {
    this.updateAdditionalEncryptedPsuAuthentication = updateAdditionalEncryptedPsuAuthentication;
  }

  public LinksUpdatePsuAuthentication selectAuthenticationMethod(HrefType selectAuthenticationMethod) {
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

  public LinksUpdatePsuAuthentication authoriseTransaction(HrefType authoriseTransaction) {
    this.authoriseTransaction = authoriseTransaction;
    return this;
  }

  /**
   * Get authoriseTransaction
   * @return authoriseTransaction
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("authoriseTransaction")
  public HrefType getAuthoriseTransaction() {
    return authoriseTransaction;
  }

  public void setAuthoriseTransaction(HrefType authoriseTransaction) {
    this.authoriseTransaction = authoriseTransaction;
  }

  public LinksUpdatePsuAuthentication scaStatus(HrefType scaStatus) {
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
    LinksUpdatePsuAuthentication _linksUpdatePsuAuthentication = (LinksUpdatePsuAuthentication) o;
    return Objects.equals(this.updateAdditionalPsuAuthentication, _linksUpdatePsuAuthentication.updateAdditionalPsuAuthentication) &&
    Objects.equals(this.updateAdditionalEncryptedPsuAuthentication, _linksUpdatePsuAuthentication.updateAdditionalEncryptedPsuAuthentication) &&
    Objects.equals(this.selectAuthenticationMethod, _linksUpdatePsuAuthentication.selectAuthenticationMethod) &&
    Objects.equals(this.authoriseTransaction, _linksUpdatePsuAuthentication.authoriseTransaction) &&
    Objects.equals(this.scaStatus, _linksUpdatePsuAuthentication.scaStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(updateAdditionalPsuAuthentication, updateAdditionalEncryptedPsuAuthentication, selectAuthenticationMethod, authoriseTransaction, scaStatus, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksUpdatePsuAuthentication {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    updateAdditionalPsuAuthentication: ").append(toIndentedString(updateAdditionalPsuAuthentication)).append("\n");
    sb.append("    updateAdditionalEncryptedPsuAuthentication: ").append(toIndentedString(updateAdditionalEncryptedPsuAuthentication)).append("\n");
    sb.append("    selectAuthenticationMethod: ").append(toIndentedString(selectAuthenticationMethod)).append("\n");
    sb.append("    authoriseTransaction: ").append(toIndentedString(authoriseTransaction)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
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

