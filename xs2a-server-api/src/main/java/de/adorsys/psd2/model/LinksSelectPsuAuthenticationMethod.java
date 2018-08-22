package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in  the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.   **Remark:** This method can be applied before or after PSU identification.  This leads to many possible hyperlink responses. Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):  - \&quot;scaRedirect\&quot;:    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to    redirect the PSU browser. - \&quot;scaOAuth\&quot;:    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the    configuration of the Authorisation Server can be retrieved.    The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. - \&quot;updatePsuIdentification\&quot;:    The link to the authorisation or cancellation authorisation sub-resource,    where PSU identification data needs to be uploaded. - \&quot;updatePsuAuthentication\&quot;:   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication data needs to be uploaded. \&quot;authoriseTransaction\&quot;:   The link to the authorisation or cancellation authorisation sub-resource,    where the authorisation data has to be uploaded, e.g. the TOP received by SMS.  \&quot;scaStatus\&quot;:    The link to retrieve the scaStatus of the corresponding authorisation sub-resource.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in  the response depend on the dynamical decisions of the ASPSP when processing the request.  **Remark:** All links can be relative or full links, to be decided by the ASPSP.   **Remark:** This method can be applied before or after PSU identification.  This leads to many possible hyperlink responses. Type of links admitted in this response, (further links might be added for ASPSP defined  extensions):  - \"scaRedirect\":    In case of an SCA Redirect Approach, the ASPSP is transmitting the link to which to    redirect the PSU browser. - \"scaOAuth\":    In case of a SCA OAuth2 Approach, the ASPSP is transmitting the URI where the    configuration of the Authorisation Server can be retrieved.    The configuration follows the OAuth 2.0 Authorisation Server Metadata specification. - \"updatePsuIdentification\":    The link to the authorisation or cancellation authorisation sub-resource,    where PSU identification data needs to be uploaded. - \"updatePsuAuthentication\":   The link to the authorisation or cancellation authorisation sub-resource,    where PSU authentication data needs to be uploaded. \"authoriseTransaction\":   The link to the authorisation or cancellation authorisation sub-resource,    where the authorisation data has to be uploaded, e.g. the TOP received by SMS.  \"scaStatus\":    The link to retrieve the scaStatus of the corresponding authorisation sub-resource. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class LinksSelectPsuAuthenticationMethod extends HashMap<String, String> {

    @JsonProperty("scaRedirect")
    private String scaRedirect = null;

    @JsonProperty("scaOAuth")
    private String scaOAuth = null;

    @JsonProperty("updatePsuIdentification")
    private String updatePsuIdentification = null;

    @JsonProperty("updatePsuAuthentication")
    private String updatePsuAuthentication = null;

    @JsonProperty("authoriseTransaction")
    private String authoriseTransaction = null;

    @JsonProperty("scaStatus")
    private String scaStatus = null;

    public LinksSelectPsuAuthenticationMethod scaRedirect(String scaRedirect) {
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

    public LinksSelectPsuAuthenticationMethod scaOAuth(String scaOAuth) {
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

    public LinksSelectPsuAuthenticationMethod updatePsuIdentification(String updatePsuIdentification) {
        this.updatePsuIdentification = updatePsuIdentification;
        return this;
    }

    /**
     * Get updatePsuIdentification
     *
     * @return updatePsuIdentification
     **/
    @ApiModelProperty
    public String getUpdatePsuIdentification() {
        return updatePsuIdentification;
    }

    public void setUpdatePsuIdentification(String updatePsuIdentification) {
        this.updatePsuIdentification = updatePsuIdentification;
    }

    public LinksSelectPsuAuthenticationMethod updatePsuAuthentication(String updatePsuAuthentication) {
        this.updatePsuAuthentication = updatePsuAuthentication;
        return this;
    }

    /**
     * Get updatePsuAuthentication
     *
     * @return updatePsuAuthentication
     **/
    @ApiModelProperty
    public String getUpdatePsuAuthentication() {
        return updatePsuAuthentication;
    }

    public void setUpdatePsuAuthentication(String updatePsuAuthentication) {
        this.updatePsuAuthentication = updatePsuAuthentication;
    }

    public LinksSelectPsuAuthenticationMethod authoriseTransaction(String authoriseTransaction) {
        this.authoriseTransaction = authoriseTransaction;
        return this;
    }

    /**
     * Get authoriseTransaction
     *
     * @return authoriseTransaction
     **/
    @ApiModelProperty
    public String getAuthoriseTransaction() {
        return authoriseTransaction;
    }

    public void setAuthoriseTransaction(String authoriseTransaction) {
        this.authoriseTransaction = authoriseTransaction;
    }

    public LinksSelectPsuAuthenticationMethod scaStatus(String scaStatus) {
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
        LinksSelectPsuAuthenticationMethod _linksSelectPsuAuthenticationMethod = (LinksSelectPsuAuthenticationMethod) o;
        return Objects.equals(this.scaRedirect, _linksSelectPsuAuthenticationMethod.scaRedirect) &&
            Objects.equals(this.scaOAuth, _linksSelectPsuAuthenticationMethod.scaOAuth) &&
            Objects.equals(this.updatePsuIdentification, _linksSelectPsuAuthenticationMethod.updatePsuIdentification) &&
            Objects.equals(this.updatePsuAuthentication, _linksSelectPsuAuthenticationMethod.updatePsuAuthentication) &&
            Objects.equals(this.authoriseTransaction, _linksSelectPsuAuthenticationMethod.authoriseTransaction) &&
            Objects.equals(this.scaStatus, _linksSelectPsuAuthenticationMethod.scaStatus) &&
            super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaRedirect, scaOAuth, updatePsuIdentification, updatePsuAuthentication, authoriseTransaction, scaStatus, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinksSelectPsuAuthenticationMethod {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
        sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
        sb.append("    updatePsuIdentification: ").append(toIndentedString(updatePsuIdentification)).append("\n");
        sb.append("    updatePsuAuthentication: ").append(toIndentedString(updatePsuAuthentication)).append("\n");
        sb.append("    authoriseTransaction: ").append(toIndentedString(authoriseTransaction)).append("\n");
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
