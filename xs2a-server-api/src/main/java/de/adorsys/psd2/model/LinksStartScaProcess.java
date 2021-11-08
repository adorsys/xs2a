package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the  response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):  - &#39;scaRedirect&#39;:    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to    redirect the PSU browser. - &#39;scaOAuth&#39;:    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the configuration of the Authorisation Server can be retrieved. The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. * &#39;confirmation&#39;:    Might be added by the ASPSP if either the \&quot;scaRedirect\&quot; or \&quot;scaOAuth\&quot; hyperlink is returned    in the same response message.    This hyperlink defines the URL to the resource which needs to be updated with      * a confirmation code as retrieved after the plain redirect authentication process with the ASPSP authentication server or     * an access token as retrieved by submitting an authorization code after the integrated OAuth based authentication process with the ASPSP authentication server. - &#39;updatePsuIdentification&#39;:    The link to the authorisation or cancellation authorisation sub-resource,    where PSU identification data needs to be uploaded. - &#39;startAuthorisationWithPsuAuthentication&#39;:   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication data needs to be uploaded. - &#39;startAuthorisationWithEncryptedPsuAuthentication&#39;:     Same as startAuthorisactionWithPsuAuthentication where the authentication data need to be encrypted on      application layer in uploading. - &#39;selectAuthenticationMethod&#39;:   The link to the authorisation or cancellation authorisation sub-resource,    where the selected authentication method needs to be uploaded.    This link is contained under exactly the same conditions as the data element &#39;scaMethods&#39;. - &#39;authoriseTransaction&#39;:   The link to the authorisation or cancellation authorisation sub-resource,    where the authorisation data has to be uploaded, e.g. the TOP received by SMS.  - &#39;scaStatus&#39;:    The link to retrieve the scaStatus of the corresponding authorisation sub-resource.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the  response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):  - 'scaRedirect':    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to    redirect the PSU browser. - 'scaOAuth':    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the configuration of the Authorisation Server can be retrieved. The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. * 'confirmation':    Might be added by the ASPSP if either the \"scaRedirect\" or \"scaOAuth\" hyperlink is returned    in the same response message.    This hyperlink defines the URL to the resource which needs to be updated with      * a confirmation code as retrieved after the plain redirect authentication process with the ASPSP authentication server or     * an access token as retrieved by submitting an authorization code after the integrated OAuth based authentication process with the ASPSP authentication server. - 'updatePsuIdentification':    The link to the authorisation or cancellation authorisation sub-resource,    where PSU identification data needs to be uploaded. - 'startAuthorisationWithPsuAuthentication':   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication data needs to be uploaded. - 'startAuthorisationWithEncryptedPsuAuthentication':     Same as startAuthorisactionWithPsuAuthentication where the authentication data need to be encrypted on      application layer in uploading. - 'selectAuthenticationMethod':   The link to the authorisation or cancellation authorisation sub-resource,    where the selected authentication method needs to be uploaded.    This link is contained under exactly the same conditions as the data element 'scaMethods'. - 'authoriseTransaction':   The link to the authorisation or cancellation authorisation sub-resource,    where the authorisation data has to be uploaded, e.g. the TOP received by SMS.  - 'scaStatus':    The link to retrieve the scaStatus of the corresponding authorisation sub-resource.  ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class LinksStartScaProcess extends HashMap<String, HrefType>  {
  @JsonProperty("scaRedirect")
  private HrefType scaRedirect = null;

  @JsonProperty("scaOAuth")
  private HrefType scaOAuth = null;

  @JsonProperty("confirmation")
  private HrefType confirmation = null;

  @JsonProperty("updatePsuIdentification")
  private HrefType updatePsuIdentification = null;

  @JsonProperty("startAuthorisationWithPsuAuthentication")
  private HrefType startAuthorisationWithPsuAuthentication = null;

  @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
  private HrefType startAuthorisationWithEncryptedPsuAuthentication = null;

  @JsonProperty("selectAuthenticationMethod")
  private HrefType selectAuthenticationMethod = null;

  @JsonProperty("authoriseTransaction")
  private HrefType authoriseTransaction = null;

  @JsonProperty("scaStatus")
  private HrefType scaStatus = null;

  public LinksStartScaProcess scaRedirect(HrefType scaRedirect) {
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

  public LinksStartScaProcess scaOAuth(HrefType scaOAuth) {
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

  public LinksStartScaProcess confirmation(HrefType confirmation) {
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

  public LinksStartScaProcess updatePsuIdentification(HrefType updatePsuIdentification) {
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

  public LinksStartScaProcess startAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuAuthentication
   * @return startAuthorisationWithPsuAuthentication
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("startAuthorisationWithPsuAuthentication")
  public HrefType getStartAuthorisationWithPsuAuthentication() {
    return startAuthorisationWithPsuAuthentication;
  }

  public void setStartAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
  }

  public LinksStartScaProcess startAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithEncryptedPsuAuthentication
   * @return startAuthorisationWithEncryptedPsuAuthentication
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
  public HrefType getStartAuthorisationWithEncryptedPsuAuthentication() {
    return startAuthorisationWithEncryptedPsuAuthentication;
  }

  public void setStartAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
  }

  public LinksStartScaProcess selectAuthenticationMethod(HrefType selectAuthenticationMethod) {
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

  public LinksStartScaProcess authoriseTransaction(HrefType authoriseTransaction) {
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

  public LinksStartScaProcess scaStatus(HrefType scaStatus) {
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
    LinksStartScaProcess _linksStartScaProcess = (LinksStartScaProcess) o;
    return Objects.equals(this.scaRedirect, _linksStartScaProcess.scaRedirect) &&
    Objects.equals(this.scaOAuth, _linksStartScaProcess.scaOAuth) &&
    Objects.equals(this.confirmation, _linksStartScaProcess.confirmation) &&
    Objects.equals(this.updatePsuIdentification, _linksStartScaProcess.updatePsuIdentification) &&
    Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksStartScaProcess.startAuthorisationWithPsuAuthentication) &&
    Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, _linksStartScaProcess.startAuthorisationWithEncryptedPsuAuthentication) &&
    Objects.equals(this.selectAuthenticationMethod, _linksStartScaProcess.selectAuthenticationMethod) &&
    Objects.equals(this.authoriseTransaction, _linksStartScaProcess.authoriseTransaction) &&
    Objects.equals(this.scaStatus, _linksStartScaProcess.scaStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaRedirect, scaOAuth, confirmation, updatePsuIdentification, startAuthorisationWithPsuAuthentication, startAuthorisationWithEncryptedPsuAuthentication, selectAuthenticationMethod, authoriseTransaction, scaStatus, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksStartScaProcess {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
    sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
    sb.append("    confirmation: ").append(toIndentedString(confirmation)).append("\n");
    sb.append("    updatePsuIdentification: ").append(toIndentedString(updatePsuIdentification)).append("\n");
    sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithEncryptedPsuAuthentication: ").append(toIndentedString(startAuthorisationWithEncryptedPsuAuthentication)).append("\n");
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

