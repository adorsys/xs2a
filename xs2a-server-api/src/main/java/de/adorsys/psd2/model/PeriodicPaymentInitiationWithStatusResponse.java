package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generic JSON response body consistion of the corresponding periodic payment initation JSON body together with an optional transaction status field.
 */
@ApiModel(description = "Generic JSON response body consistion of the corresponding periodic payment initation JSON body together with an optional transaction status field. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class PeriodicPaymentInitiationWithStatusResponse   {
  @JsonProperty("endToEndIdentification")
  private String endToEndIdentification = null;

  @JsonProperty("instructionIdentification")
  private String instructionIdentification = null;

  @JsonProperty("debtorName")
  private String debtorName = null;

  @JsonProperty("debtorAccount")
  private AccountReference debtorAccount = null;

  @JsonProperty("ultimateDebtor")
  private String ultimateDebtor = null;

  @JsonProperty("instructedAmount")
  private Amount instructedAmount = null;

  @JsonProperty("creditorAccount")
  private AccountReference creditorAccount = null;

  @JsonProperty("creditorAgent")
  private String creditorAgent = null;

  @JsonProperty("creditorId")
  private String creditorId = null;

  @JsonProperty("creditorName")
  private String creditorName = null;

  @JsonProperty("creditorAddress")
  private Address creditorAddress = null;

  @JsonProperty("ultimateCreditor")
  private String ultimateCreditor = null;

  @JsonProperty("purposeCode")
  private PurposeCode purposeCode = null;

  @JsonProperty("remittanceInformationUnstructured")
  private String remittanceInformationUnstructured = null;

  @JsonProperty("remittanceInformationStructured")
  private String remittanceInformationStructured = null;

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

  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessageGeneric> tppMessages = null;

  public PeriodicPaymentInitiationWithStatusResponse endToEndIdentification(String endToEndIdentification) {
    this.endToEndIdentification = endToEndIdentification;
    return this;
  }

  /**
   * Get endToEndIdentification
   * @return endToEndIdentification
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("endToEndIdentification")
  public String getEndToEndIdentification() {
    return endToEndIdentification;
  }

  public void setEndToEndIdentification(String endToEndIdentification) {
    this.endToEndIdentification = endToEndIdentification;
  }

  public PeriodicPaymentInitiationWithStatusResponse instructionIdentification(String instructionIdentification) {
    this.instructionIdentification = instructionIdentification;
    return this;
  }

  /**
   * Get instructionIdentification
   * @return instructionIdentification
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("instructionIdentification")
  public String getInstructionIdentification() {
    return instructionIdentification;
  }

  public void setInstructionIdentification(String instructionIdentification) {
    this.instructionIdentification = instructionIdentification;
  }

  public PeriodicPaymentInitiationWithStatusResponse debtorName(String debtorName) {
    this.debtorName = debtorName;
    return this;
  }

  /**
   * Get debtorName
   * @return debtorName
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("debtorName")
  public String getDebtorName() {
    return debtorName;
  }

  public void setDebtorName(String debtorName) {
    this.debtorName = debtorName;
  }

  public PeriodicPaymentInitiationWithStatusResponse debtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
    return this;
  }

  /**
   * Get debtorAccount
   * @return debtorAccount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("debtorAccount")
  public AccountReference getDebtorAccount() {
    return debtorAccount;
  }

  public void setDebtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
  }

  public PeriodicPaymentInitiationWithStatusResponse ultimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
    return this;
  }

  /**
   * Get ultimateDebtor
   * @return ultimateDebtor
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("ultimateDebtor")
  public String getUltimateDebtor() {
    return ultimateDebtor;
  }

  public void setUltimateDebtor(String ultimateDebtor) {
    this.ultimateDebtor = ultimateDebtor;
  }

  public PeriodicPaymentInitiationWithStatusResponse instructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
    return this;
  }

  /**
   * Get instructedAmount
   * @return instructedAmount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("instructedAmount")
  public Amount getInstructedAmount() {
    return instructedAmount;
  }

  public void setInstructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
  }

  public PeriodicPaymentInitiationWithStatusResponse creditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
    return this;
  }

  /**
   * Get creditorAccount
   * @return creditorAccount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("creditorAccount")
  public AccountReference getCreditorAccount() {
    return creditorAccount;
  }

  public void setCreditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
  }

  public PeriodicPaymentInitiationWithStatusResponse creditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
    return this;
  }

  /**
   * Get creditorAgent
   * @return creditorAgent
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")

  @JsonProperty("creditorAgent")
  public String getCreditorAgent() {
    return creditorAgent;
  }

  public void setCreditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
  }

  public PeriodicPaymentInitiationWithStatusResponse creditorId(String creditorId) {
    this.creditorId = creditorId;
    return this;
  }

  /**
   * Identification of Creditors, e.g. a SEPA Creditor ID.
   * @return creditorId
  **/
  @ApiModelProperty(value = "Identification of Creditors, e.g. a SEPA Creditor ID.")

@Size(max=35)

  @JsonProperty("creditorId")
  public String getCreditorId() {
    return creditorId;
  }

  public void setCreditorId(String creditorId) {
    this.creditorId = creditorId;
  }

  public PeriodicPaymentInitiationWithStatusResponse creditorName(String creditorName) {
    this.creditorName = creditorName;
    return this;
  }

  /**
   * Get creditorName
   * @return creditorName
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Size(max=70)

  @JsonProperty("creditorName")
  public String getCreditorName() {
    return creditorName;
  }

  public void setCreditorName(String creditorName) {
    this.creditorName = creditorName;
  }

  public PeriodicPaymentInitiationWithStatusResponse creditorAddress(Address creditorAddress) {
    this.creditorAddress = creditorAddress;
    return this;
  }

  /**
   * Get creditorAddress
   * @return creditorAddress
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("creditorAddress")
  public Address getCreditorAddress() {
    return creditorAddress;
  }

  public void setCreditorAddress(Address creditorAddress) {
    this.creditorAddress = creditorAddress;
  }

  public PeriodicPaymentInitiationWithStatusResponse ultimateCreditor(String ultimateCreditor) {
    this.ultimateCreditor = ultimateCreditor;
    return this;
  }

  /**
   * Get ultimateCreditor
   * @return ultimateCreditor
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("ultimateCreditor")
  public String getUltimateCreditor() {
    return ultimateCreditor;
  }

  public void setUltimateCreditor(String ultimateCreditor) {
    this.ultimateCreditor = ultimateCreditor;
  }

  public PeriodicPaymentInitiationWithStatusResponse purposeCode(PurposeCode purposeCode) {
    this.purposeCode = purposeCode;
    return this;
  }

  /**
   * Get purposeCode
   * @return purposeCode
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("purposeCode")
  public PurposeCode getPurposeCode() {
    return purposeCode;
  }

  public void setPurposeCode(PurposeCode purposeCode) {
    this.purposeCode = purposeCode;
  }

  public PeriodicPaymentInitiationWithStatusResponse remittanceInformationUnstructured(String remittanceInformationUnstructured) {
    this.remittanceInformationUnstructured = remittanceInformationUnstructured;
    return this;
  }

  /**
   * Get remittanceInformationUnstructured
   * @return remittanceInformationUnstructured
  **/
  @ApiModelProperty(value = "")

@Size(max=140)

  @JsonProperty("remittanceInformationUnstructured")
  public String getRemittanceInformationUnstructured() {
    return remittanceInformationUnstructured;
  }

  public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
    this.remittanceInformationUnstructured = remittanceInformationUnstructured;
  }

  public PeriodicPaymentInitiationWithStatusResponse remittanceInformationStructured(String remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
    return this;
  }

  /**
   * Get remittanceInformationStructured
   * @return remittanceInformationStructured
  **/
  @ApiModelProperty(value = "")

