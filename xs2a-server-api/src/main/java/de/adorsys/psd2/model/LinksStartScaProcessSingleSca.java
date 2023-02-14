/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. Type of links admitted in this response (which might be extended by single ASPSPs as indicated in its XS2A documentation):  - &#x27;scaRedirect&#x27;:   In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to   redirect the PSU browser. - &#x27;scaOAuth&#x27;:   In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the configuration of the Authorisation Server can be retrieved. The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. - &#x27;startAuthorisation&#x27;:   In case, where an explicit start of the transaction authorisation is needed, but no more data needs to be updated (no authentication method to be selected, no PSU identification nor PSU authentication data to be uploaded). - &#x27;startAuthorisationWithPsuIdentification&#x27;:   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while uploading the PSU identification data. - &#x27;startAuthorisationWithPsuAuthentication&#x27;:   The link to the authorisation or cancellation authorisation sub-resource,   where PSU authentication data needs to be uploaded. - &#x27;startAuthorisationWithEncryptedPsuAuthentication&#x27;:   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while uploading the encrypted PSU authentication data. - &#x27;startAuthorisationWithAuthentication MethodSelection&#x27;:   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while selecting the authentication method.   This link is contained under exactly the same conditions as the data element \&quot;scaMethods\&quot;. - &#x27;startAuthorisationWithTransactionAuthorisation&#x27;:   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while authorising the transaction e.g. by uploading an OTP received by SMS. - &#x27;self&#x27;:   The link to the Establish Account Information Consent resource created by this request.   This link can be used to retrieve the resource data. - &#x27;status&#x27;:   The link to retrieve the transaction status of the payment initiation. - &#x27;scaStatus&#x27;:   The link to retrieve the scaStatus of the corresponding authorisation sub-resource.
 */
@Schema(description = "A list of hyperlinks to be recognised by the TPP. Type of links admitted in this response (which might be extended by single ASPSPs as indicated in its XS2A documentation):  - 'scaRedirect':   In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to   redirect the PSU browser. - 'scaOAuth':   In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the configuration of the Authorisation Server can be retrieved. The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. - 'startAuthorisation':   In case, where an explicit start of the transaction authorisation is needed, but no more data needs to be updated (no authentication method to be selected, no PSU identification nor PSU authentication data to be uploaded). - 'startAuthorisationWithPsuIdentification':   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while uploading the PSU identification data. - 'startAuthorisationWithPsuAuthentication':   The link to the authorisation or cancellation authorisation sub-resource,   where PSU authentication data needs to be uploaded. - 'startAuthorisationWithEncryptedPsuAuthentication':   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while uploading the encrypted PSU authentication data. - 'startAuthorisationWithAuthentication MethodSelection':   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while selecting the authentication method.   This link is contained under exactly the same conditions as the data element \"scaMethods\". - 'startAuthorisationWithTransactionAuthorisation':   The link to the authorisation end-point, where the authorisation sub-resource has to be generated while authorising the transaction e.g. by uploading an OTP received by SMS. - 'self':   The link to the Establish Account Information Consent resource created by this request.   This link can be used to retrieve the resource data. - 'status':   The link to retrieve the transaction status of the payment initiation. - 'scaStatus':   The link to retrieve the scaStatus of the corresponding authorisation sub-resource. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:06.283258+03:00[Europe/Kiev]")


public class LinksStartScaProcessSingleSca extends HashMap<String, HrefType>  {
  @JsonProperty("scaRedirect")
  private HrefType scaRedirect = null;

  @JsonProperty("scaOAuth")
  private HrefType scaOAuth = null;

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

  public LinksStartScaProcessSingleSca scaRedirect(HrefType scaRedirect) {
    this.scaRedirect = scaRedirect;
    return this;
  }

  /**
   * Get scaRedirect
   * @return scaRedirect
   **/
  @Schema(description = "")

    @Valid
    public HrefType getScaRedirect() {
    return scaRedirect;
  }

  public void setScaRedirect(HrefType scaRedirect) {
    this.scaRedirect = scaRedirect;
  }

  public LinksStartScaProcessSingleSca scaOAuth(HrefType scaOAuth) {
    this.scaOAuth = scaOAuth;
    return this;
  }

