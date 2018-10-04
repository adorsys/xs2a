/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body of the JSON response for a successful get status request for a consent.
 */
@ApiModel(description = "Body of the JSON response for a successful get status request for a consent.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class ConsentStatusResponse200 {
    @JsonProperty("consentStatus")
    private ConsentStatus consentStatus = null;

    public ConsentStatusResponse200 consentStatus(ConsentStatus consentStatus) {
        this.consentStatus = consentStatus;
        return this;
    }

    /**
     * Get consentStatus
     *
     * @return consentStatus
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public ConsentStatus getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(ConsentStatus consentStatus) {
        this.consentStatus = consentStatus;
    }


    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ConsentStatusResponse200 consentStatusResponse200 = (ConsentStatusResponse200) o;
        return Objects.equals(this.consentStatus, consentStatusResponse200.consentStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consentStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentStatusResponse200 {\n");

        sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
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