@Size(max=140)

  @JsonProperty("remittanceInformationStructured")
  public String getRemittanceInformationStructured() {
    return remittanceInformationStructured;
  }

  public void setRemittanceInformationStructured(String remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
  }

  public PeriodicPaymentInitiationWithStatusResponse startDate(LocalDate startDate) {
    this.startDate = startDate;
    return this;
  }

  /**
   * Get startDate
   * @return startDate
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("startDate")
  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public PeriodicPaymentInitiationWithStatusResponse endDate(LocalDate endDate) {
    this.endDate = endDate;
    return this;
  }

  /**
   * Get endDate
   * @return endDate
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("endDate")
  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public PeriodicPaymentInitiationWithStatusResponse executionRule(ExecutionRule executionRule) {
    this.executionRule = executionRule;
    return this;
  }

  /**
   * Get executionRule
   * @return executionRule
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("executionRule")
  public ExecutionRule getExecutionRule() {
    return executionRule;
  }

  public void setExecutionRule(ExecutionRule executionRule) {
    this.executionRule = executionRule;
  }

  public PeriodicPaymentInitiationWithStatusResponse frequency(FrequencyCode frequency) {
    this.frequency = frequency;
    return this;
  }

  /**
   * Get frequency
   * @return frequency
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("frequency")
  public FrequencyCode getFrequency() {
    return frequency;
  }

  public void setFrequency(FrequencyCode frequency) {
    this.frequency = frequency;
  }

  public PeriodicPaymentInitiationWithStatusResponse dayOfExecution(DayOfExecution dayOfExecution) {
    this.dayOfExecution = dayOfExecution;
    return this;
  }

  /**
   * Get dayOfExecution
   * @return dayOfExecution
  **/
  @ApiModelProperty(value = "")

  @Valid
