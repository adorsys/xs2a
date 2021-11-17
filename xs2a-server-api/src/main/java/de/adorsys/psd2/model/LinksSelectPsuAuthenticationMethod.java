package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in  the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.   **Remark:** This method can be applied before or after PSU identification.  This leads to many possible hyperlink responses. Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):  - &#39;scaRedirect&#39;:    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to    redirect the PSU browser. - &#39;scaOAuth&#39;:    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the    configuration of the Authorisation Server can be retrieved.    The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. * &#39;confirmation&#39;:    Might be added by the ASPSP if either the \&quot;scaRedirect\&quot; or \&quot;scaOAuth\&quot; hyperlink is returned    in the same response message.    This hyperlink defines the URL to the resource which needs to be updated with      * a confirmation code as retrieved after the plain redirect authentication process with the ASPSP authentication server or     * an access token as retrieved by submitting an authorization code after the integrated OAuth based authentication process with the ASPSP authentication server. - &#39;updatePsuIdentification&#39;:    The link to the authorisation or cancellation authorisation sub-resource,    where PSU identification data needs to be uploaded. - &#39;updatePsuAuthentication&#39;:   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication data needs to be uploaded.   - &#39;updateEncryptedPsuAuthentication&#39;:   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication encrypted data needs to be uploaded. - &#39;updateAdditionalPsuAuthentication&#39;:     The link to the payment initiation or account information resource,      which needs to be updated by an additional PSU password.  - &#39;updateAdditionalEncryptedPsuAuthentication&#39;:      The link to the payment initiation or account information resource,      which needs to be updated by an additional encrypted PSU password.  - &#39;authoriseTransaction&#39;:   The link to the authorisation or cancellation authorisation sub-resource,    where the authorisation data has to be uploaded, e.g. the TOP received by SMS.  - &#39;scaStatus&#39;:    The link to retrieve the scaStatus of the corresponding authorisation sub-resource.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in  the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.   **Remark:** This method can be applied before or after PSU identification.  This leads to many possible hyperlink responses. Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):  - 'scaRedirect':    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to    redirect the PSU browser. - 'scaOAuth':    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the    configuration of the Authorisation Server can be retrieved.    The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. * 'confirmation':    Might be added by the ASPSP if either the \"scaRedirect\" or \"scaOAuth\" hyperlink is returned    in the same response message.    This hyperlink defines the URL to the resource which needs to be updated with      * a confirmation code as retrieved after the plain redirect authentication process with the ASPSP authentication server or     * an access token as retrieved by submitting an authorization code after the integrated OAuth based authentication process with the ASPSP authentication server. - 'updatePsuIdentification':    The link to the authorisation or cancellation authorisation sub-resource,    where PSU identification data needs to be uploaded. - 'updatePsuAuthentication':   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication data needs to be uploaded.   - 'updateEncryptedPsuAuthentication':   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication encrypted data needs to be uploaded. - 'updateAdditionalPsuAuthentication':     The link to the payment initiation or account information resource,      which needs to be updated by an additional PSU password.  - 'updateAdditionalEncryptedPsuAuthentication':      The link to the payment initiation or account information resource,      which needs to be updated by an additional encrypted PSU password.  - 'authoriseTransaction':   The link to the authorisation or cancellation authorisation sub-resource,    where the authorisation data has to be uploaded, e.g. the TOP received by SMS.  - 'scaStatus':    The link to retrieve the scaStatus of the corresponding authorisation sub-resource. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class LinksSelectPsuAuthenticationMethod extends HashMap<String, HrefType>  {
  @JsonProperty("scaRedirect")
  private HrefType scaRedirect = null;

  @JsonProperty("scaOAuth")
  private HrefType scaOAuth = null;

  @JsonProperty("confirmation")
  private HrefType confirmation = null;

  @JsonProperty("updatePsuIdentification")
  private HrefType updatePsuIdentification = null;

  @JsonProperty("updatePsuAuthentication")
  private HrefType updatePsuAuthentication = null;

  @JsonProperty("updateAdditionalPsuAuthentication")
  private HrefType updateAdditionalPsuAuthentication = null;

  @JsonProperty("updateAdditionalEncryptedPsuAuthentication")
  private HrefType updateAdditionalEncryptedPsuAuthentication = null;

  @JsonProperty("authoriseTransaction")
  private HrefType authoriseTransaction = null;

  @JsonProperty("scaStatus")
  private HrefType scaStatus = null;

  public LinksSelectPsuAuthenticationMethod scaRedirect(HrefType scaRedirect) {
    this.scaRedirect = scaRedirect;
    return this;
  }

  /**
   * Get scaRedirect
   * @return scaRedirect
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("scaRedirect")
  public HrefType getScaRedirect() {
    return scaRedirect;
  }

  public void setScaRedirect(HrefType scaRedirect) {
    this.scaRedirect = scaRedirect;
  }

  public LinksSelectPsuAuthenticationMethod scaOAuth(HrefType scaOAuth) {
    this.scaOAuth = scaOAuth;
    return this;
  }

  /**
   * Get scaOAuth
   * @return scaOAuth
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("scaOAuth")
  public HrefType getScaOAuth() {
    return scaOAuth;
  }

  public void setScaOAuth(HrefType scaOAuth) {
    this.scaOAuth = scaOAuth;
  }

  public LinksSelectPsuAuthenticationMethod confirmation(HrefType confirmation) {
    this.confirmation = confirmation;
    return this;
  }

  /**
   * Get confirmation
   * @return confirmation
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("confirmation")
  public HrefType getConfirmation() {
    return confirmation;
  }

  public void setConfirmation(HrefType confirmation) {
    this.confirmation = confirmation;
  }

  public LinksSelectPsuAuthenticationMethod updatePsuIdentification(HrefType updatePsuIdentification) {
    this.updatePsuIdentification = updatePsuIdentification;
    return this;
  }

  /**
   * Get updatePsuIdentification
   * @return updatePsuIdentification
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("updatePsuIdentification")
  public HrefType getUpdatePsuIdentification() {
    return updatePsuIdentification;
  }

  public void setUpdatePsuIdentification(HrefType updatePsuIdentification) {
    this.updatePsuIdentification = updatePsuIdentification;
  }

  public LinksSelectPsuAuthenticationMethod updatePsuAuthentication(HrefType updatePsuAuthentication) {
    this.updatePsuAuthentication = updatePsuAuthentication;
    return this;
  }

  /**
   * Get updatePsuAuthentication
   * @return updatePsuAuthentication
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("updatePsuAuthentication")
  public HrefType getUpdatePsuAuthentication() {
    return updatePsuAuthentication;
  }

  public void setUpdatePsuAuthentication(HrefType updatePsuAuthentication) {
    this.updatePsuAuthentication = updatePsuAuthentication;
  }

  public LinksSelectPsuAuthenticationMethod updateAdditionalPsuAuthentication(HrefType updateAdditionalPsuAuthentication) {
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

  public LinksSelectPsuAuthenticationMethod updateAdditionalEncryptedPsuAuthentication(HrefType updateAdditionalEncryptedPsuAuthentication) {
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

  public LinksSelectPsuAuthenticationMethod authoriseTransaction(HrefType authoriseTransaction) {
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

  public LinksSelectPsuAuthenticationMethod scaStatus(HrefType scaStatus) {
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
    LinksSelectPsuAuthenticationMethod _linksSelectPsuAuthenticationMethod = (LinksSelectPsuAuthenticationMethod) o;
    return Objects.equals(this.scaRedirect, _linksSelectPsuAuthenticationMethod.scaRedirect) &&
    Objects.equals(this.scaOAuth, _linksSelectPsuAuthenticationMethod.scaOAuth) &&
    Objects.equals(this.confirmation, _linksSelectPsuAuthenticationMethod.confirmation) &&
    Objects.equals(this.updatePsuIdentification, _linksSelectPsuAuthenticationMethod.updatePsuIdentification) &&
    Objects.equals(this.updatePsuAuthentication, _linksSelectPsuAuthenticationMethod.updatePsuAuthentication) &&
    Objects.equals(this.updateAdditionalPsuAuthentication, _linksSelectPsuAuthenticationMethod.updateAdditionalPsuAuthentication) &&
    Objects.equals(this.updateAdditionalEncryptedPsuAuthentication, _linksSelectPsuAuthenticationMethod.updateAdditionalEncryptedPsuAuthentication) &&
    Objects.equals(this.authoriseTransaction, _linksSelectPsuAuthenticationMethod.authoriseTransaction) &&
    Objects.equals(this.scaStatus, _linksSelectPsuAuthenticationMethod.scaStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaRedirect, scaOAuth, confirmation, updatePsuIdentification, updatePsuAuthentication, updateAdditionalPsuAuthentication, updateAdditionalEncryptedPsuAuthentication, authoriseTransaction, scaStatus, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksSelectPsuAuthenticationMethod {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
    sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
    sb.append("    confirmation: ").append(toIndentedString(confirmation)).append("\n");
    sb.append("    updatePsuIdentification: ").append(toIndentedString(updatePsuIdentification)).append("\n");
    sb.append("    updatePsuAuthentication: ").append(toIndentedString(updatePsuAuthentication)).append("\n");
    sb.append("    updateAdditionalPsuAuthentication: ").append(toIndentedString(updateAdditionalPsuAuthentication)).append("\n");
    sb.append("    updateAdditionalEncryptedPsuAuthentication: ").append(toIndentedString(updateAdditionalEncryptedPsuAuthentication)).append("\n");
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

