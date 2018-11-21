package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Body for a SCT INST payment initation.
 */
@ApiModel(description = "Body for a SCT INST payment initation. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class PaymentInitiationSctInstBulkElementJson {
    @JsonProperty("endToEndIdentification")
    private String endToEndIdentification = null;
    @JsonProperty("instructedAmount")
    private Amount instructedAmount = null;
    @JsonProperty("creditorAccount")
    private Object creditorAccount = null;
    @JsonProperty("creditorAgent")
    private String creditorAgent = null;
    @JsonProperty("creditorName")
    private String creditorName = null;
    @JsonProperty("creditorAddress")
    private Address creditorAddress = null;
    @JsonProperty("remittanceInformationUnstructured")
    private String remittanceInformationUnstructured = null;

    public PaymentInitiationSctInstBulkElementJson endToEndIdentification(String endToEndIdentification) {
        this.endToEndIdentification = endToEndIdentification;
        return this;
    }

    /**
     * Get endToEndIdentification
     *
     * @return endToEndIdentification
     **/
    @ApiModelProperty(value = "")
    @Size(max = 35)
    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

    public void setEndToEndIdentification(String endToEndIdentification) {
        this.endToEndIdentification = endToEndIdentification;
    }

    public PaymentInitiationSctInstBulkElementJson instructedAmount(Amount instructedAmount) {
        this.instructedAmount = instructedAmount;
        return this;
    }

    /**
     * Get instructedAmount
     *
     * @return instructedAmount
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public Amount getInstructedAmount() {
        return instructedAmount;
    }

    public void setInstructedAmount(Amount instructedAmount) {
        this.instructedAmount = instructedAmount;
    }

    public PaymentInitiationSctInstBulkElementJson creditorAccount(Object creditorAccount) {
        this.creditorAccount = creditorAccount;
        return this;
    }

    /**
     * Get creditorAccount
     *
     * @return creditorAccount
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public Object getCreditorAccount() {
        return creditorAccount;
    }

    public void setCreditorAccount(Object creditorAccount) {
        this.creditorAccount = creditorAccount;
    }

    public PaymentInitiationSctInstBulkElementJson creditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
        return this;
    }

    /**
     * Get creditorAgent
     *
     * @return creditorAgent
     **/
    @ApiModelProperty(value = "")
    public String getCreditorAgent() {
        return creditorAgent;
    }

    public void setCreditorAgent(String creditorAgent) {
        this.creditorAgent = creditorAgent;
    }

    public PaymentInitiationSctInstBulkElementJson creditorName(String creditorName) {
        this.creditorName = creditorName;
        return this;
    }

    /**
     * Get creditorName
     *
     * @return creditorName
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public String getCreditorName() {
        return creditorName;
    }

    public void setCreditorName(String creditorName) {
        this.creditorName = creditorName;
    }

    public PaymentInitiationSctInstBulkElementJson creditorAddress(Address creditorAddress) {
        this.creditorAddress = creditorAddress;
        return this;
    }

    /**
     * Get creditorAddress
     *
     * @return creditorAddress
     **/
    @ApiModelProperty(value = "")
    @Valid
    public Address getCreditorAddress() {
        return creditorAddress;
    }

    public void setCreditorAddress(Address creditorAddress) {
        this.creditorAddress = creditorAddress;
    }

    public PaymentInitiationSctInstBulkElementJson remittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        return this;
    }

    /**
     * Get remittanceInformationUnstructured
     *
     * @return remittanceInformationUnstructured
     **/
    @ApiModelProperty(value = "")
    @Size(max = 140)
    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PaymentInitiationSctInstBulkElementJson paymentInitiationSctInstBulkElementJson =
            (PaymentInitiationSctInstBulkElementJson) o;
        return Objects.equals(this.endToEndIdentification,
            paymentInitiationSctInstBulkElementJson.endToEndIdentification) && Objects.equals(this.instructedAmount,
            paymentInitiationSctInstBulkElementJson.instructedAmount) && Objects.equals(this.creditorAccount,
            paymentInitiationSctInstBulkElementJson.creditorAccount) && Objects.equals(this.creditorAgent,
            paymentInitiationSctInstBulkElementJson.creditorAgent) && Objects.equals(this.creditorName,
            paymentInitiationSctInstBulkElementJson.creditorName) && Objects.equals(this.creditorAddress,
            paymentInitiationSctInstBulkElementJson.creditorAddress) && Objects.equals(this.remittanceInformationUnstructured, paymentInitiationSctInstBulkElementJson.remittanceInformationUnstructured);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endToEndIdentification, instructedAmount, creditorAccount, creditorAgent, creditorName,
            creditorAddress, remittanceInformationUnstructured);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PaymentInitiationSctInstBulkElementJson {\n");
        sb.append("    endToEndIdentification: ").append(toIndentedString(endToEndIdentification)).append("\n");
        sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
        sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
        sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
        sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
        sb.append("    creditorAddress: ").append(toIndentedString(creditorAddress)).append("\n");
        sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
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

