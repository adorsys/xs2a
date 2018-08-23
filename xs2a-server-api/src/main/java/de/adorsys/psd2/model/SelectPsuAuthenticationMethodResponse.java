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
 * Body of the JSON response for a successful select PSU Authentication Method request.
 */
@ApiModel(description = "Body of the JSON response for a successful select PSU Authentication Method request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class SelectPsuAuthenticationMethodResponse {

    @JsonProperty("chosenScaMethod")
    private ChosenScaMethod chosenScaMethod = null;

    @JsonProperty("challengeData")
    private ChallengeData challengeData = null;

    @JsonProperty("_links")
    private Map _links = null;

    @JsonProperty("scaStatus")
    private ScaStatus scaStatus = null;

    @JsonProperty("psuMessage")
    private String psuMessage = null;

    public SelectPsuAuthenticationMethodResponse chosenScaMethod(ChosenScaMethod chosenScaMethod) {
        this.chosenScaMethod = chosenScaMethod;
        return this;
    }

    /**
     * Get chosenScaMethod
     *
     * @return chosenScaMethod
     **/
    @ApiModelProperty
    @Valid
    public ChosenScaMethod getChosenScaMethod() {
        return chosenScaMethod;
    }

    public void setChosenScaMethod(ChosenScaMethod chosenScaMethod) {
        this.chosenScaMethod = chosenScaMethod;
    }

    public SelectPsuAuthenticationMethodResponse challengeData(ChallengeData challengeData) {
        this.challengeData = challengeData;
        return this;
    }

    /**
     * Get challengeData
     *
     * @return challengeData
     **/
    @ApiModelProperty
    @Valid
    public ChallengeData getChallengeData() {
        return challengeData;
    }

    public void setChallengeData(ChallengeData challengeData) {
        this.challengeData = challengeData;
    }

    public SelectPsuAuthenticationMethodResponse _links(Map _links) {
        this._links = _links;
        return this;
    }

    /**
     * Get _links
     *
     * @return _links
     **/
    @ApiModelProperty
    @Valid
    public Map getLinks() {
        return _links;
    }

    public void setLinks(Map _links) {
        this._links = _links;
    }

    public SelectPsuAuthenticationMethodResponse scaStatus(ScaStatus scaStatus) {
        this.scaStatus = scaStatus;
        return this;
    }

    /**
     * Get scaStatus
     *
     * @return scaStatus
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public ScaStatus getScaStatus() {
        return scaStatus;
    }

    public void setScaStatus(ScaStatus scaStatus) {
        this.scaStatus = scaStatus;
    }

    public SelectPsuAuthenticationMethodResponse psuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
        return this;
    }

    /**
     * Get psuMessage
     *
     * @return psuMessage
     **/
    @ApiModelProperty
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
        SelectPsuAuthenticationMethodResponse selectPsuAuthenticationMethodResponse = (SelectPsuAuthenticationMethodResponse) o;
        return Objects.equals(this.chosenScaMethod, selectPsuAuthenticationMethodResponse.chosenScaMethod) &&
            Objects.equals(this.challengeData, selectPsuAuthenticationMethodResponse.challengeData) &&
            Objects.equals(this._links, selectPsuAuthenticationMethodResponse._links) &&
            Objects.equals(this.scaStatus, selectPsuAuthenticationMethodResponse.scaStatus) &&
            Objects.equals(this.psuMessage, selectPsuAuthenticationMethodResponse.psuMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(chosenScaMethod, challengeData, _links, scaStatus, psuMessage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SelectPsuAuthenticationMethodResponse {\n");

        sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
        sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
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
