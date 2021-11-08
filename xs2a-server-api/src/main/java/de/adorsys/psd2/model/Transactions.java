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
import java.util.Map;
import java.util.Objects;

/**
 * Transaction details.
 */
@ApiModel(description = "Transaction details.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-11-05T12:22:49.487689+02:00[Europe/Kiev]")

public class Transactions   {
  @JsonProperty("transactionId")
  private String transactionId = null;

  @JsonProperty("entryReference")
  private String entryReference = null;

  @JsonProperty("endToEndId")
  private String endToEndId = null;

  @JsonProperty("batchIndicator")
  private Boolean batchIndicator = null;

  @JsonProperty("batchNumberOfTransactions")
  private Integer batchNumberOfTransactions = null;

  @JsonProperty("mandateId")
  private String mandateId = null;

  @JsonProperty("checkId")
  private String checkId = null;

  @JsonProperty("creditorId")
  private String creditorId = null;

  @JsonProperty("bookingDate")
  private LocalDate bookingDate = null;

  @JsonProperty("valueDate")
  private LocalDate valueDate = null;

  @JsonProperty("transactionAmount")
  private Amount transactionAmount = null;

  @JsonProperty("currencyExchange")
  private ReportExchangeRateList currencyExchange = null;

  @JsonProperty("creditorName")
  private String creditorName = null;

  @JsonProperty("creditorAccount")
  private AccountReference creditorAccount = null;

  @JsonProperty("creditorAgent")
  private String creditorAgent = null;

  @JsonProperty("ultimateCreditor")
  private String ultimateCreditor = null;

  @JsonProperty("debtorName")
  private String debtorName = null;

  @JsonProperty("debtorAccount")
  private AccountReference debtorAccount = null;

  @JsonProperty("debtorAgent")
  private String debtorAgent = null;

  @JsonProperty("ultimateDebtor")
  private String ultimateDebtor = null;

  @JsonProperty("remittanceInformationUnstructured")
  private String remittanceInformationUnstructured = null;

  @JsonProperty("remittanceInformationUnstructuredArray")
  private RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray = null;

  @JsonProperty("remittanceInformationStructured")
  private String remittanceInformationStructured = null;

  @JsonProperty("remittanceInformationStructuredArray")
  private RemittanceInformationStructuredArray remittanceInformationStructuredArray = null;

  @JsonProperty("entryDetails")
  private EntryDetails entryDetails = null;

  @JsonProperty("additionalInformation")
  private String additionalInformation = null;

  @JsonProperty("additionalInformationStructured")
  private AdditionalInformationStructured additionalInformationStructured = null;

  @JsonProperty("purposeCode")
  private PurposeCode purposeCode = null;

  @JsonProperty("bankTransactionCode")
  private String bankTransactionCode = null;

  @JsonProperty("proprietaryBankTransactionCode")
  private String proprietaryBankTransactionCode = null;

  @JsonProperty("balanceAfterTransaction")
  private Balance balanceAfterTransaction = null;

  @JsonProperty("_links")
  private Map _links = null;

  public Transactions transactionId(String transactionId) {
    this.transactionId = transactionId;
    return this;
  }

  /**
   * Get transactionId
   * @return transactionId
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("transactionId")
  public String getTransactionId() {
    return transactionId;
  }

  public void setTransactionId(String transactionId) {
    this.transactionId = transactionId;
  }

  public Transactions entryReference(String entryReference) {
    this.entryReference = entryReference;
    return this;
  }

  /**
   * Get entryReference
   * @return entryReference
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("entryReference")
  public String getEntryReference() {
    return entryReference;
  }

  public void setEntryReference(String entryReference) {
    this.entryReference = entryReference;
  }

  public Transactions endToEndId(String endToEndId) {
    this.endToEndId = endToEndId;
    return this;
  }

  /**
   * Unique end to end identity.
   * @return endToEndId
  **/
  @ApiModelProperty(value = "Unique end to end identity.")

@Size(max=35)

  @JsonProperty("endToEndId")
  public String getEndToEndId() {
    return endToEndId;
  }

  public void setEndToEndId(String endToEndId) {
    this.endToEndId = endToEndId;
  }

