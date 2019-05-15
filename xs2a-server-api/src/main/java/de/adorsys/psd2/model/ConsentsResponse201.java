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
 * Body of the JSON response for a successful conset request.
 */
@ApiModel(description = "Body of the JSON response for a successful conset request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-05-13T12:27:24.493+03:00[Europe/Kiev]")

public class ConsentsResponse201 {
    @JsonProperty("consentStatus")
    private ConsentStatus consentStatus = null;

    @JsonProperty("consentId")
    private String consentId = null;

    @JsonProperty("scaMethods")
    private ScaMethods scaMethods = null;

    @JsonProperty("chosenScaMethod")
    private ChosenScaMethod chosenScaMethod = null;

    @JsonProperty("challengeData")
    private ChallengeData challengeData = null;

    @JsonProperty("_links")
    private Map _links = null;

    @JsonProperty("psuMessage")
    private String psuMessage = null;

    public ConsentsResponse201 consentStatus(ConsentStatus consentStatus) {
        this.consentStatus = consentStatus;
        return this;
    }

    /**
     * Get consentStatus
     *
     * @return consentStatus
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull

    @Valid


    @JsonProperty("consentStatus")
    public ConsentStatus getConsentStatus() {
        return consentStatus;
    }

    public void setConsentStatus(ConsentStatus consentStatus) {
        this.consentStatus = consentStatus;
    }

    public ConsentsResponse201 consentId(String consentId) {
        this.consentId = consentId;
        return this;
    }

    /**
     * Get consentId
     *
     * @return consentId
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull


    @JsonProperty("consentId")
    public String getConsentId() {
        return consentId;
    }

    public void setConsentId(String consentId) {
        this.consentId = consentId;
    }

    public ConsentsResponse201 scaMethods(ScaMethods scaMethods) {
        this.scaMethods = scaMethods;
        return this;
    }

    /**
     * Get scaMethods
     *
     * @return scaMethods
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("scaMethods")
    public ScaMethods getScaMethods() {
        return scaMethods;
    }

    public void setScaMethods(ScaMethods scaMethods) {
        this.scaMethods = scaMethods;
    }

    public ConsentsResponse201 chosenScaMethod(ChosenScaMethod chosenScaMethod) {
        this.chosenScaMethod = chosenScaMethod;
        return this;
    }

    /**
     * Get chosenScaMethod
     *
     * @return chosenScaMethod
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("chosenScaMethod")
    public ChosenScaMethod getChosenScaMethod() {
        return chosenScaMethod;
    }

    public void setChosenScaMethod(ChosenScaMethod chosenScaMethod) {
        this.chosenScaMethod = chosenScaMethod;
    }

    public ConsentsResponse201 challengeData(ChallengeData challengeData) {
        this.challengeData = challengeData;
        return this;
    }

    /**
     * Get challengeData
     *
     * @return challengeData
     **/
    @ApiModelProperty(value = "")

    @Valid


    @JsonProperty("challengeData")
    public ChallengeData getChallengeData() {
        return challengeData;
    }

    public void setChallengeData(ChallengeData challengeData) {
        this.challengeData = challengeData;
    }

    public ConsentsResponse201 _links(Map _links) {
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

    public ConsentsResponse201 psuMessage(String psuMessage) {
        this.psuMessage = psuMessage;
        return this;
    }

    /**
     * Text to be displayed to the PSU, e.g. in a Decoupled SCA Approach.
     *
     * @return psuMessage
     **/
    @ApiModelProperty(value = "Text to be displayed to the PSU, e.g. in a Decoupled SCA Approach.")

    @Size(max = 512)

    @JsonProperty("psuMessage")
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
        ConsentsResponse201 consentsResponse201 = (ConsentsResponse201) o;
        return Objects.equals(this.consentStatus, consentsResponse201.consentStatus) &&
                   Objects.equals(this.consentId, consentsResponse201.consentId) &&
                   Objects.equals(this.scaMethods, consentsResponse201.scaMethods) &&
                   Objects.equals(this.chosenScaMethod, consentsResponse201.chosenScaMethod) &&
                   Objects.equals(this.challengeData, consentsResponse201.challengeData) &&
                   Objects.equals(this._links, consentsResponse201._links) &&
                   Objects.equals(this.psuMessage, consentsResponse201.psuMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(consentStatus, consentId, scaMethods, chosenScaMethod, challengeData, _links, psuMessage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ConsentsResponse201 {\n");

        sb.append("    consentStatus: ").append(toIndentedString(consentStatus)).append("\n");
        sb.append("    consentId: ").append(toIndentedString(consentId)).append("\n");
        sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
        sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
        sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
        sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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

