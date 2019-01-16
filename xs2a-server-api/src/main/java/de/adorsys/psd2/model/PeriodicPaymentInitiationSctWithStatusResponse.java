/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import java.util.Objects;

/**
 * JSON response body consistion of the corresponding periodic SCT payment initation JSON body together with an optional transaction status field. 
 */
@ApiModel(description = "JSON response body consistion of the corresponding periodic SCT payment initation JSON body together with an optional transaction status field. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class PeriodicPaymentInitiationSctWithStatusResponse   {
  @JsonProperty("endToEndIdentification")
  private String endToEndIdentification = null;

  @JsonProperty("debtorAccount")
  private AccountReference debtorAccount = null;

  @JsonProperty("instructedAmount")
  private Amount instructedAmount = null;

  @JsonProperty("creditorAccount")
  private AccountReference creditorAccount = null;

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
   * @return endToEndIdentification
  **/
  @ApiModelProperty(value = "")

@Size(max=35) 
  public String getEndToEndIdentification() {
    return endToEndIdentification;
  }

  public void setEndToEndIdentification(String endToEndIdentification) {
    this.endToEndIdentification = endToEndIdentification;
  }

  public PeriodicPaymentInitiationSctWithStatusResponse debtorAccount(AccountReference debtorAccount) {
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

  public AccountReference getDebtorAccount() {
    return debtorAccount;
  }

  public void setDebtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
  }

  public PeriodicPaymentInitiationSctWithStatusResponse instructedAmount(Amount instructedAmount) {
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

  public Amount getInstructedAmount() {
    return instructedAmount;
  }

  public void setInstructedAmount(Amount instructedAmount) {
    this.instructedAmount = instructedAmount;
  }

  public PeriodicPaymentInitiationSctWithStatusResponse creditorAccount(AccountReference creditorAccount) {
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

  public AccountReference getCreditorAccount() {
    return creditorAccount;
  }

  public void setCreditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
  }

  public PeriodicPaymentInitiationSctWithStatusResponse creditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
    return this;
  }

  /**
   * Get creditorAgent
   * @return creditorAgent
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}") 
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
   * @return creditorName
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Size(max=70) 
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
   * @return remittanceInformationUnstructured
  **/
  @ApiModelProperty(value = "")

@Size(max=140) 
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
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PeriodicPaymentInitiationSctWithStatusResponse periodicPaymentInitiationSctWithStatusResponse = (PeriodicPaymentInitiationSctWithStatusResponse) o;
    return Objects.equals(this.endToEndIdentification, periodicPaymentInitiationSctWithStatusResponse.endToEndIdentification) &&
        Objects.equals(this.debtorAccount, periodicPaymentInitiationSctWithStatusResponse.debtorAccount) &&
        Objects.equals(this.instructedAmount, periodicPaymentInitiationSctWithStatusResponse.instructedAmount) &&
        Objects.equals(this.creditorAccount, periodicPaymentInitiationSctWithStatusResponse.creditorAccount) &&
        Objects.equals(this.creditorAgent, periodicPaymentInitiationSctWithStatusResponse.creditorAgent) &&
        Objects.equals(this.creditorName, periodicPaymentInitiationSctWithStatusResponse.creditorName) &&
        Objects.equals(this.creditorAddress, periodicPaymentInitiationSctWithStatusResponse.creditorAddress) &&
        Objects.equals(this.remittanceInformationUnstructured, periodicPaymentInitiationSctWithStatusResponse.remittanceInformationUnstructured) &&
        Objects.equals(this.startDate, periodicPaymentInitiationSctWithStatusResponse.startDate) &&
        Objects.equals(this.endDate, periodicPaymentInitiationSctWithStatusResponse.endDate) &&
        Objects.equals(this.executionRule, periodicPaymentInitiationSctWithStatusResponse.executionRule) &&
        Objects.equals(this.frequency, periodicPaymentInitiationSctWithStatusResponse.frequency) &&
        Objects.equals(this.dayOfExecution, periodicPaymentInitiationSctWithStatusResponse.dayOfExecution) &&
        Objects.equals(this.transactionStatus, periodicPaymentInitiationSctWithStatusResponse.transactionStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endToEndIdentification, debtorAccount, instructedAmount, creditorAccount, creditorAgent, creditorName, creditorAddress, remittanceInformationUnstructured, startDate, endDate, executionRule, frequency, dayOfExecution, transactionStatus);
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
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

