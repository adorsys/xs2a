package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Objects;

/**
 * A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response depend on the
 * dynamical decisions of the ASPSP when processing the request.  Remark: All links can be relative or full links, to
 * be decided by the ASPSP.  Type of links admitted in this response, (further links might be added for ASPSP defined
 * extensions):    * &#39;startAuthorisation&#39;:     In case, where just the authorisation process of the
 * cancellation needs to be started,     but no additional data needs to be updated for time being (no authentication
 * method to be selected,     no PSU identification nor PSU authentication data to be uploaded).   * &#39;
 * startAuthorisationWithPsuIdentification&#39;:     In case where a PSU Identification needs to be updated when
 * starting the cancellation authorisation:     The link to the cancellation-authorisations end-point, where the
 * cancellation sub-resource has to be     generated while uploading the PSU identification data.   * &#39;
 * startAuthorisationWithPsuAuthentication&#39;:     In case of a yet to be created authorisation sub-resource: The
 * link to the authorisation end-point,     where the authorisation sub-resource has to be generated while uploading
 * the PSU authentication data.
 */
@ApiModel(description = "A list of hyperlinks to be recognised by the TPP. The actual hyperlinks used in the response" +
    " depend on the dynamical decisions of the ASPSP when processing the request.  Remark: All links can be relative " +
    "or full links, to be decided by the ASPSP.  Type of links admitted in this response, (further links might be " +
    "added for ASPSP defined extensions):    * 'startAuthorisation':     In case, where just the authorisation " +
    "process of the cancellation needs to be started,     but no additional data needs to be updated for time being " +
    "(no authentication method to be selected,     no PSU identification nor PSU authentication data to be uploaded)." +
    "   * 'startAuthorisationWithPsuIdentification':     In case where a PSU Identification needs to be updated when " +
    "starting the cancellation authorisation:     The link to the cancellation-authorisations end-point, where the " +
    "cancellation sub-resource has to be     generated while uploading the PSU identification data.   * " +
    "'startAuthorisationWithPsuAuthentication':     In case of a yet to be created authorisation sub-resource: The " +
    "link to the authorisation end-point,     where the authorisation sub-resource has to be generated while " +
    "uploading the PSU authentication data. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class LinksPaymentInitiationCancel extends HashMap<String, String> {
    @JsonProperty("startAuthorisation")
    private String startAuthorisation = null;
    @JsonProperty("startAuthorisationWithPsuIdentification")
    private String startAuthorisationWithPsuIdentification = null;
    @JsonProperty("startAuthorisationWithPsuAuthentication")
    private String startAuthorisationWithPsuAuthentication = null;

    public LinksPaymentInitiationCancel startAuthorisation(String startAuthorisation) {
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

    public LinksPaymentInitiationCancel startAuthorisationWithPsuIdentification(String startAuthorisationWithPsuIdentification) {
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

    public LinksPaymentInitiationCancel startAuthorisationWithPsuAuthentication(String startAuthorisationWithPsuAuthentication) {
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

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LinksPaymentInitiationCancel _linksPaymentInitiationCancel = (LinksPaymentInitiationCancel) o;
        return Objects.equals(this.startAuthorisation, _linksPaymentInitiationCancel.startAuthorisation) && Objects.equals(this.startAuthorisationWithPsuIdentification, _linksPaymentInitiationCancel.startAuthorisationWithPsuIdentification) && Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksPaymentInitiationCancel.startAuthorisationWithPsuAuthentication) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startAuthorisation, startAuthorisationWithPsuIdentification,
            startAuthorisationWithPsuAuthentication, super.hashCode());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LinksPaymentInitiationCancel {\n");
        sb.append("    ").append(toIndentedString(super.toString())).append("\n");
        sb.append("    startAuthorisation: ").append(toIndentedString(startAuthorisation)).append("\n");
        sb.append("    startAuthorisationWithPsuIdentification: ").append(toIndentedString(startAuthorisationWithPsuIdentification)).append("\n");
        sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
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