  /**
   * Get scaOAuth
   * @return scaOAuth
   **/
  @Schema(description = "")

    @Valid
    public HrefType getScaOAuth() {
    return scaOAuth;
  }

  public void setScaOAuth(HrefType scaOAuth) {
    this.scaOAuth = scaOAuth;
  }

  public LinksStartScaProcessSingleSca startAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisation
   * @return startAuthorisation
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisation() {
    return startAuthorisation;
  }

  public void setStartAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
  }

  public LinksStartScaProcessSingleSca startAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuIdentification
   * @return startAuthorisationWithPsuIdentification
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithPsuIdentification() {
    return startAuthorisationWithPsuIdentification;
  }

  public void setStartAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
  }

  public LinksStartScaProcessSingleSca startAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuAuthentication
   * @return startAuthorisationWithPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithPsuAuthentication() {
    return startAuthorisationWithPsuAuthentication;
  }

  public void setStartAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
  }

  public LinksStartScaProcessSingleSca startAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithEncryptedPsuAuthentication
   * @return startAuthorisationWithEncryptedPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithEncryptedPsuAuthentication() {
    return startAuthorisationWithEncryptedPsuAuthentication;
  }

  public void setStartAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
  }

  public LinksStartScaProcessSingleSca startAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
    return this;
  }

  /**
   * Get startAuthorisationWithAuthenticationMethodSelection
   * @return startAuthorisationWithAuthenticationMethodSelection
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithAuthenticationMethodSelection() {
    return startAuthorisationWithAuthenticationMethodSelection;
  }

  public void setStartAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
  }

  public LinksStartScaProcessSingleSca startAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisationWithTransactionAuthorisation
   * @return startAuthorisationWithTransactionAuthorisation
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithTransactionAuthorisation() {
    return startAuthorisationWithTransactionAuthorisation;
  }

  public void setStartAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
  }

  public LinksStartScaProcessSingleSca self(HrefType self) {
    this.self = self;
    return this;
  }

  /**
   * Get self
   * @return self
   **/
  @Schema(description = "")

    @Valid
    public HrefType getSelf() {
    return self;
  }

  public void setSelf(HrefType self) {
    this.self = self;
  }

  public LinksStartScaProcessSingleSca status(HrefType status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStatus() {
    return status;
  }

  public void setStatus(HrefType status) {
    this.status = status;
  }

  public LinksStartScaProcessSingleSca scaStatus(HrefType scaStatus) {
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksStartScaProcessSingleSca _linksStartScaProcessSingleSca = (LinksStartScaProcessSingleSca) o;
    return Objects.equals(this.scaRedirect, _linksStartScaProcessSingleSca.scaRedirect) &&
        Objects.equals(this.scaOAuth, _linksStartScaProcessSingleSca.scaOAuth) &&
        Objects.equals(this.startAuthorisation, _linksStartScaProcessSingleSca.startAuthorisation) &&
        Objects.equals(this.startAuthorisationWithPsuIdentification, _linksStartScaProcessSingleSca.startAuthorisationWithPsuIdentification) &&
        Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksStartScaProcessSingleSca.startAuthorisationWithPsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, _linksStartScaProcessSingleSca.startAuthorisationWithEncryptedPsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, _linksStartScaProcessSingleSca.startAuthorisationWithAuthenticationMethodSelection) &&
        Objects.equals(this.startAuthorisationWithTransactionAuthorisation, _linksStartScaProcessSingleSca.startAuthorisationWithTransactionAuthorisation) &&
        Objects.equals(this.self, _linksStartScaProcessSingleSca.self) &&
        Objects.equals(this.status, _linksStartScaProcessSingleSca.status) &&
        Objects.equals(this.scaStatus, _linksStartScaProcessSingleSca.scaStatus) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaRedirect, scaOAuth, startAuthorisation, startAuthorisationWithPsuIdentification, startAuthorisationWithPsuAuthentication, startAuthorisationWithEncryptedPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection, startAuthorisationWithTransactionAuthorisation, self, status, scaStatus, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksStartScaProcessSingleSca {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
    sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
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
