package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;
import java.util.Objects;

/**
 * Body of the JSON response for a successful update PSU Identification request.
 */
@ApiModel(description = "Body of the JSON response for a successful update PSU Identification request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class UpdatePsuIdenticationResponse {
    @JsonProperty("_links")
    private Map _links = null;
    @JsonProperty("scaStatus")
    private ScaStatus scaStatus = null;
    @JsonProperty("psuMessage")
    private String psuMessage = null;

    public UpdatePsuIdenticationResponse _links(Map _links) {
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
    public Map getLinks() {
        return _links;
    }

    public void setLinks(Map _links) {
        this._links = _links;
    }

    public UpdatePsuIdenticationResponse scaStatus(ScaStatus scaStatus) {
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
    public ScaStatus getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(ScaStatus scaStatus) {
        this.scaStatus = scaStatus;
    }

    public UpdatePsuIdenticationResponse psuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
        return this;
    }

    /**
     * Get psuMessage
     *
     * @return psuMessage
     **/
    @ApiModelProperty(value = "")
    public String getPsuMessage() {
        return psuMessage;
    }

    public void setPsuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdatePsuIdenticationResponse updatePsuIdenticationResponse = (UpdatePsuIdenticationResponse) o;
        return Objects.equals(this._links, updatePsuIdenticationResponse._links) && Objects.equals(this.scaStatus,
            updatePsuIdenticationResponse.scaStatus) && Objects.equals(this.psuMessage,
            updatePsuIdenticationResponse.psuMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(_links, scaStatus, psuMessage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdatePsuIdenticationResponse {\n");
        sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
        sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
        sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
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

