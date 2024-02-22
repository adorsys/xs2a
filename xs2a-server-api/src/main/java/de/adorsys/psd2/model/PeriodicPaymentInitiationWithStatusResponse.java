/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Generic JSON response body consistion of the corresponding periodic payment initation JSON body together with an optional transaction status field. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class PeriodicPaymentInitiationWithStatusResponse {
    @JsonProperty("endToEndIdentification")
    private String endToEndIdentification = null;

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
    private RemittanceInformationStructuredMax140 remittanceInformationStructured = null;

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
     *
     * @return endToEndIdentification
     **/
    @Schema(description = "")
    @JsonProperty("endToEndIdentification")

    @Size(max = 35)
    public String getEndToEndIdentification() {
        return endToEndIdentification;
    }

  public void setEndToEndIdentification(String endToEndIdentification) {
    this.endToEndIdentification = endToEndIdentification;
  }

  public PeriodicPaymentInitiationWithStatusResponse debtorName(String debtorName) {
    this.debtorName = debtorName;
    return this;
  }

    /**
     * Debtor name.
     *
     * @return debtorName
     **/
    @Schema(example = "Debtor Name", description = "Debtor name.")
    @JsonProperty("debtorName")

    @Size(max = 70)
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
     *
     * @return debtorAccount
     **/
    @Schema(required = true, description = "")
    @JsonProperty("debtorAccount")
    @NotNull

    @Valid
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
     * Ultimate debtor.
     *
     * @return ultimateDebtor
     **/
    @Schema(example = "Ultimate Debtor", description = "Ultimate debtor.")
    @JsonProperty("ultimateDebtor")

    @Size(max = 70)
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
     *
     * @return instructedAmount
     **/
    @Schema(required = true, description = "")
    @JsonProperty("instructedAmount")
    @NotNull

    @Valid
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
     *
     * @return creditorAccount
     **/
    @Schema(required = true, description = "")
    @JsonProperty("creditorAccount")
    @NotNull

    @Valid
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
     * BICFI
     *
     * @return creditorAgent
     **/
    @Schema(example = "AAAADEBBXXX", description = "BICFI ")
    @JsonProperty("creditorAgent")

    @Pattern(regexp = "[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")   public String getCreditorAgent() {
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
     *
     * @return creditorId
     **/
    @Schema(description = "Identification of Creditors, e.g. a SEPA Creditor ID.")
    @JsonProperty("creditorId")

@Size(max=35)   public String getCreditorId() {
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
     * Creditor name.
     *
     * @return creditorName
     **/
    @Schema(example = "Creditor Name", required = true, description = "Creditor name.")
    @JsonProperty("creditorName")
    @NotNull

@Size(max=70)   public String getCreditorName() {
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
     *
     * @return creditorAddress
     **/
    @Schema(description = "")
    @JsonProperty("creditorAddress")

    @Valid
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
     * Ultimate creditor.
     *
     * @return ultimateCreditor
     **/
    @Schema(example = "Ultimate Creditor", description = "Ultimate creditor.")
  @JsonProperty("ultimateCreditor")

@Size(max=70)   public String getUltimateCreditor() {
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
  @Schema(description = "")
  @JsonProperty("purposeCode")

  @Valid
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
     * Unstructured remittance information.
     *
     * @return remittanceInformationUnstructured
     **/
    @Schema(example = "Ref Number Merchant", description = "Unstructured remittance information. ")
    @JsonProperty("remittanceInformationUnstructured")

@Size(max=140)   public String getRemittanceInformationUnstructured() {
    return remittanceInformationUnstructured;
  }

  public void setRemittanceInformationUnstructured(String remittanceInformationUnstructured) {
      this.remittanceInformationUnstructured = remittanceInformationUnstructured;
  }

    public PeriodicPaymentInitiationWithStatusResponse remittanceInformationStructured(RemittanceInformationStructuredMax140 remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
        return this;
    }

    /**
     * Get remittanceInformationStructured
     *
     * @return remittanceInformationStructured
     **/
    @Schema(description = "")
    @JsonProperty("remittanceInformationStructured")

    @Valid
    public RemittanceInformationStructuredMax140 getRemittanceInformationStructured() {
        return remittanceInformationStructured;
    }

    public void setRemittanceInformationStructured(RemittanceInformationStructuredMax140 remittanceInformationStructured) {
    this.remittanceInformationStructured = remittanceInformationStructured;
    }

    public PeriodicPaymentInitiationWithStatusResponse startDate(LocalDate startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * The first applicable day of execution starting from this date is the first payment.
     *
     * @return startDate
     **/
    @Schema(required = true, description = "The first applicable day of execution starting from this date is the first payment. ")
  @JsonProperty("startDate")
    @NotNull

  @Valid
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
     * The last applicable day of execution. If not given, it is an infinite standing order.
     *
     * @return endDate
     **/
    @Schema(description = "The last applicable day of execution. If not given, it is an infinite standing order. ")
  @JsonProperty("endDate")

  @Valid
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
  @Schema(description = "")
  @JsonProperty("executionRule")

  @Valid
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
  @Schema(required = true, description = "")
  @JsonProperty("frequency")
    @NotNull

  @Valid
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
  @Schema(description = "")
  @JsonProperty("dayOfExecution")

    @Valid
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
  @Schema(description = "")
  @JsonProperty("transactionStatus")

  @Valid
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
  @Schema(description = "Messages to the TPP on operational issues.")
  @JsonProperty("tppMessages")
    @Valid
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
    }
    PeriodicPaymentInitiationWithStatusResponse periodicPaymentInitiationWithStatusResponse = (PeriodicPaymentInitiationWithStatusResponse) o;
    return Objects.equals(this.endToEndIdentification, periodicPaymentInitiationWithStatusResponse.endToEndIdentification) &&
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
    return Objects.hash(endToEndIdentification, debtorName, debtorAccount, ultimateDebtor, instructedAmount, creditorAccount, creditorAgent, creditorId, creditorName, creditorAddress, ultimateCreditor, purposeCode, remittanceInformationUnstructured, remittanceInformationStructured, startDate, endDate, executionRule, frequency, dayOfExecution, transactionStatus, tppMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PeriodicPaymentInitiationWithStatusResponse {\n");

    sb.append("    endToEndIdentification: ").append(toIndentedString(endToEndIdentification)).append("\n");
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
