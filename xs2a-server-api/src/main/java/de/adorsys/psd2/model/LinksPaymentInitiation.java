package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP defined extensions):  * &#39;scaRedirect&#39;:    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to redirect the PSU browser. * &#39;scaOAuth&#39;:    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the configuration of the Authorisation   Server can be retrieved. The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. * &#39;confirmation&#39;:    Might be added by the ASPSP if either the \&quot;scaRedirect\&quot; or \&quot;scaOAuth\&quot; hyperlink is returned    in the same response message.    This hyperlink defines the URL to the resource which needs to be updated with      * a confirmation code as retrieved after the plain redirect authentication process with the ASPSP authentication server or     * an access token as retrieved by submitting an authorization code after the integrated OAuth based authentication process with the ASPSP authentication server.  * &#39;startAuthorisation&#39;:    In case, where an explicit start of the transaction authorisation is needed, but no more data needs to be updated    (no authentication method to be selected, no PSU identification nor PSU authentication data to be uploaded). * &#39;startAuthorisationWithPsuIdentification&#39;:    The link to the authorisation end-point, where the authorisation sub-resource has to be generated while    uploading the PSU identification data. * &#39;startAuthorisationWithPsuAuthentication&#39;:    The link to the authorisation end-point, where the authorisation sub-resource has to be generated while    uploading the PSU authentication data.   * &#39;startAuthorisationWithEncryptedPsuAuthentication&#39;:     Same as startAuthorisactionWithPsuAuthentication where the authentication data need to be encrypted on      application layer in uploading. * &#39;startAuthorisationWithAuthenticationMethodSelection&#39;:    The link to the authorisation end-point, where the authorisation sub-resource has to be generated while    selecting the authentication method.    This link is contained under exactly the same conditions as the data element \&quot;scaMethods\&quot; * &#39;startAuthorisationWithTransactionAuthorisation&#39;:   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while    authorising the transaction e.g. by uploading an OTP received by SMS. * &#39;self&#39;:    The link to the payment initiation resource created by this request.   This link can be used to retrieve the resource data. * &#39;status&#39;:    The link to retrieve the transaction status of the payment initiation. * &#39;scaStatus&#39;:    The link to retrieve the scaStatus of the corresponding authorisation sub-resource.    This link is only contained, if an authorisation sub-resource has been already created.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP defined extensions):  * 'scaRedirect':    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to redirect the PSU browser. * 'scaOAuth':    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the configuration of the Authorisation   Server can be retrieved. The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. * 'confirmation':    Might be added by the ASPSP if either the \"scaRedirect\" or \"scaOAuth\" hyperlink is returned    in the same response message.    This hyperlink defines the URL to the resource which needs to be updated with      * a confirmation code as retrieved after the plain redirect authentication process with the ASPSP authentication server or     * an access token as retrieved by submitting an authorization code after the integrated OAuth based authentication process with the ASPSP authentication server.  * 'startAuthorisation':    In case, where an explicit start of the transaction authorisation is needed, but no more data needs to be updated    (no authentication method to be selected, no PSU identification nor PSU authentication data to be uploaded). * 'startAuthorisationWithPsuIdentification':    The link to the authorisation end-point, where the authorisation sub-resource has to be generated while    uploading the PSU identification data. * 'startAuthorisationWithPsuAuthentication':    The link to the authorisation end-point, where the authorisation sub-resource has to be generated while    uploading the PSU authentication data.   * 'startAuthorisationWithEncryptedPsuAuthentication':     Same as startAuthorisactionWithPsuAuthentication where the authentication data need to be encrypted on      application layer in uploading. * 'startAuthorisationWithAuthenticationMethodSelection':    The link to the authorisation end-point, where the authorisation sub-resource has to be generated while    selecting the authentication method.    This link is contained under exactly the same conditions as the data element \"scaMethods\" * 'startAuthorisationWithTransactionAuthorisation':   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while    authorising the transaction e.g. by uploading an OTP received by SMS. * 'self':    The link to the payment initiation resource created by this request.   This link can be used to retrieve the resource data. * 'status':    The link to retrieve the transaction status of the payment initiation. * 'scaStatus':    The link to retrieve the scaStatus of the corresponding authorisation sub-resource.    This link is only contained, if an authorisation sub-resource has been already created. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class LinksPaymentInitiation extends HashMap<String, HrefType>  {
  @JsonProperty("scaRedirect")
  private HrefType scaRedirect = null;

  @JsonProperty("scaOAuth")
  private HrefType scaOAuth = null;

  @JsonProperty("confirmation")
  private HrefType confirmation = null;

  @JsonProperty("startAuthorisation")
  private HrefType startAuthorisation = null;

  @JsonProperty("startAuthorisationWithPsuIdentification")
  private HrefType startAuthorisationWithPsuIdentification = null;

  @JsonProperty("startAuthorisationWithPsuAuthentication")
  private HrefType startAuthorisationWithPsuAuthentication = null;

  @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
  private HrefType startAuthorisationWithEncryptedPsuAuthentication = null;

  @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
  private HrefType startAuthorisationWithAuthenticationMethodSelection = null;

  @JsonProperty("startAuthorisationWithTransactionAuthorisation")
  private HrefType startAuthorisationWithTransactionAuthorisation = null;

  @JsonProperty("self")
  private HrefType self = null;

  @JsonProperty("status")
  private HrefType status = null;

  @JsonProperty("scaStatus")
  private HrefType scaStatus = null;

  public LinksPaymentInitiation scaRedirect(HrefType scaRedirect) {
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

  public LinksPaymentInitiation scaOAuth(HrefType scaOAuth) {
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

  public LinksPaymentInitiation confirmation(HrefType confirmation) {
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

  public LinksPaymentInitiation startAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisation
   * @return startAuthorisation
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("startAuthorisation")
  public HrefType getStartAuthorisation() {
    return startAuthorisation;
  }

  public void setStartAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
  }

  public LinksPaymentInitiation startAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuIdentification
   * @return startAuthorisationWithPsuIdentification
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("startAuthorisationWithPsuIdentification")
  public HrefType getStartAuthorisationWithPsuIdentification() {
    return startAuthorisationWithPsuIdentification;
  }

  public void setStartAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
  }

  public LinksPaymentInitiation startAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
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

  public LinksPaymentInitiation startAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
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

  public LinksPaymentInitiation startAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
    return this;
  }

  /**
   * Get startAuthorisationWithAuthenticationMethodSelection
   * @return startAuthorisationWithAuthenticationMethodSelection
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
  public HrefType getStartAuthorisationWithAuthenticationMethodSelection() {
    return startAuthorisationWithAuthenticationMethodSelection;
  }

  public void setStartAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
  }

  public LinksPaymentInitiation startAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisationWithTransactionAuthorisation
   * @return startAuthorisationWithTransactionAuthorisation
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("startAuthorisationWithTransactionAuthorisation")
  public HrefType getStartAuthorisationWithTransactionAuthorisation() {
    return startAuthorisationWithTransactionAuthorisation;
  }

  public void setStartAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
  }

  public LinksPaymentInitiation self(HrefType self) {
    this.self = self;
    return this;
  }

  /**
   * Get self
   * @return self
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("self")
  public HrefType getSelf() {
    return self;
  }

  public void setSelf(HrefType self) {
    this.self = self;
  }

  public LinksPaymentInitiation status(HrefType status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("status")
  public HrefType getStatus() {
    return status;
  }

  public void setStatus(HrefType status) {
    this.status = status;
  }

  public LinksPaymentInitiation scaStatus(HrefType scaStatus) {
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
    LinksPaymentInitiation _linksPaymentInitiation = (LinksPaymentInitiation) o;
    return Objects.equals(this.scaRedirect, _linksPaymentInitiation.scaRedirect) &&
    Objects.equals(this.scaOAuth, _linksPaymentInitiation.scaOAuth) &&
    Objects.equals(this.confirmation, _linksPaymentInitiation.confirmation) &&
    Objects.equals(this.startAuthorisation, _linksPaymentInitiation.startAuthorisation) &&
    Objects.equals(this.startAuthorisationWithPsuIdentification, _linksPaymentInitiation.startAuthorisationWithPsuIdentification) &&
    Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksPaymentInitiation.startAuthorisationWithPsuAuthentication) &&
    Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, _linksPaymentInitiation.startAuthorisationWithEncryptedPsuAuthentication) &&
    Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, _linksPaymentInitiation.startAuthorisationWithAuthenticationMethodSelection) &&
    Objects.equals(this.startAuthorisationWithTransactionAuthorisation, _linksPaymentInitiation.startAuthorisationWithTransactionAuthorisation) &&
    Objects.equals(this.self, _linksPaymentInitiation.self) &&
    Objects.equals(this.status, _linksPaymentInitiation.status) &&
    Objects.equals(this.scaStatus, _linksPaymentInitiation.scaStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaRedirect, scaOAuth, confirmation, startAuthorisation, startAuthorisationWithPsuIdentification, startAuthorisationWithPsuAuthentication, startAuthorisationWithEncryptedPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection, startAuthorisationWithTransactionAuthorisation, self, status, scaStatus, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksPaymentInitiation {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
    sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
    sb.append("    confirmation: ").append(toIndentedString(confirmation)).append("\n");
    sb.append("    startAuthorisation: ").append(toIndentedString(startAuthorisation)).append("\n");
    sb.append("    startAuthorisationWithPsuIdentification: ").append(toIndentedString(startAuthorisationWithPsuIdentification)).append("\n");
    sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithEncryptedPsuAuthentication: ").append(toIndentedString(startAuthorisationWithEncryptedPsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithAuthenticationMethodSelection: ").append(toIndentedString(startAuthorisationWithAuthenticationMethodSelection)).append("\n");
    sb.append("    startAuthorisationWithTransactionAuthorisation: ").append(toIndentedString(startAuthorisationWithTransactionAuthorisation)).append("\n");
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

