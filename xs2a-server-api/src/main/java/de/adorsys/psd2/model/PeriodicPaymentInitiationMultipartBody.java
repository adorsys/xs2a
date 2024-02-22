/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import java.util.Objects;

/**
 * The multipart message definition for the initiation of a periodic payment initiation  where the information of the payment is contained in a pain.001 message (Part 1) and  the additional informations related to the periodic payment is an additional JSON message (Part 2).
 */
@Schema(description = "The multipart message definition for the initiation of a periodic payment initiation  where the information of the payment is contained in a pain.001 message (Part 1) and  the additional informations related to the periodic payment is an additional JSON message (Part 2). ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


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
    @Schema(description = "")


    @JsonProperty("xmlSct")
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
    @Schema(description = "")
    @JsonProperty("jsonStandingorderType")

    @Valid
    public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson getJsonStandingorderType() {
        return jsonStandingorderType;
    }

    public void setJsonStandingorderType(PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingorderType) {
        this.jsonStandingorderType = jsonStandingorderType;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PeriodicPaymentInitiationMultipartBody periodicPaymentInitiationMultipartBody = (PeriodicPaymentInitiationMultipartBody) o;
        return Objects.equals(this.xmlSct, periodicPaymentInitiationMultipartBody.xmlSct) &&
                   Objects.equals(this.jsonStandingorderType, periodicPaymentInitiationMultipartBody.jsonStandingorderType);
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
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}
