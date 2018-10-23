package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Objects;

/**
 * JSON response body consistion of the corresponding periodic SCT payment initation JSON body together with an
 * optional transaction status field.
 */
@ApiModel(description = "JSON response body consistion of the corresponding periodic SCT payment initation JSON body " +
    "together with an optional transaction status field. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class PeriodicPaymentInitiationSctWithStatusResponse {
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
    @JsonProperty("startDate")
    private LocalDate startDate = null;
    @JsonProperty("endDate")
    private LocalDate endDate = null;
    @JsonProperty("executionRule")
    private ExecutionRule executionRule = null;
    @JsonProperty("frequency")
    private FrequencyCode frequency = null;
    @JsonProperty("dayOfExecution")
    private DayOfExecution dayOfExecution = null;
    @JsonProperty("transactionStatus")
    private TransactionStatus transactionStatus = null;

    public PeriodicPaymentInitiationSctWithStatusResponse endToEndIdentification(String endToEndIdentification) {
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

    public PeriodicPaymentInitiationSctWithStatusResponse debtorAccount(Object debtorAccount) {
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

    public PeriodicPaymentInitiationSctWithStatusResponse instructedAmount(Amount instructedAmount) {
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

    public PeriodicPaymentInitiationSctWithStatusResponse creditorAccount(Object creditorAccount) {
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

    public PeriodicPaymentInitiationSctWithStatusResponse creditorAgent(String creditorAgent) {
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

    public PeriodicPaymentInitiationSctWithStatusResponse creditorName(String creditorName) {
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

    public PeriodicPaymentInitiationSctWithStatusResponse creditorAddress(Address creditorAddress) {
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

    public PeriodicPaymentInitiationSctWithStatusResponse remittanceInformationUnstructured(String remittanceInformationUnstructured) {
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

    public PeriodicPaymentInitiationSctWithStatusResponse startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * Get startDate
     *
     * @return startDate
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public PeriodicPaymentInitiationSctWithStatusResponse endDate(LocalDate endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Get endDate
     *
     * @return endDate
     **/
    @ApiModelProperty(value = "")
    @Valid
    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public PeriodicPaymentInitiationSctWithStatusResponse executionRule(ExecutionRule executionRule) {
        this.executionRule = executionRule;
        return this;
    }

    /**
     * Get executionRule
     *
     * @return executionRule
     **/
    @ApiModelProperty(value = "")
    @Valid
    public ExecutionRule getExecutionRule() {
        return executionRule;
    }

    public void setExecutionRule(ExecutionRule executionRule) {
        this.executionRule = executionRule;
    }

    public PeriodicPaymentInitiationSctWithStatusResponse frequency(FrequencyCode frequency) {
        this.frequency = frequency;
        return this;
    }

    /**
     * Get frequency
     *
     * @return frequency
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    @Valid
    public FrequencyCode getFrequency() {
        return frequency;
    }

    public void setFrequency(FrequencyCode frequency) {
        this.frequency = frequency;
    }

    public PeriodicPaymentInitiationSctWithStatusResponse dayOfExecution(DayOfExecution dayOfExecution) {
        this.dayOfExecution = dayOfExecution;
        return this;
    }

    /**
     * Get dayOfExecution
     *
     * @return dayOfExecution
     **/
    @ApiModelProperty(value = "")
    @Valid
    public DayOfExecution getDayOfExecution() {
        return dayOfExecution;
    }

    public void setDayOfExecution(DayOfExecution dayOfExecution) {
        this.dayOfExecution = dayOfExecution;
    }

    public PeriodicPaymentInitiationSctWithStatusResponse transactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
        return this;
    }

    /**
     * Get transactionStatus
     *
     * @return transactionStatus
     **/
    @ApiModelProperty(value = "")
    @Valid
    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PeriodicPaymentInitiationSctWithStatusResponse periodicPaymentInitiationSctWithStatusResponse =
            (PeriodicPaymentInitiationSctWithStatusResponse) o;
        return Objects.equals(this.endToEndIdentification,
            periodicPaymentInitiationSctWithStatusResponse.endToEndIdentification) && Objects.equals(this.debtorAccount, periodicPaymentInitiationSctWithStatusResponse.debtorAccount) && Objects.equals(this.instructedAmount, periodicPaymentInitiationSctWithStatusResponse.instructedAmount) && Objects.equals(this.creditorAccount, periodicPaymentInitiationSctWithStatusResponse.creditorAccount) && Objects.equals(this.creditorAgent, periodicPaymentInitiationSctWithStatusResponse.creditorAgent) && Objects.equals(this.creditorName, periodicPaymentInitiationSctWithStatusResponse.creditorName) && Objects.equals(this.creditorAddress, periodicPaymentInitiationSctWithStatusResponse.creditorAddress) && Objects.equals(this.remittanceInformationUnstructured, periodicPaymentInitiationSctWithStatusResponse.remittanceInformationUnstructured) && Objects.equals(this.startDate, periodicPaymentInitiationSctWithStatusResponse.startDate) && Objects.equals(this.endDate, periodicPaymentInitiationSctWithStatusResponse.endDate) && Objects.equals(this.executionRule, periodicPaymentInitiationSctWithStatusResponse.executionRule) && Objects.equals(this.frequency, periodicPaymentInitiationSctWithStatusResponse.frequency) && Objects.equals(this.dayOfExecution, periodicPaymentInitiationSctWithStatusResponse.dayOfExecution) && Objects.equals(this.transactionStatus, periodicPaymentInitiationSctWithStatusResponse.transactionStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(endToEndIdentification, debtorAccount, instructedAmount, creditorAccount, creditorAgent,
            creditorName, creditorAddress, remittanceInformationUnstructured, startDate, endDate, executionRule,
            frequency, dayOfExecution, transactionStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PeriodicPaymentInitiationSctWithStatusResponse {\n");
        sb.append("    endToEndIdentification: ").append(toIndentedString(endToEndIdentification)).append("\n");
        sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
        sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
        sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
        sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
        sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
        sb.append("    creditorAddress: ").append(toIndentedString(creditorAddress)).append("\n");
        sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
        sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
        sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
        sb.append("    executionRule: ").append(toIndentedString(executionRule)).append("\n");
        sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
        sb.append("    dayOfExecution: ").append(toIndentedString(dayOfExecution)).append("\n");
        sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
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

