package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Body of the JSON response for a successful create signing basket request.
 */
@ApiModel(description = "Body of the JSON response for a successful create signing basket request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class SigningBasketResponse201 {

    @JsonProperty("transactionStatus")
    private TransactionStatus transactionStatus = null;

    @JsonProperty("basketId")
    private String basketId = null;

    @JsonProperty("scaMethods")
    private ScaMethods scaMethods = null;

    @JsonProperty("chosenScaMethod")
    private ChosenScaMethod chosenScaMethod = null;

    @JsonProperty("challengeData")
    private ChallengeData challengeData = null;

    @JsonProperty("_links")
    private LinksSigningBasket _links = null;

    @JsonProperty("psuMessage")
    private String psuMessage = null;

    @JsonProperty("tppMessages")
    private TppMessages tppMessages = null;

    public SigningBasketResponse201 transactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
        return this;
    }

    /**
     * Get transactionStatus
     *
     * @return transactionStatus
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public SigningBasketResponse201 basketId(String basketId) {
        this.basketId = basketId;
        return this;
    }

    /**
     * Get basketId
     *
     * @return basketId
     **/
    @ApiModelProperty(required = true)
    @NotNull

    public String getBasketId() {
        return basketId;
    }

    public void setBasketId(String basketId) {
        this.basketId = basketId;
    }

    public SigningBasketResponse201 scaMethods(ScaMethods scaMethods) {
        this.scaMethods = scaMethods;
        return this;
    }

    /**
     * Get scaMethods
     *
     * @return scaMethods
     **/
    @ApiModelProperty
    @Valid
    public ScaMethods getScaMethods() {
        return scaMethods;
    }

    public void setScaMethods(ScaMethods scaMethods) {
        this.scaMethods = scaMethods;
    }

    public SigningBasketResponse201 chosenScaMethod(ChosenScaMethod chosenScaMethod) {
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

    public SigningBasketResponse201 challengeData(ChallengeData challengeData) {
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

    public SigningBasketResponse201 _links(LinksSigningBasket _links) {
        this._links = _links;
        return this;
    }

    /**
     * Get _links
     *
     * @return _links
     **/
    @ApiModelProperty(required = true)
    @NotNull
    @Valid
    public LinksSigningBasket getLinks() {
        return _links;
    }

    public void setLinks(LinksSigningBasket _links) {
        this._links = _links;
    }

    public SigningBasketResponse201 psuMessage(String psuMessage) {
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

    public SigningBasketResponse201 tppMessages(TppMessages tppMessages) {
        this.tppMessages = tppMessages;
        return this;
    }

    /**
     * Get tppMessages
     *
     * @return tppMessages
     **/
    @ApiModelProperty
    @Valid
    public TppMessages getTppMessages() {
        return tppMessages;
    }

    public void setTppMessages(TppMessages tppMessages) {
        this.tppMessages = tppMessages;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SigningBasketResponse201 signingBasketResponse201 = (SigningBasketResponse201) o;
        return Objects.equals(this.transactionStatus, signingBasketResponse201.transactionStatus) &&
            Objects.equals(this.basketId, signingBasketResponse201.basketId) &&
            Objects.equals(this.scaMethods, signingBasketResponse201.scaMethods) &&
            Objects.equals(this.chosenScaMethod, signingBasketResponse201.chosenScaMethod) &&
            Objects.equals(this.challengeData, signingBasketResponse201.challengeData) &&
            Objects.equals(this._links, signingBasketResponse201._links) &&
            Objects.equals(this.psuMessage, signingBasketResponse201.psuMessage) &&
            Objects.equals(this.tppMessages, signingBasketResponse201.tppMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionStatus, basketId, scaMethods, chosenScaMethod, challengeData, _links, psuMessage, tppMessages);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SigningBasketResponse201 {\n");

        sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
        sb.append("    basketId: ").append(toIndentedString(basketId)).append("\n");
        sb.append("    scaMethods: ").append(toIndentedString(scaMethods)).append("\n");
        sb.append("    chosenScaMethod: ").append(toIndentedString(chosenScaMethod)).append("\n");
        sb.append("    challengeData: ").append(toIndentedString(challengeData)).append("\n");
        sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
        sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
        sb.append("    tppMessages: ").append(toIndentedString(tppMessages)).append("\n");
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
