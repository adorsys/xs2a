package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the
 * dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full
 * links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for
 * ASPSP defined extensions):  * &#39;scaRedirect&#39;:   In case of an SCA Redirect Approach, the ASPSP is
 * transmitting the link to which to redirect the PSU browser. * &#39;scaOAuth&#39;:   In case of a SCA OAuth2
 * Approach, the ASPSP is transmitting the URI where the configuration of the Authorisation   Server can be retrieved
 * . The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. * &#39;
 * startAuthorisation&#39;:   In case, where an explicit start of the transaction authorisation is needed, but no
 * more data needs to be updated   (no authentication method to be selected, no PSU identification nor PSU
 * authentication data to be uploaded). * &#39;startAuthorisationWithPsuIdentification&#39;:   The link to the
 * authorisation end-point, where the authorisation sub-resource has to be generated while   uploading the PSU
 * identification data. * &#39;startAuthorisationWithPsuAuthentication&#39;:   The link to the authorisation
 * end-point, where the authorisation sub-resource has to be generated while   uploading the PSU authentication data.
 * * &#39;startAuthorisationWithAuthenticationMethodSelection&#39;:   The link to the authorisation end-point, where
 * the authorisation sub-resource has to be generated while   selecting the authentication method.   This link is
 * contained under exactly the same conditions as the data element \&quot;scaMethods\&quot; * &#39;
 * startAuthorisationWithTransactionAuthorisation&#39;:   The link to the authorisation end-point, where the
 * authorisation sub-resource has to be generated while   authorising the transaction e.g. by uploading an OTP
 * received by SMS. * &#39;self&#39;:   The link to the payment initiation resource created by this request.   This
 * link can be used to retrieve the resource data. * &#39;status&#39;:   The link to retrieve the transaction status
 * of the payment initiation. * &#39;scaStatus&#39;:   The link to retrieve the scaStatus of the corresponding
 * authorisation sub-resource.   This link is only contained, if an authorisation sub-resource has been already created.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response" +
    " depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be " +
    "relative or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links " +
    "might be added for ASPSP defined extensions):  * 'scaRedirect':   In case of an SCA Redirect Approach, the ASPSP" +
    " is transmitting the link to which to redirect the PSU browser. * 'scaOAuth':   In case of a SCA OAuth2 " +
    "Approach, the ASPSP is transmitting the URI where the configuration of the Authorisation   Server can be " +
    "retrieved. The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. * " +
    "'startAuthorisation':   In case, where an explicit start of the transaction authorisation is needed, but no more" +
    " data needs to be updated   (no authentication method to be selected, no PSU identification nor PSU " +
    "authentication data to be uploaded). * 'startAuthorisationWithPsuIdentification':   The link to the " +
    "authorisation end-point, where the authorisation sub-resource has to be generated while   uploading the PSU " +
    "identification data. * 'startAuthorisationWithPsuAuthentication':   The link to the authorisation end-point, " +
    "where the authorisation sub-resource has to be generated while   uploading the PSU authentication data. * " +
    "'startAuthorisationWithAuthenticationMethodSelection':   The link to the authorisation end-point, where the " +
    "authorisation sub-resource has to be generated while   selecting the authentication method.   This link is " +
    "contained under exactly the same conditions as the data element \"scaMethods\" * " +
    "'startAuthorisationWithTransactionAuthorisation':   The link to the authorisation end-point, where the " +
    "authorisation sub-resource has to be generated while   authorising the transaction e.g. by uploading an OTP " +
    "received by SMS. * 'self':   The link to the payment initiation resource created by this request.   This link " +
    "can be used to retrieve the resource data. * 'status':   The link to retrieve the transaction status of the " +
    "payment initiation. * 'scaStatus':   The link to retrieve the scaStatus of the corresponding authorisation " +
    "sub-resource.   This link is only contained, if an authorisation sub-resource has been already created. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class LinksPaymentInitiation extends HashMap<String, String> {
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

    public LinksPaymentInitiation scaRedirect(String scaRedirect) {
        this.scaRedirect = scaRedirect;
        return this;
    }

    /**
     * Get scaRedirect
     *
     * @return scaRedirect
     **/
    @ApiModelProperty(value = "")
    public String getScaRedirect() {
        return scaRedirect;
    }

    public void setScaRedirect(String scaRedirect) {
        this.scaRedirect = scaRedirect;
    }

    public LinksPaymentInitiation scaOAuth(String scaOAuth) {
        this.scaOAuth = scaOAuth;
        return this;
    }

    /**
     * Get scaOAuth
     *
     * @return scaOAuth
     **/
    @ApiModelProperty(value = "")
    public String getScaOAuth() {
        return scaOAuth;
    }

    public void setScaOAuth(String scaOAuth) {
        this.scaOAuth = scaOAuth;
    }

    public LinksPaymentInitiation startAuthorisation(String startAuthorisation) {
        this.startAuthorisation = startAuthorisation;
        return this;
    }

    /**
     * Get startAuthorisation
     *
     * @return startAuthorisation
     **/
    @ApiModelProperty(value = "")
    public String getStartAuthorisation() {
        return startAuthorisation;
    }

    public void setStartAuthorisation(String startAuthorisation) {
        this.startAuthorisation = startAuthorisation;
    }

    public LinksPaymentInitiation startAuthorisationWithPsuIdentification(String startAuthorisationWithPsuIdentification) {
        this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
        return this;
    }

    /**
     * Get startAuthorisationWithPsuIdentification
     *
     * @return startAuthorisationWithPsuIdentification
     **/
    @ApiModelProperty(value = "")
    public String getStartAuthorisationWithPsuIdentification() {
        return startAuthorisationWithPsuIdentification;
    }

    public void setStartAuthorisationWithPsuIdentification(String startAuthorisationWithPsuIdentification) {
        this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    }

    public LinksPaymentInitiation startAuthorisationWithPsuAuthentication(String startAuthorisationWithPsuAuthentication) {
        this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
        return this;
    }

    /**
     * Get startAuthorisationWithPsuAuthentication
     *
     * @return startAuthorisationWithPsuAuthentication
     **/
    @ApiModelProperty(value = "")
    public String getStartAuthorisationWithPsuAuthentication() {
        return startAuthorisationWithPsuAuthentication;
    }

    public void setStartAuthorisationWithPsuAuthentication(String startAuthorisationWithPsuAuthentication) {
        this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    }

    public LinksPaymentInitiation startAuthorisationWithAuthenticationMethodSelection(String startAuthorisationWithAuthenticationMethodSelection) {
        this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
        return this;
    }

    /**
     * Get startAuthorisationWithAuthenticationMethodSelection
     *
     * @return startAuthorisationWithAuthenticationMethodSelection
     **/
    @ApiModelProperty(value = "")
    public String getStartAuthorisationWithAuthenticationMethodSelection() {
        return startAuthorisationWithAuthenticationMethodSelection;
    }

    public void setStartAuthorisationWithAuthenticationMethodSelection(String startAuthorisationWithAuthenticationMethodSelection) {
        this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
    }

    public LinksPaymentInitiation startAuthorisationWithTransactionAuthorisation(String startAuthorisationWithTransactionAuthorisation) {
        this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
        return this;
    }

    /**
     * Get startAuthorisationWithTransactionAuthorisation
     *
     * @return startAuthorisationWithTransactionAuthorisation
     **/
    @ApiModelProperty(value = "")
    public String getStartAuthorisationWithTransactionAuthorisation() {
        return startAuthorisationWithTransactionAuthorisation;
    }

    public void setStartAuthorisationWithTransactionAuthorisation(String startAuthorisationWithTransactionAuthorisation) {
        this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
    }

    public LinksPaymentInitiation self(String self) {
        this.self = self;
        return this;
    }

    /**
     * Get self
     *
     * @return self
     **/
    @ApiModelProperty(value = "")
    public String getSelf() {
        return self;
    }

    public void setSelf(String self) {
        this.self = self;
    }

    public LinksPaymentInitiation status(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get status
     *
     * @return status
     **/
    @ApiModelProperty(value = "")
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LinksPaymentInitiation scaStatus(String scaStatus) {
        this.scaStatus = scaStatus;
        return this;
    }

    /**
     * Get scaStatus
     *
     * @return scaStatus
     **/
    @ApiModelProperty(value = "")
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
        LinksPaymentInitiation _linksPaymentInitiation = (LinksPaymentInitiation) o;
        return Objects.equals(this.scaRedirect, _linksPaymentInitiation.scaRedirect) && Objects.equals(this.scaOAuth,
            _linksPaymentInitiation.scaOAuth) && Objects.equals(this.startAuthorisation,
            _linksPaymentInitiation.startAuthorisation) && Objects.equals(this.startAuthorisationWithPsuIdentification, _linksPaymentInitiation.startAuthorisationWithPsuIdentification) && Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksPaymentInitiation.startAuthorisationWithPsuAuthentication) && Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, _linksPaymentInitiation.startAuthorisationWithAuthenticationMethodSelection) && Objects.equals(this.startAuthorisationWithTransactionAuthorisation, _linksPaymentInitiation.startAuthorisationWithTransactionAuthorisation) && Objects.equals(this.self, _linksPaymentInitiation.self) && Objects.equals(this.status, _linksPaymentInitiation.status) && Objects.equals(this.scaStatus, _linksPaymentInitiation.scaStatus) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaRedirect, scaOAuth, startAuthorisation, startAuthorisationWithPsuIdentification,
            startAuthorisationWithPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection,
            startAuthorisationWithTransactionAuthorisation, self, status, scaStatus, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinksPaymentInitiation {\n");
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

