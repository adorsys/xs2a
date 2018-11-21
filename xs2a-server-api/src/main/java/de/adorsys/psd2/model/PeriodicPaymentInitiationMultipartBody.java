package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Objects;

/**
 * The multipart message definition for the initiation of a periodic payment initiation where the information of the
 * payment is contained in an pain.001 message (Part 1) and the additional informations related to the periodic
 * payment is an additional JSON message (Part 2).
 */
@ApiModel(description = "The multipart message definition for the initiation of a periodic payment initiation where " +
    "the information of the payment is contained in an pain.001 message (Part 1) and the additional informations " +
    "related to the periodic payment is an additional JSON message (Part 2). ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class PeriodicPaymentInitiationMultipartBody {
    @JsonProperty("xml_sct")
    private Object xmlSct = null;
    @JsonProperty("json_standingorderType")
    private PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingorderType = null;

    public PeriodicPaymentInitiationMultipartBody xmlSct(Object xmlSct) {
        this.xmlSct = xmlSct;
        return this;
    }

    /**
     * Get xmlSct
     *
     * @return xmlSct
     **/
    @ApiModelProperty(value = "")
    public Object getXmlSct() {
        return xmlSct;
    }

    public void setXmlSct(Object xmlSct) {
        this.xmlSct = xmlSct;
    }

    public PeriodicPaymentInitiationMultipartBody jsonStandingorderType(PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingorderType) {
        this.jsonStandingorderType = jsonStandingorderType;
        return this;
    }

    /**
     * Get jsonStandingorderType
     *
     * @return jsonStandingorderType
     **/
    @ApiModelProperty(value = "")
    @Valid
    public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson getJsonStandingorderType() {
        return jsonStandingorderType;
    }

    public void setJsonStandingorderType(PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingorderType) {
        this.jsonStandingorderType = jsonStandingorderType;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PeriodicPaymentInitiationMultipartBody periodicPaymentInitiationMultipartBody =
            (PeriodicPaymentInitiationMultipartBody) o;
        return Objects.equals(this.xmlSct, periodicPaymentInitiationMultipartBody.xmlSct) && Objects.equals(this.jsonStandingorderType, periodicPaymentInitiationMultipartBody.jsonStandingorderType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(xmlSct, jsonStandingorderType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PeriodicPaymentInitiationMultipartBody {\n");
        sb.append("    xmlSct: ").append(toIndentedString(xmlSct)).append("\n");
        sb.append("    jsonStandingorderType: ").append(toIndentedString(jsonStandingorderType)).append("\n");
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