  public Transactions batchIndicator(Boolean batchIndicator) {
    this.batchIndicator = batchIndicator;
    return this;
  }

  /**
   * If this indicator equals true, then the related entry is a batch entry.
   * @return batchIndicator
  **/
  @ApiModelProperty(value = "If this indicator equals true, then the related entry is a batch entry. ")



  @JsonProperty("batchIndicator")
  public Boolean isBatchIndicator() {
    return batchIndicator;
  }

  public void setBatchIndicator(Boolean batchIndicator) {
    this.batchIndicator = batchIndicator;
  }

  public Transactions batchNumberOfTransactions(Integer batchNumberOfTransactions) {
    this.batchNumberOfTransactions = batchNumberOfTransactions;
    return this;
  }

  /**
   * Shall be used if and only if the batchIndicator is contained and equals true.
   * @return batchNumberOfTransactions
  **/
  @ApiModelProperty(value = "Shall be used if and only if the batchIndicator is contained and equals true. ")



  @JsonProperty("batchNumberOfTransactions")
  public Integer getBatchNumberOfTransactions() {
    return batchNumberOfTransactions;
  }

  public void setBatchNumberOfTransactions(Integer batchNumberOfTransactions) {
    this.batchNumberOfTransactions = batchNumberOfTransactions;
  }

  public Transactions mandateId(String mandateId) {
    this.mandateId = mandateId;
    return this;
  }

  /**
   * Identification of Mandates, e.g. a SEPA Mandate ID.
   * @return mandateId
  **/
  @ApiModelProperty(value = "Identification of Mandates, e.g. a SEPA Mandate ID.")

@Size(max=35)

  @JsonProperty("mandateId")
  public String getMandateId() {
    return mandateId;
  }

  public void setMandateId(String mandateId) {
    this.mandateId = mandateId;
  }

  public Transactions checkId(String checkId) {
    this.checkId = checkId;
    return this;
  }

  /**
   * Identification of a Cheque.
   * @return checkId
  **/
  @ApiModelProperty(value = "Identification of a Cheque.")

@Size(max=35)

  @JsonProperty("checkId")
  public String getCheckId() {
    return checkId;
  }

  public void setCheckId(String checkId) {
    this.checkId = checkId;
  }

  public Transactions creditorId(String creditorId) {
    this.creditorId = creditorId;
    return this;
  }

  /**
   * Get creditorId
   * @return creditorId
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("creditorId")
  public String getCreditorId() {
    return creditorId;
  }

  public void setCreditorId(String creditorId) {
    this.creditorId = creditorId;
  }

  public Transactions bookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
    return this;
  }

  /**
   * Get bookingDate
   * @return bookingDate
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("bookingDate")
  public LocalDate getBookingDate() {
    return bookingDate;
  }

  public void setBookingDate(LocalDate bookingDate) {
    this.bookingDate = bookingDate;
  }

  public Transactions valueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
    return this;
  }

  /**
   * The Date at which assets become available to the account owner in case of a credit, or cease to be available to the account owner in case of a debit entry. **Usage:** If entry status is pending and value date is present, then the value date refers to an expected/requested value date.
   * @return valueDate
  **/
  @ApiModelProperty(value = "The Date at which assets become available to the account owner in case of a credit, or cease to be available to the account owner in case of a debit entry. **Usage:** If entry status is pending and value date is present, then the value date refers to an expected/requested value date.")

  @Valid


  @JsonProperty("valueDate")
  public LocalDate getValueDate() {
    return valueDate;
  }

  public void setValueDate(LocalDate valueDate) {
    this.valueDate = valueDate;
  }

  public Transactions transactionAmount(Amount transactionAmount) {
    this.transactionAmount = transactionAmount;
    return this;
  }

