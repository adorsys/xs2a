package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the  response depend on the dynamical decisions of the ASPSP when processing the request.  Remark: All links can be relative or full links, to be decided by the ASPSP. Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):    * &#x27;scaRedirect&#x27;:      In case of an SCA Redirect Approach, the ASPSP is transmitting the link to      which to redirect the PSU browser.   * &#x27;scaOAuth&#x27;:      In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the configuration of      the Authorisation Server can be retrieved. The configuration follows the      OAuth 2.0 Authorisation Server Metadata specification.   * &#x27;startAuthorisation&#x27;:      In case, where an explicit start of the transaction authorisation is needed,      but no more data needs to be updated (no authentication method to be selected,      no PSU identification nor PSU authentication data to be uploaded).   * &#x27;startAuthorisationWithPsuIdentification&#x27;:      The link to the authorisation end-point, where the authorisation sub-resource      has to be generated while uploading the PSU identification data.   * &#x27;startAuthorisationWithPsuAuthentication&#x27;:     The link to the authorisation end-point, where the authorisation sub-resource      has to be generated while uploading the PSU authentication data.   * &#x27;startAuthorisationWithAuthenticationMethodSelection&#x27;:     The link to the authorisation end-point, where the authorisation sub-resource      has to be generated while selecting the authentication method.      This link is contained under exactly the same conditions as the data element &#x27;scaMethods&#x27;    * &#x27;startAuthorisationWithTransactionAuthorisation&#x27;:     The link to the authorisation end-point, where the authorisation sub-resource      has to be generated while authorising the transaction e.g. by uploading an      OTP received by SMS.   * &#x27;self&#x27;:      The link to the payment initiation resource created by this request.      This link can be used to retrieve the resource data.    * &#x27;status&#x27;:      The link to retrieve the transaction status of the payment initiation.   * &#x27;scaStatus&#x27;:      The link to retrieve the scaStatus of the corresponding authorisation sub-resource.      This link is only contained, if an authorisation sub-resource has been already created.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the  response depend on the dynamical decisions of the ASPSP when processing the request.  Remark: All links can be relative or full links, to be decided by the ASPSP. Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):    * 'scaRedirect':      In case of an SCA Redirect Approach, the ASPSP is transmitting the link to      which to redirect the PSU browser.   * 'scaOAuth':      In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the configuration of      the Authorisation Server can be retrieved. The configuration follows the      OAuth 2.0 Authorisation Server Metadata specification.   * 'startAuthorisation':      In case, where an explicit start of the transaction authorisation is needed,      but no more data needs to be updated (no authentication method to be selected,      no PSU identification nor PSU authentication data to be uploaded).   * 'startAuthorisationWithPsuIdentification':      The link to the authorisation end-point, where the authorisation sub-resource      has to be generated while uploading the PSU identification data.   * 'startAuthorisationWithPsuAuthentication':     The link to the authorisation end-point, where the authorisation sub-resource      has to be generated while uploading the PSU authentication data.   * 'startAuthorisationWithAuthenticationMethodSelection':     The link to the authorisation end-point, where the authorisation sub-resource      has to be generated while selecting the authentication method.      This link is contained under exactly the same conditions as the data element 'scaMethods'    * 'startAuthorisationWithTransactionAuthorisation':     The link to the authorisation end-point, where the authorisation sub-resource      has to be generated while authorising the transaction e.g. by uploading an      OTP received by SMS.   * 'self':      The link to the payment initiation resource created by this request.      This link can be used to retrieve the resource data.    * 'status':      The link to retrieve the transaction status of the payment initiation.   * 'scaStatus':      The link to retrieve the scaStatus of the corresponding authorisation sub-resource.      This link is only contained, if an authorisation sub-resource has been already created. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class LinksSigningBasket {

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

    public LinksSigningBasket scaRedirect(String scaRedirect) {
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

    public LinksSigningBasket scaOAuth(String scaOAuth) {
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

    public LinksSigningBasket startAuthorisation(String startAuthorisation) {
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

    public LinksSigningBasket startAuthorisationWithPsuIdentification(String startAuthorisationWithPsuIdentification) {
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

    public LinksSigningBasket startAuthorisationWithPsuAuthentication(String startAuthorisationWithPsuAuthentication) {
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

    public LinksSigningBasket startAuthorisationWithAuthenticationMethodSelection(String startAuthorisationWithAuthenticationMethodSelection) {
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

    public LinksSigningBasket startAuthorisationWithTransactionAuthorisation(String startAuthorisationWithTransactionAuthorisation) {
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

    public LinksSigningBasket self(String self) {
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

    public LinksSigningBasket status(String status) {
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

    public LinksSigningBasket scaStatus(String scaStatus) {
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
        LinksSigningBasket _linksSigningBasket = (LinksSigningBasket) o;
        return Objects.equals(this.scaRedirect, _linksSigningBasket.scaRedirect) &&
            Objects.equals(this.scaOAuth, _linksSigningBasket.scaOAuth) &&
            Objects.equals(this.startAuthorisation, _linksSigningBasket.startAuthorisation) &&
            Objects.equals(this.startAuthorisationWithPsuIdentification, _linksSigningBasket.startAuthorisationWithPsuIdentification) &&
            Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksSigningBasket.startAuthorisationWithPsuAuthentication) &&
            Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, _linksSigningBasket.startAuthorisationWithAuthenticationMethodSelection) &&
            Objects.equals(this.startAuthorisationWithTransactionAuthorisation, _linksSigningBasket.startAuthorisationWithTransactionAuthorisation) &&
            Objects.equals(this.self, _linksSigningBasket.self) &&
            Objects.equals(this.status, _linksSigningBasket.status) &&
            Objects.equals(this.scaStatus, _linksSigningBasket.scaStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaRedirect, scaOAuth, startAuthorisation, startAuthorisationWithPsuIdentification, startAuthorisationWithPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection, startAuthorisationWithTransactionAuthorisation, self, status, scaStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinksSigningBasket {\n");

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
