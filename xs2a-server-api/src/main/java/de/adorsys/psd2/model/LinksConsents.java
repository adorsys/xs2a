package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP.  Type of links admitted in this response (which might be extended by single ASPSPs as indicated in its XS2A  documentation):   - &#x27;scaRedirect&#x27;:      In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to redirect the      PSU browser.   - &#x27;scaOAuth&#x27;:      In case of an OAuth2 based Redirect Approach, the ASPSP is transmitting the link where the configuration      of the OAuth2 Server is defined.      The configuration follows the OAuth 2.0 Authorisation Server Metadata specification.    - &#x27;startAuthorisation&#x27;:      In case, where an explicit start of the transaction authorisation is needed,      but no more data needs to be updated (no authentication method to be selected,      no PSU identification nor PSU authentication data to be uploaded).   - &#x27;startAuthorisationWithPsuIdentification&#x27;:      The link to the authorisation end-point, where the authorisation sub-resource has to be generated      while uploading the PSU identification data.   - &#x27;startAuthorisationWithPsuAuthentication&#x27;:     The link to the authorisation end-point, where the authorisation sub-resource has to be generated      while uploading the PSU authentication data.   - &#x27;startAuthorisationWithAuthenticationMethodSelection&#x27;:     The link to the authorisation end-point, where the authorisation sub-resource has to be generated      while selecting the authentication method. This link is contained under exactly the same conditions      as the data element &#x27;scaMethods&#x27;    - &#x27;startAuthorisationWithTransactionAuthorisation&#x27;:     The link to the authorisation end-point, where the authorisation sub-resource has to be generated      while authorising the transaction e.g. by uploading an OTP received by SMS.   - &#x27;self&#x27;:      The link to the Establish Account Information Consent resource created by this request.      This link can be used to retrieve the resource data.    - &#x27;status&#x27;:      The link to retrieve the status of the account information consent.   - &#x27;scaStatus&#x27;: The link to retrieve the scaStatus of the corresponding authorisation sub-resource.      This link is only contained, if an authorisation sub-resource has been already created.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP.  Type of links admitted in this response (which might be extended by single ASPSPs as indicated in its XS2A  documentation):   - 'scaRedirect':      In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to redirect the      PSU browser.   - 'scaOAuth':      In case of an OAuth2 based Redirect Approach, the ASPSP is transmitting the link where the configuration      of the OAuth2 Server is defined.      The configuration follows the OAuth 2.0 Authorisation Server Metadata specification.    - 'startAuthorisation':      In case, where an explicit start of the transaction authorisation is needed,      but no more data needs to be updated (no authentication method to be selected,      no PSU identification nor PSU authentication data to be uploaded).   - 'startAuthorisationWithPsuIdentification':      The link to the authorisation end-point, where the authorisation sub-resource has to be generated      while uploading the PSU identification data.   - 'startAuthorisationWithPsuAuthentication':     The link to the authorisation end-point, where the authorisation sub-resource has to be generated      while uploading the PSU authentication data.   - 'startAuthorisationWithAuthenticationMethodSelection':     The link to the authorisation end-point, where the authorisation sub-resource has to be generated      while selecting the authentication method. This link is contained under exactly the same conditions      as the data element 'scaMethods'    - 'startAuthorisationWithTransactionAuthorisation':     The link to the authorisation end-point, where the authorisation sub-resource has to be generated      while authorising the transaction e.g. by uploading an OTP received by SMS.   - 'self':      The link to the Establish Account Information Consent resource created by this request.      This link can be used to retrieve the resource data.    - 'status':      The link to retrieve the status of the account information consent.   - 'scaStatus': The link to retrieve the scaStatus of the corresponding authorisation sub-resource.      This link is only contained, if an authorisation sub-resource has been already created. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class LinksConsents extends HashMap<String, String> {

    @JsonProperty("scaRedirect")
    private String scaRedirect = null;

    @JsonProperty("scaOAuth")
    private String scaOAuth = null;

    @JsonProperty("startAuthorisation")
    private String startAuthorisation = null;

    @JsonProperty("startAuthorisationWithPsuIdentification")
    private String startAuthorisationWithPsuIdentification = null;

    @JsonProperty("startAuthorisationWithPsuAuthentication")
    private String startAuthorisationWithPsuAuthentication = null;

    @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
    private String startAuthorisationWithAuthenticationMethodSelection = null;

    @JsonProperty("startAuthorisationWithTransactionAuthorisation")
    private String startAuthorisationWithTransactionAuthorisation = null;

    @JsonProperty("self")
    private String self = null;

    @JsonProperty("status")
    private String status = null;

    @JsonProperty("scaStatus")
    private String scaStatus = null;

    public LinksConsents scaRedirect(String scaRedirect) {
        this.scaRedirect = scaRedirect;
        return this;
    }

    /**
     * Get scaRedirect
     *
     * @return scaRedirect
     **/
    @ApiModelProperty
    public String getScaRedirect() {
        return scaRedirect;
    }

    public void setScaRedirect(String scaRedirect) {
        this.scaRedirect = scaRedirect;
    }

    public LinksConsents scaOAuth(String scaOAuth) {
        this.scaOAuth = scaOAuth;
        return this;
    }

    /**
     * Get scaOAuth
     *
     * @return scaOAuth
     **/
    @ApiModelProperty
    public String getScaOAuth() {
        return scaOAuth;
    }

    public void setScaOAuth(String scaOAuth) {
        this.scaOAuth = scaOAuth;
    }

    public LinksConsents startAuthorisation(String startAuthorisation) {
        this.startAuthorisation = startAuthorisation;
        return this;
    }

    /**
     * Get startAuthorisation
     *
     * @return startAuthorisation
     **/
    @ApiModelProperty
    public String getStartAuthorisation() {
        return startAuthorisation;
    }

    public void setStartAuthorisation(String startAuthorisation) {
        this.startAuthorisation = startAuthorisation;
    }

    public LinksConsents startAuthorisationWithPsuIdentification(String startAuthorisationWithPsuIdentification) {
        this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
        return this;
    }

    /**
     * Get startAuthorisationWithPsuIdentification
     *
     * @return startAuthorisationWithPsuIdentification
     **/
    @ApiModelProperty
    public String getStartAuthorisationWithPsuIdentification() {
        return startAuthorisationWithPsuIdentification;
    }

    public void setStartAuthorisationWithPsuIdentification(String startAuthorisationWithPsuIdentification) {
        this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    }

    public LinksConsents startAuthorisationWithPsuAuthentication(String startAuthorisationWithPsuAuthentication) {
        this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
        return this;
    }

    /**
     * Get startAuthorisationWithPsuAuthentication
     *
     * @return startAuthorisationWithPsuAuthentication
     **/
    @ApiModelProperty
    public String getStartAuthorisationWithPsuAuthentication() {
        return startAuthorisationWithPsuAuthentication;
    }

    public void setStartAuthorisationWithPsuAuthentication(String startAuthorisationWithPsuAuthentication) {
        this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    }

    public LinksConsents startAuthorisationWithAuthenticationMethodSelection(String startAuthorisationWithAuthenticationMethodSelection) {
        this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
        return this;
    }

    /**
     * Get startAuthorisationWithAuthenticationMethodSelection
     *
     * @return startAuthorisationWithAuthenticationMethodSelection
     **/
    @ApiModelProperty
    public String getStartAuthorisationWithAuthenticationMethodSelection() {
        return startAuthorisationWithAuthenticationMethodSelection;
    }

    public void setStartAuthorisationWithAuthenticationMethodSelection(String startAuthorisationWithAuthenticationMethodSelection) {
        this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
    }

    public LinksConsents startAuthorisationWithTransactionAuthorisation(String startAuthorisationWithTransactionAuthorisation) {
        this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
        return this;
    }

    /**
     * Get startAuthorisationWithTransactionAuthorisation
     *
     * @return startAuthorisationWithTransactionAuthorisation
     **/
    @ApiModelProperty
    public String getStartAuthorisationWithTransactionAuthorisation() {
        return startAuthorisationWithTransactionAuthorisation;
    }

    public void setStartAuthorisationWithTransactionAuthorisation(String startAuthorisationWithTransactionAuthorisation) {
        this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
    }

    public LinksConsents self(String self) {
        this.self = self;
        return this;
    }

    /**
     * Get self
     *
     * @return self
     **/
    @ApiModelProperty
    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public LinksConsents status(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @ApiModelProperty
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LinksConsents scaStatus(String scaStatus) {
        this.scaStatus = scaStatus;
        return this;
    }

    /**
     * Get scaStatus
     *
     * @return scaStatus
     **/
    @ApiModelProperty
    public String getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(String scaStatus) {
        this.scaStatus = scaStatus;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinksConsents _linksConsents = (LinksConsents) o;
        return Objects.equals(this.scaRedirect, _linksConsents.scaRedirect) &&
            Objects.equals(this.scaOAuth, _linksConsents.scaOAuth) &&
            Objects.equals(this.startAuthorisation, _linksConsents.startAuthorisation) &&
            Objects.equals(this.startAuthorisationWithPsuIdentification, _linksConsents.startAuthorisationWithPsuIdentification) &&
            Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksConsents.startAuthorisationWithPsuAuthentication) &&
            Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, _linksConsents.startAuthorisationWithAuthenticationMethodSelection) &&
            Objects.equals(this.startAuthorisationWithTransactionAuthorisation, _linksConsents.startAuthorisationWithTransactionAuthorisation) &&
            Objects.equals(this.self, _linksConsents.self) &&
            Objects.equals(this.status, _linksConsents.status) &&
            Objects.equals(this.scaStatus, _linksConsents.scaStatus) &&
            super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaRedirect, scaOAuth, startAuthorisation, startAuthorisationWithPsuIdentification, startAuthorisationWithPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection, startAuthorisationWithTransactionAuthorisation, self, status, scaStatus, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinksConsents {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
        sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
        sb.append("    startAuthorisation: ").append(toIndentedString(startAuthorisation)).append("\n");
        sb.append("    startAuthorisationWithPsuIdentification: ").append(toIndentedString(startAuthorisationWithPsuIdentification)).append("\n");
        sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
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
    private String toIndentedString(java.lang.Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