  /**
   * Get transactionAmount
   * @return transactionAmount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("transactionAmount")
  public Amount getTransactionAmount() {
    return transactionAmount;
  }

  public void setTransactionAmount(Amount transactionAmount) {
    this.transactionAmount = transactionAmount;
  }

  public Transactions currencyExchange(ReportExchangeRateList currencyExchange) {
    this.currencyExchange = currencyExchange;
    return this;
  }

  /**
   * Get currencyExchange
   * @return currencyExchange
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("currencyExchange")
  public ReportExchangeRateList getCurrencyExchange() {
    return currencyExchange;
  }

  public void setCurrencyExchange(ReportExchangeRateList currencyExchange) {
    this.currencyExchange = currencyExchange;
  }

  public Transactions creditorName(String creditorName) {
    this.creditorName = creditorName;
    return this;
  }

  /**
   * Get creditorName
   * @return creditorName
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("creditorName")
  public String getCreditorName() {
    return creditorName;
  }

  public void setCreditorName(String creditorName) {
    this.creditorName = creditorName;
  }

  public Transactions creditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
    return this;
  }

  /**
   * Get creditorAccount
   * @return creditorAccount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("creditorAccount")
  public AccountReference getCreditorAccount() {
    return creditorAccount;
  }

  public void setCreditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
  }

  public Transactions creditorAgent(String creditorAgent) {
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

  public Transactions ultimateCreditor(String ultimateCreditor) {
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

  public Transactions debtorName(String debtorName) {
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

  public Transactions debtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
    return this;
  }

  /**
   * Get debtorAccount
   * @return debtorAccount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("debtorAccount")
  public AccountReference getDebtorAccount() {
    return debtorAccount;
  }

  public void setDebtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
  }

  public Transactions debtorAgent(String debtorAgent) {
    this.debtorAgent = debtorAgent;
    return this;
  }

  /**
   * Get debtorAgent
   * @return debtorAgent
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")

  @JsonProperty("debtorAgent")
  public String getDebtorAgent() {
    return debtorAgent;
  }

  public void setDebtorAgent(String debtorAgent) {
    this.debtorAgent = debtorAgent;
  }

  public Transactions ultimateDebtor(String ultimateDebtor) {
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

  public Transactions remittanceInformationUnstructured(String remittanceInformationUnstructured) {
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

  public Transactions remittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
    this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
    return this;
  }

  /**
   * Get remittanceInformationUnstructuredArray
   * @return remittanceInformationUnstructuredArray
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("remittanceInformationUnstructuredArray")
  public RemittanceInformationUnstructuredArray getRemittanceInformationUnstructuredArray() {
    return remittanceInformationUnstructuredArray;
  }

  public void setRemittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
    this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
  }

  public Transactions remittanceInformationStructured(String remittanceInformationStructured) {
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

  public Transactions remittanceInformationStructuredArray(RemittanceInformationStructuredArray remittanceInformationStructuredArray) {
    this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
    return this;
  }

  /**
   * Get remittanceInformationStructuredArray
   * @return remittanceInformationStructuredArray
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("remittanceInformationStructuredArray")
  public RemittanceInformationStructuredArray getRemittanceInformationStructuredArray() {
    return remittanceInformationStructuredArray;
  }

  public void setRemittanceInformationStructuredArray(RemittanceInformationStructuredArray remittanceInformationStructuredArray) {
    this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
  }

  public Transactions entryDetails(EntryDetails entryDetails) {
    this.entryDetails = entryDetails;
    return this;
  }

  /**
   * Get entryDetails
   * @return entryDetails
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("entryDetails")
  public EntryDetails getEntryDetails() {
    return entryDetails;
  }

  public void setEntryDetails(EntryDetails entryDetails) {
    this.entryDetails = entryDetails;
  }

  public Transactions additionalInformation(String additionalInformation) {
    this.additionalInformation = additionalInformation;
    return this;
  }

  /**
   * Get additionalInformation
   * @return additionalInformation
  **/
  @ApiModelProperty(value = "")

@Size(max=500)

  @JsonProperty("additionalInformation")
  public String getAdditionalInformation() {
    return additionalInformation;
  }

  public void setAdditionalInformation(String additionalInformation) {
    this.additionalInformation = additionalInformation;
  }

  public Transactions additionalInformationStructured(AdditionalInformationStructured additionalInformationStructured) {
    this.additionalInformationStructured = additionalInformationStructured;
    return this;
  }

