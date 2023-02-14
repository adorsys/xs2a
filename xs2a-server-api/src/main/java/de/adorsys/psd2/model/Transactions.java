/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
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
import java.util.Map;
import java.util.Objects;

/**
 * Transaction details.
 */
@Schema(description = "Transaction details.")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


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
  private RemittanceInformationStructuredMax140 remittanceInformationStructured = null;

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
     * This identification is given by the attribute transactionId of the corresponding entry of a transaction list.
     *
     * @return transactionId
     **/
    @Schema(example = "3dc3d5b3-7023-4848-9853-f5400a64e80f", description = "This identification is given by the attribute transactionId of the corresponding entry of a transaction list. ")
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
     * Is the identification of the transaction as used e.g. for reference for deltafunction on application level. The same identification as for example used within camt.05x messages.
     *
     * @return entryReference
     **/
    @Schema(description = "Is the identification of the transaction as used e.g. for reference for deltafunction on application level. The same identification as for example used within camt.05x messages. ")
    @JsonProperty("entryReference")

    @Size(max = 35)
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
     *
     * @return endToEndId
     **/
    @Schema(description = "Unique end to end identity.")
    @JsonProperty("endToEndId")

    @Size(max = 35)
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
     *
     * @return batchIndicator
     **/
    @Schema(description = "If this indicator equals true, then the related entry is a batch entry. ")
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
     *
     * @return batchNumberOfTransactions
     **/
    @Schema(description = "Shall be used if and only if the batchIndicator is contained and equals true. ")
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
     *
     * @return mandateId
     **/
    @Schema(description = "Identification of Mandates, e.g. a SEPA Mandate ID.")
    @JsonProperty("mandateId")

    @Size(max=35)   public String getMandateId() {
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
     *
     * @return checkId
     **/
    @Schema(description = "Identification of a Cheque.")
    @JsonProperty("checkId")

@Size(max=35)   public String getCheckId() {
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
     * Identification of Creditors, e.g. a SEPA Creditor ID.
     *
     * @return creditorId
     **/
    @Schema(example = "Creditor Id 5678", description = "Identification of Creditors, e.g. a SEPA Creditor ID.")
    @JsonProperty("creditorId")

@Size(max=35)   public String getCreditorId() {
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
     * The date when an entry is posted to an account on the ASPSPs books.
     *
     * @return bookingDate
     **/
    @Schema(description = "The date when an entry is posted to an account on the ASPSPs books. ")
    @JsonProperty("bookingDate")

  @Valid
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
     *
     * @return valueDate
     **/
    @Schema(description = "The Date at which assets become available to the account owner in case of a credit, or cease to be available to the account owner in case of a debit entry. **Usage:** If entry status is pending and value date is present, then the value date refers to an expected/requested value date.")
  @JsonProperty("valueDate")

  @Valid
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
     *
     * @return transactionAmount
     **/
    @Schema(required = true, description = "")
    @JsonProperty("transactionAmount")
    @NotNull

  @Valid
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
     *
     * @return currencyExchange
     **/
    @Schema(description = "")
    @JsonProperty("currencyExchange")

    @Valid
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
     * Creditor name.
     *
     * @return creditorName
     **/
    @Schema(example = "Creditor Name", description = "Creditor name.")
  @JsonProperty("creditorName")

@Size(max=70)   public String getCreditorName() {
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
  @Schema(description = "")
  @JsonProperty("creditorAccount")

  @Valid
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
     * BICFI
     *
     * @return creditorAgent
     **/
    @Schema(example = "AAAADEBBXXX", description = "BICFI ")
    @JsonProperty("creditorAgent")

    @Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")   public String getCreditorAgent() {
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

    public Transactions debtorName(String debtorName) {
        this.debtorName = debtorName;
        return this;
    }

    /**
     * Debtor name.
     * @return debtorName
   **/
  @Schema(example = "Debtor Name", description = "Debtor name.")
  @JsonProperty("debtorName")

@Size(max=70)   public String getDebtorName() {
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
  @Schema(description = "")
  @JsonProperty("debtorAccount")

  @Valid
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
     * BICFI
     *
     * @return debtorAgent
     **/
    @Schema(example = "AAAADEBBXXX", description = "BICFI ")
    @JsonProperty("debtorAgent")

    @Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")   public String getDebtorAgent() {
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
     * Ultimate debtor.
     * @return ultimateDebtor
   **/
  @Schema(example = "Ultimate Debtor", description = "Ultimate debtor.")
  @JsonProperty("ultimateDebtor")

@Size(max=70)   public String getUltimateDebtor() {
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

  public Transactions remittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
      this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
      return this;
  }

    /**
     * Get remittanceInformationUnstructuredArray
     *
     * @return remittanceInformationUnstructuredArray
     **/
    @Schema(description = "")
  @JsonProperty("remittanceInformationUnstructuredArray")

  @Valid
  public RemittanceInformationUnstructuredArray getRemittanceInformationUnstructuredArray() {
    return remittanceInformationUnstructuredArray;
  }

  public void setRemittanceInformationUnstructuredArray(RemittanceInformationUnstructuredArray remittanceInformationUnstructuredArray) {
    this.remittanceInformationUnstructuredArray = remittanceInformationUnstructuredArray;
  }

    public Transactions remittanceInformationStructured(RemittanceInformationStructuredMax140 remittanceInformationStructured) {
        this.remittanceInformationStructured = remittanceInformationStructured;
        return this;
    }

    /**
     * Get remittanceInformationStructured
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

  public Transactions remittanceInformationStructuredArray(RemittanceInformationStructuredArray remittanceInformationStructuredArray) {
      this.remittanceInformationStructuredArray = remittanceInformationStructuredArray;
      return this;
  }

    /**
     * Get remittanceInformationStructuredArray
     *
     * @return remittanceInformationStructuredArray
     **/
    @Schema(description = "")
  @JsonProperty("remittanceInformationStructuredArray")

  @Valid
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
  @Schema(description = "")
  @JsonProperty("entryDetails")

  @Valid
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
     * Might be used by the ASPSP to transport additional transaction related information to the PSU
     *
     * @return additionalInformation
     **/
    @Schema(example = "Some additional transaction related information.", description = "Might be used by the ASPSP to transport additional transaction related information to the PSU ")
  @JsonProperty("additionalInformation")

@Size(max=500)   public String getAdditionalInformation() {
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
  @Schema(description = "")
  @JsonProperty("additionalInformationStructured")

  @Valid
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
    @Schema(description = "")
    @JsonProperty("purposeCode")

    @Valid
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
     * Bank transaction code as used by the ASPSP and using the sub elements of this structured code defined by ISO 20022.   This code type is concatenating the three ISO20022 Codes    * Domain Code,    * Family Code, and    * SubFamiliy Code  by hyphens, resulting in 'DomainCode'-'FamilyCode'-'SubFamilyCode'. For standing order reports the following codes are applicable:   * \"PMNT-ICDT-STDO\" for credit transfers,   * \"PMNT-IRCT-STDO\"  for instant credit transfers   * \"PMNT-ICDT-XBST\" for cross-border credit transfers   * \"PMNT-IRCT-XBST\" for cross-border real time credit transfers and   * \"PMNT-MCOP-OTHR\" for specific standing orders which have a dynamical amount to move left funds e.g. on month end to a saving account
     *
     * @return bankTransactionCode
     **/
    @Schema(example = "PMNT-RDDT-ESDD", description = "Bank transaction code as used by the ASPSP and using the sub elements of this structured code defined by ISO 20022.   This code type is concatenating the three ISO20022 Codes    * Domain Code,    * Family Code, and    * SubFamiliy Code  by hyphens, resulting in 'DomainCode'-'FamilyCode'-'SubFamilyCode'. For standing order reports the following codes are applicable:   * \"PMNT-ICDT-STDO\" for credit transfers,   * \"PMNT-IRCT-STDO\"  for instant credit transfers   * \"PMNT-ICDT-XBST\" for cross-border credit transfers   * \"PMNT-IRCT-XBST\" for cross-border real time credit transfers and   * \"PMNT-MCOP-OTHR\" for specific standing orders which have a dynamical amount to move left funds e.g. on month end to a saving account ")
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
     * Proprietary bank transaction code as used within a community or within an ASPSP e.g.  for MT94x based transaction reports.
     *
     * @return proprietaryBankTransactionCode
     **/
    @Schema(description = "Proprietary bank transaction code as used within a community or within an ASPSP e.g.  for MT94x based transaction reports. ")
  @JsonProperty("proprietaryBankTransactionCode")

@Size(max=35)   public String getProprietaryBankTransactionCode() {
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
    @Schema(description = "")
    @JsonProperty("balanceAfterTransaction")

    @Valid
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
  @Schema(description = "")
  @JsonProperty("_links")

  @Valid
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
    }
    Transactions transactions = (Transactions) o;
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
