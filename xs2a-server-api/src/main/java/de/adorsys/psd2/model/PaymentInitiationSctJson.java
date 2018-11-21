package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * Body for a SCT payment initation.
 */
@ApiModel(description = "Body for a SCT payment initation. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T15:31" +
    ":52.888+02:00[Europe/Berlin]")
public class PaymentInitiationSctJson {
    @JsonProperty("endToEndIdentification")
    private String endToEndIdentification = null;
    @JsonProperty("debtorAccount")
    private Object debtorAccount = null;
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
    @JsonProperty("requestedExecutionDate")
    private LocalDate requestedExecutionDate = null;
    @JsonProperty("requestedExecutionTime")
    private OffsetDateTime requestedExecutionTime = null;

    public PaymentInitiationSctJson endToEndIdentification(String endToEndIdentification) {
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

    public PaymentInitiationSctJson debtorAccount(Object debtorAccount) {
        this.debtorAccount = debtorAccount;
        return this;
    }

    /**
     * Get debtorAccount
     *
     * @return debtorAccount
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public Object getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(Object debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public PaymentInitiationSctJson instructedAmount(Amount instructedAmount) {
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

    public PaymentInitiationSctJson creditorAccount(Object creditorAccount) {
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

    public PaymentInitiationSctJson creditorAgent(String creditorAgent) {
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

    public PaymentInitiationSctJson creditorName(String creditorName) {
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

    public PaymentInitiationSctJson creditorAddress(Address creditorAddress) {
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

    public PaymentInitiationSctJson remittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
        return this;
    }

    /**
     * Get remittanceInformationUnstructured
     *
     * @return remittanceInformationUnstructured
     **/
    @ApiModelProperty(value = "")
    public String getRemittanceInformationUnstructured() {
        return remittanceInformationUnstructured;
    }

    public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
        this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    }

    public PaymentInitiationSctJson requestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
        return this;
    }

    /**
     * Get requestedExecutionDate
     *
     * @return requestedExecutionDate
     **/
    @ApiModelProperty(value = "")
    @Valid
    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public PaymentInitiationSctJson requestedExecutionTime(OffsetDateTime requestedExecutionTime) {
        this.requestedExecutionTime = requestedExecutionTime;
        return this;
    }

    /**
     * Get requestedExecutionTime
     *
     * @return requestedExecutionTime
     **/
    @ApiModelProperty(value = "")
    @Valid
    public OffsetDateTime getRequestedExecutionTime() {
        return requestedExecutionTime;
    }

    public void setRequestedExecutionTime(OffsetDateTime requestedExecutionTime) {
        this.requestedExecutionTime = requestedExecutionTime;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PaymentInitiationSctJson paymentInitiationSctJson = (PaymentInitiationSctJson) o;
        return Objects.equals(this.endToEndIdentification, paymentInitiationSctJson.endToEndIdentification) && Objects.equals(this.debtorAccount, paymentInitiationSctJson.debtorAccount) && Objects.equals(this.instructedAmount, paymentInitiationSctJson.instructedAmount) && Objects.equals(this.creditorAccount, paymentInitiationSctJson.creditorAccount) && Objects.equals(this.creditorAgent, paymentInitiationSctJson.creditorAgent) && Objects.equals(this.creditorName, paymentInitiationSctJson.creditorName) && Objects.equals(this.creditorAddress, paymentInitiationSctJson.creditorAddress) && Objects.equals(this.remittanceInformationUnstructured, paymentInitiationSctJson.remittanceInformationUnstructured) && Objects.equals(this.requestedExecutionDate, paymentInitiationSctJson.requestedExecutionDate) && Objects.equals(this.requestedExecutionTime, paymentInitiationSctJson.requestedExecutionTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endToEndIdentification, debtorAccount, instructedAmount, creditorAccount, creditorAgent,
            creditorName, creditorAddress, remittanceInformationUnstructured, requestedExecutionDate,
            requestedExecutionTime);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PaymentInitiationSctJson {\n");
        sb.append("    endToEndIdentification: ").append(toIndentedString(endToEndIdentification)).append("\n");
        sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
        sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
        sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
        sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
        sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
        sb.append("    creditorAddress: ").append(toIndentedString(creditorAddress)).append("\n");
        sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
        sb.append("    requestedExecutionDate: ").append(toIndentedString(requestedExecutionDate)).append("\n");
        sb.append("    requestedExecutionTime: ").append(toIndentedString(requestedExecutionTime)).append("\n");
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