  /**
   * Get additionalInformationStructured
   * @return additionalInformationStructured
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("additionalInformationStructured")
  public AdditionalInformationStructured getAdditionalInformationStructured() {
    return additionalInformationStructured;
  }

  public void setAdditionalInformationStructured(AdditionalInformationStructured additionalInformationStructured) {
    this.additionalInformationStructured = additionalInformationStructured;
  }

  public Transactions purposeCode(PurposeCode purposeCode) {
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

  public Transactions bankTransactionCode(String bankTransactionCode) {
    this.bankTransactionCode = bankTransactionCode;
    return this;
  }

  /**
   * Get bankTransactionCode
   * @return bankTransactionCode
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("bankTransactionCode")
  public String getBankTransactionCode() {
    return bankTransactionCode;
  }

  public void setBankTransactionCode(String bankTransactionCode) {
    this.bankTransactionCode = bankTransactionCode;
  }

  public Transactions proprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
    this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
    return this;
  }

  /**
   * Get proprietaryBankTransactionCode
   * @return proprietaryBankTransactionCode
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("proprietaryBankTransactionCode")
  public String getProprietaryBankTransactionCode() {
    return proprietaryBankTransactionCode;
  }

  public void setProprietaryBankTransactionCode(String proprietaryBankTransactionCode) {
    this.proprietaryBankTransactionCode = proprietaryBankTransactionCode;
  }

  public Transactions balanceAfterTransaction(Balance balanceAfterTransaction) {
    this.balanceAfterTransaction = balanceAfterTransaction;
    return this;
  }

  /**
   * Get balanceAfterTransaction
   * @return balanceAfterTransaction
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("balanceAfterTransaction")
  public Balance getBalanceAfterTransaction() {
    return balanceAfterTransaction;
  }

  public void setBalanceAfterTransaction(Balance balanceAfterTransaction) {
    this.balanceAfterTransaction = balanceAfterTransaction;
  }

  public Transactions _links(Map _links) {
    this._links = _links;
    return this;
  }

  /**
   * Get _links
   * @return _links
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("_links")
  public Map getLinks() {
    return _links;
  }

  public void setLinks(Map _links) {
    this._links = _links;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    Transactions transactions = (Transactions) o;
    return Objects.equals(this.transactionId, transactions.transactionId) &&
    Objects.equals(this.entryReference, transactions.entryReference) &&
    Objects.equals(this.endToEndId, transactions.endToEndId) &&
    Objects.equals(this.batchIndicator, transactions.batchIndicator) &&
    Objects.equals(this.batchNumberOfTransactions, transactions.batchNumberOfTransactions) &&
    Objects.equals(this.mandateId, transactions.mandateId) &&
    Objects.equals(this.checkId, transactions.checkId) &&
    Objects.equals(this.creditorId, transactions.creditorId) &&
    Objects.equals(this.bookingDate, transactions.bookingDate) &&
    Objects.equals(this.valueDate, transactions.valueDate) &&
    Objects.equals(this.transactionAmount, transactions.transactionAmount) &&
    Objects.equals(this.currencyExchange, transactions.currencyExchange) &&
    Objects.equals(this.creditorName, transactions.creditorName) &&
    Objects.equals(this.creditorAccount, transactions.creditorAccount) &&
    Objects.equals(this.creditorAgent, transactions.creditorAgent) &&
    Objects.equals(this.ultimateCreditor, transactions.ultimateCreditor) &&
    Objects.equals(this.debtorName, transactions.debtorName) &&
    Objects.equals(this.debtorAccount, transactions.debtorAccount) &&
    Objects.equals(this.debtorAgent, transactions.debtorAgent) &&
    Objects.equals(this.ultimateDebtor, transactions.ultimateDebtor) &&
    Objects.equals(this.remittanceInformationUnstructured, transactions.remittanceInformationUnstructured) &&
    Objects.equals(this.remittanceInformationUnstructuredArray, transactions.remittanceInformationUnstructuredArray) &&
    Objects.equals(this.remittanceInformationStructured, transactions.remittanceInformationStructured) &&
    Objects.equals(this.remittanceInformationStructuredArray, transactions.remittanceInformationStructuredArray) &&
    Objects.equals(this.entryDetails, transactions.entryDetails) &&
    Objects.equals(this.additionalInformation, transactions.additionalInformation) &&
    Objects.equals(this.additionalInformationStructured, transactions.additionalInformationStructured) &&
    Objects.equals(this.purposeCode, transactions.purposeCode) &&
    Objects.equals(this.bankTransactionCode, transactions.bankTransactionCode) &&
    Objects.equals(this.proprietaryBankTransactionCode, transactions.proprietaryBankTransactionCode) &&
    Objects.equals(this.balanceAfterTransaction, transactions.balanceAfterTransaction) &&
    Objects.equals(this._links, transactions._links);
  }

  @Override
  public int hashCode() {
    return Objects.hash(transactionId, entryReference, endToEndId, batchIndicator, batchNumberOfTransactions, mandateId, checkId, creditorId, bookingDate, valueDate, transactionAmount, currencyExchange, creditorName, creditorAccount, creditorAgent, ultimateCreditor, debtorName, debtorAccount, debtorAgent, ultimateDebtor, remittanceInformationUnstructured, remittanceInformationUnstructuredArray, remittanceInformationStructured, remittanceInformationStructuredArray, entryDetails, additionalInformation, additionalInformationStructured, purposeCode, bankTransactionCode, proprietaryBankTransactionCode, balanceAfterTransaction, _links);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Transactions {\n");

    sb.append("    transactionId: ").append(toIndentedString(transactionId)).append("\n");
    sb.append("    entryReference: ").append(toIndentedString(entryReference)).append("\n");
    sb.append("    endToEndId: ").append(toIndentedString(endToEndId)).append("\n");
    sb.append("    batchIndicator: ").append(toIndentedString(batchIndicator)).append("\n");
    sb.append("    batchNumberOfTransactions: ").append(toIndentedString(batchNumberOfTransactions)).append("\n");
    sb.append("    mandateId: ").append(toIndentedString(mandateId)).append("\n");
    sb.append("    checkId: ").append(toIndentedString(checkId)).append("\n");
    sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
    sb.append("    bookingDate: ").append(toIndentedString(bookingDate)).append("\n");
    sb.append("    valueDate: ").append(toIndentedString(valueDate)).append("\n");
    sb.append("    transactionAmount: ").append(toIndentedString(transactionAmount)).append("\n");
    sb.append("    currencyExchange: ").append(toIndentedString(currencyExchange)).append("\n");
    sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
    sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
    sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
    sb.append("    ultimateCreditor: ").append(toIndentedString(ultimateCreditor)).append("\n");
    sb.append("    debtorName: ").append(toIndentedString(debtorName)).append("\n");
    sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
    sb.append("    debtorAgent: ").append(toIndentedString(debtorAgent)).append("\n");
    sb.append("    ultimateDebtor: ").append(toIndentedString(ultimateDebtor)).append("\n");
    sb.append("    remittanceInformationUnstructured: ").append(toIndentedString(remittanceInformationUnstructured)).append("\n");
    sb.append("    remittanceInformationUnstructuredArray: ").append(toIndentedString(remittanceInformationUnstructuredArray)).append("\n");
    sb.append("    remittanceInformationStructured: ").append(toIndentedString(remittanceInformationStructured)).append("\n");
    sb.append("    remittanceInformationStructuredArray: ").append(toIndentedString(remittanceInformationStructuredArray)).append("\n");
    sb.append("    entryDetails: ").append(toIndentedString(entryDetails)).append("\n");
    sb.append("    additionalInformation: ").append(toIndentedString(additionalInformation)).append("\n");
    sb.append("    additionalInformationStructured: ").append(toIndentedString(additionalInformationStructured)).append("\n");
    sb.append("    purposeCode: ").append(toIndentedString(purposeCode)).append("\n");
    sb.append("    bankTransactionCode: ").append(toIndentedString(bankTransactionCode)).append("\n");
    sb.append("    proprietaryBankTransactionCode: ").append(toIndentedString(proprietaryBankTransactionCode)).append("\n");
    sb.append("    balanceAfterTransaction: ").append(toIndentedString(balanceAfterTransaction)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
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

