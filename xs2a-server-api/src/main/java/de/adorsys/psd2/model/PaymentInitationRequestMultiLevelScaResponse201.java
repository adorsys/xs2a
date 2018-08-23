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
public class PaymentInitationRequestMultiLevelScaResponse201 {

    @JsonProperty("transactionStatus")
    private TransactionStatus transactionStatus = null;

    @JsonProperty("paymentId")
    private String paymentId = null;

    @JsonProperty("transactionFees")
    private Amount transactionFees = null;

    @JsonProperty("transactionFeeIndicator")
    private Boolean transactionFeeIndicator = null;

    @JsonProperty("_links")
    private Map _links = null;

    @JsonProperty("psuMessage")
    private String psuMessage = null;

    @JsonProperty("tppMessages")
    private TppMessages tppMessages = null;

    public PaymentInitationRequestMultiLevelScaResponse201 transactionStatus(TransactionStatus transactionStatus) {
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

    public PaymentInitationRequestMultiLevelScaResponse201 paymentId(String paymentId) {
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

    public PaymentInitationRequestMultiLevelScaResponse201 transactionFees(Amount transactionFees) {
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

    public PaymentInitationRequestMultiLevelScaResponse201 transactionFeeIndicator(Boolean transactionFeeIndicator) {
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

    public PaymentInitationRequestMultiLevelScaResponse201 _links(Map _links) {
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

    public PaymentInitationRequestMultiLevelScaResponse201 psuMessage(String psuMessage) {
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

    public PaymentInitationRequestMultiLevelScaResponse201 tppMessages(TppMessages tppMessages) {
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
        PaymentInitationRequestMultiLevelScaResponse201 paymentInitationRequestMultiLevelScaResponse201 = (PaymentInitationRequestMultiLevelScaResponse201) o;
        return Objects.equals(this.transactionStatus, paymentInitationRequestMultiLevelScaResponse201.transactionStatus) &&
            Objects.equals(this.paymentId, paymentInitationRequestMultiLevelScaResponse201.paymentId) &&
            Objects.equals(this.transactionFees, paymentInitationRequestMultiLevelScaResponse201.transactionFees) &&
            Objects.equals(this.transactionFeeIndicator, paymentInitationRequestMultiLevelScaResponse201.transactionFeeIndicator) &&
            Objects.equals(this._links, paymentInitationRequestMultiLevelScaResponse201._links) &&
            Objects.equals(this.psuMessage, paymentInitationRequestMultiLevelScaResponse201.psuMessage) &&
            Objects.equals(this.tppMessages, paymentInitationRequestMultiLevelScaResponse201.tppMessages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(transactionStatus, paymentId, transactionFees, transactionFeeIndicator, _links, psuMessage, tppMessages);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PaymentInitationRequestMultiLevelScaResponse201 {\n");

        sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
        sb.append("    paymentId: ").append(toIndentedString(paymentId)).append("\n");
        sb.append("    transactionFees: ").append(toIndentedString(transactionFees)).append("\n");
        sb.append("    transactionFeeIndicator: ").append(toIndentedString(transactionFeeIndicator)).append("\n");
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
