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
 * Body of the response for a successful payment initiation request.
 */
@ApiModel(description = "Body of the response for a successful payment initiation request.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-08-09T18:41:17.591+02:00[Europe/Berlin]")
public class PaymentInitationRequestResponse201 {

    @JsonProperty("transactionStatus")
    private TransactionStatus transactionStatus = null;

    @JsonProperty("paymentId")
    private String paymentId = null;

    @JsonProperty("transactionFees")
    private Amount transactionFees = null;

    @JsonProperty("transactionFeeIndicator")
    private Boolean transactionFeeIndicator = null;

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

    @JsonProperty("tppMessages")
    private TppMessages tppMessages = null;

    public PaymentInitationRequestResponse201 transactionStatus(TransactionStatus transactionStatus) {
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

    public PaymentInitationRequestResponse201 paymentId(String paymentId) {
        this.paymentId = paymentId;
        return this;
    }

    /**
     * Get paymentId
     *
     * @return paymentId
     **/
    @ApiModelProperty(required = true)
    @NotNull

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public PaymentInitationRequestResponse201 transactionFees(Amount transactionFees) {
        this.transactionFees = transactionFees;
        return this;
    }

    /**
     * Get transactionFees
     *
     * @return transactionFees
     **/
    @ApiModelProperty
    @Valid
    public Amount getTransactionFees() {
        return transactionFees;
    }

    public void setTransactionFees(Amount transactionFees) {
        this.transactionFees = transactionFees;
    }

    public PaymentInitationRequestResponse201 transactionFeeIndicator(Boolean transactionFeeIndicator) {
        this.transactionFeeIndicator = transactionFeeIndicator;
        return this;
    }

    /**
     * Get transactionFeeIndicator
     *
     * @return transactionFeeIndicator
     **/
    @ApiModelProperty
    public Boolean getTransactionFeeIndicator() {
        return transactionFeeIndicator;
    }

    public void setTransactionFeeIndicator(Boolean transactionFeeIndicator) {
        this.transactionFeeIndicator = transactionFeeIndicator;
    }

    public PaymentInitationRequestResponse201 scaMethods(ScaMethods scaMethods) {
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

    public PaymentInitationRequestResponse201 chosenScaMethod(ChosenScaMethod chosenScaMethod) {
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

    public PaymentInitationRequestResponse201 challengeData(ChallengeData challengeData) {
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

    public PaymentInitationRequestResponse201 _links(Map _links) {
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
    public Map getLinks() {
        return _links;
    }

    public void setLinks(Map _links) {
        this._links = _links;
    }

    public PaymentInitationRequestResponse201 psuMessage(String psuMessage) {
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

    public PaymentInitationRequestResponse201 tppMessages(TppMessages tppMessages) {
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
        PaymentInitationRequestResponse201 paymentInitationRequestResponse201 = (PaymentInitationRequestResponse201) o;
        return Objects.equals(this.transactionStatus, paymentInitationRequestResponse201.transactionStatus) &&
            Objects.equals(this.paymentId, paymentInitationRequestResponse201.paymentId) &&
            Objects.equals(this.transactionFees, paymentInitationRequestResponse201.transactionFees) &&
            Objects.equals(this.transactionFeeIndicator, paymentInitationRequestResponse201.transactionFeeIndicator) &&
            Objects.equals(this.scaMethods, paymentInitationRequestResponse201.scaMethods) &&
            Objects.equals(this.chosenScaMethod, paymentInitationRequestResponse201.chosenScaMethod) &&
            Objects.equals(this.challengeData, paymentInitationRequestResponse201.challengeData) &&
            Objects.equals(this._links, paymentInitationRequestResponse201._links) &&
            Objects.equals(this.psuMessage, paymentInitationRequestResponse201.psuMessage) &&
            Objects.equals(this.tppMessages, paymentInitationRequestResponse201.tppMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionStatus, paymentId, transactionFees, transactionFeeIndicator, scaMethods, chosenScaMethod, challengeData, _links, psuMessage, tppMessages);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PaymentInitationRequestResponse201 {\n");

        sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
        sb.append("    paymentId: ").append(toIndentedString(paymentId)).append("\n");
        sb.append("    transactionFees: ").append(toIndentedString(transactionFees)).append("\n");
        sb.append("    transactionFeeIndicator: ").append(toIndentedString(transactionFeeIndicator)).append("\n");
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
