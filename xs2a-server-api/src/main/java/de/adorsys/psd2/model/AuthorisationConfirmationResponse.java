/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
import javax.validation.constraints.Size;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response for an authorisation confirmation request.
 */
@ApiModel(description = "Body of the JSON response for an authorisation confirmation request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-03-16T13:49:16.891743+02:00[Europe/Kiev]")

public class AuthorisationConfirmationResponse {
    @JsonProperty("scaStatus")
    private ScaStatusAuthorisationConfirmation scaStatus = null;

    @JsonProperty("_links")
    private Map _links = null;

    @JsonProperty("psuMessage")
    private String psuMessage = null;

    public AuthorisationConfirmationResponse scaStatus(ScaStatusAuthorisationConfirmation scaStatus) {
        this.scaStatus = scaStatus;
        return this;
    }

    /**
     * Get scaStatus
     *
     * @return scaStatus
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid


    @JsonProperty("scaStatus")
    public ScaStatusAuthorisationConfirmation getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(ScaStatusAuthorisationConfirmation scaStatus) {
        this.scaStatus = scaStatus;
    }

    public AuthorisationConfirmationResponse _links(Map _links) {
        this._links = _links;
        return this;
    }

    /**
     * Get _links
     *
     * @return _links
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid


    @JsonProperty("_links")
    public Map getLinks() {
        return _links;
    }

    public void setLinks(Map _links) {
        this._links = _links;
    }

    public AuthorisationConfirmationResponse psuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
        return this;
    }

    /**
     * Get psuMessage
     *
     * @return psuMessage
     **/
    @ApiModelProperty(value = "")

    @Size(max = 500)

    @JsonProperty("psuMessage")
    public String getPsuMessage() {
        return psuMessage;
    }

    public void setPsuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthorisationConfirmationResponse authorisationConfirmationResponse = (AuthorisationConfirmationResponse) o;
        return Objects.equals(this.scaStatus, authorisationConfirmationResponse.scaStatus) &&
                   Objects.equals(this._links, authorisationConfirmationResponse._links) &&
                   Objects.equals(this.psuMessage, authorisationConfirmationResponse.psuMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(scaStatus, _links, psuMessage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthorisationConfirmationResponse {\n");

        sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
        sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
        sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
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