@Size(max=2)

  @JsonProperty("dayOfExecution")
  public DayOfExecution getDayOfExecution() {
    return dayOfExecution;
  }

  public void setDayOfExecution(DayOfExecution dayOfExecution) {
    this.dayOfExecution = dayOfExecution;
  }

  public PeriodicPaymentInitiationWithStatusResponse transactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
    return this;
  }

  /**
   * Get transactionStatus
   * @return transactionStatus
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("transactionStatus")
  public TransactionStatus getTransactionStatus() {
    return transactionStatus;
  }

  public void setTransactionStatus(TransactionStatus transactionStatus) {
    this.transactionStatus = transactionStatus;
  }

  public PeriodicPaymentInitiationWithStatusResponse tppMessages(List<TppMessageGeneric> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public PeriodicPaymentInitiationWithStatusResponse addTppMessagesItem(TppMessageGeneric tppMessagesItem) {
    if (this.tppMessages == null) {
      this.tppMessages = new ArrayList<>();
    }
    this.tppMessages.add(tppMessagesItem);
    return this;
  }

  /**
   * Messages to the TPP on operational issues.
   * @return tppMessages
  **/
  @ApiModelProperty(value = "Messages to the TPP on operational issues.")

  @Valid


  @JsonProperty("tppMessages")
  public List<TppMessageGeneric> getTppMessages() {
    return tppMessages;
  }

  public void setTppMessages(List<TppMessageGeneric> tppMessages) {
    this.tppMessages = tppMessages;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    PeriodicPaymentInitiationWithStatusResponse periodicPaymentInitiationWithStatusResponse = (PeriodicPaymentInitiationWithStatusResponse) o;
    return Objects.equals(this.endToEndIdentification, periodicPaymentInitiationWithStatusResponse.endToEndIdentification) &&
    Objects.equals(this.instructionIdentification, periodicPaymentInitiationWithStatusResponse.instructionIdentification) &&
    Objects.equals(this.debtorName, periodicPaymentInitiationWithStatusResponse.debtorName) &&
    Objects.equals(this.debtorAccount, periodicPaymentInitiationWithStatusResponse.debtorAccount) &&
    Objects.equals(this.ultimateDebtor, periodicPaymentInitiationWithStatusResponse.ultimateDebtor) &&
    Objects.equals(this.instructedAmount, periodicPaymentInitiationWithStatusResponse.instructedAmount) &&
    Objects.equals(this.creditorAccount, periodicPaymentInitiationWithStatusResponse.creditorAccount) &&
    Objects.equals(this.creditorAgent, periodicPaymentInitiationWithStatusResponse.creditorAgent) &&
    Objects.equals(this.creditorId, periodicPaymentInitiationWithStatusResponse.creditorId) &&
    Objects.equals(this.creditorName, periodicPaymentInitiationWithStatusResponse.creditorName) &&
    Objects.equals(this.creditorAddress, periodicPaymentInitiationWithStatusResponse.creditorAddress) &&
    Objects.equals(this.ultimateCreditor, periodicPaymentInitiationWithStatusResponse.ultimateCreditor) &&
    Objects.equals(this.purposeCode, periodicPaymentInitiationWithStatusResponse.purposeCode) &&
    Objects.equals(this.remittanceInformationUnstructured, periodicPaymentInitiationWithStatusResponse.remittanceInformationUnstructured) &&
    Objects.equals(this.remittanceInformationStructured, periodicPaymentInitiationWithStatusResponse.remittanceInformationStructured) &&
    Objects.equals(this.startDate, periodicPaymentInitiationWithStatusResponse.startDate) &&
    Objects.equals(this.endDate, periodicPaymentInitiationWithStatusResponse.endDate) &&
    Objects.equals(this.executionRule, periodicPaymentInitiationWithStatusResponse.executionRule) &&
    Objects.equals(this.frequency, periodicPaymentInitiationWithStatusResponse.frequency) &&
    Objects.equals(this.dayOfExecution, periodicPaymentInitiationWithStatusResponse.dayOfExecution) &&
    Objects.equals(this.transactionStatus, periodicPaymentInitiationWithStatusResponse.transactionStatus) &&
    Objects.equals(this.tppMessages, periodicPaymentInitiationWithStatusResponse.tppMessages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endToEndIdentification, instructionIdentification, debtorName, debtorAccount, ultimateDebtor, instructedAmount, creditorAccount, creditorAgent, creditorId, creditorName, creditorAddress, ultimateCreditor, purposeCode, remittanceInformationUnstructured, remittanceInformationStructured, startDate, endDate, executionRule, frequency, dayOfExecution, transactionStatus, tppMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PeriodicPaymentInitiationWithStatusResponse {\n");

    sb.append("    endToEndIdentification: ").append(toIndentedString(endToEndIdentification)).append("\n");
    sb.append("    instructionIdentification: ").append(toIndentedString(instructionIdentification)).append("\n");
    sb.append("    debtorName: ").append(toIndentedString(debtorName)).append("\n");
    sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
    sb.append("    ultimateDebtor: ").append(toIndentedString(ultimateDebtor)).append("\n");
    sb.append("    instructedAmount: ").append(toIndentedString(instructedAmount)).append("\n");
    sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
    sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
    sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
    sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
    sb.append("    creditorAddress: ").append(toIndentedString(creditorAddress)).append("\n");
    sb.append("    ultimateCreditor: ").append(toIndentedString(ultimateCreditor)).append("\n");
    sb.append("    purposeCode: ").append(toIndentedString(purposeCode)).append("\n");
    sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
    sb.append("    remittanceInformationStructured: ").append(toIndentedString(remittanceInformationStructured)).append("\n");
    sb.append("    startDate: ").append(toIndentedString(startDate)).append("\n");
    sb.append("    endDate: ").append(toIndentedString(endDate)).append("\n");
    sb.append("    executionRule: ").append(toIndentedString(executionRule)).append("\n");
    sb.append("    frequency: ").append(toIndentedString(frequency)).append("\n");
    sb.append("    dayOfExecution: ").append(toIndentedString(dayOfExecution)).append("\n");
    sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
    sb.append("    tppMessages: ").append(toIndentedString(tppMessages)).append("\n");
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

