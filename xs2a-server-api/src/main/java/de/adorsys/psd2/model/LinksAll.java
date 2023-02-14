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
import java.util.HashMap;
import java.util.Objects;

/**
 * A _link object with all available link types.
 */
@Schema(description = "A _link object with all available link types. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T13:00:42.214155+03:00[Europe/Kiev]")


public class LinksAll extends HashMap<String, HrefType>  {
  @JsonProperty("scaRedirect")
  private HrefType scaRedirect = null;

  @JsonProperty("scaOAuth")
  private HrefType scaOAuth = null;

  @JsonProperty("confirmation")
  private HrefType confirmation = null;

  @JsonProperty("startAuthorisation")
  private HrefType startAuthorisation = null;

  @JsonProperty("startAuthorisationWithPsuIdentification")
  private HrefType startAuthorisationWithPsuIdentification = null;

  @JsonProperty("updatePsuIdentification")
  private HrefType updatePsuIdentification = null;

  @JsonProperty("startAuthorisationWithProprietaryData")
  private HrefType startAuthorisationWithProprietaryData = null;

  @JsonProperty("updateProprietaryData")
  private HrefType updateProprietaryData = null;

  @JsonProperty("startAuthorisationWithPsuAuthentication")
  private HrefType startAuthorisationWithPsuAuthentication = null;

  @JsonProperty("updatePsuAuthentication")
  private HrefType updatePsuAuthentication = null;

  @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
  private HrefType startAuthorisationWithEncryptedPsuAuthentication = null;

  @JsonProperty("updateEncryptedPsuAuthentication")
  private HrefType updateEncryptedPsuAuthentication = null;

  @JsonProperty("updateAdditionalPsuAuthentication")
  private HrefType updateAdditionalPsuAuthentication = null;

  @JsonProperty("updateAdditionalEncryptedPsuAuthentication")
  private HrefType updateAdditionalEncryptedPsuAuthentication = null;

  @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
  private HrefType startAuthorisationWithAuthenticationMethodSelection = null;

  @JsonProperty("selectAuthenticationMethod")
  private HrefType selectAuthenticationMethod = null;

  @JsonProperty("startAuthorisationWithTransactionAuthorisation")
  private HrefType startAuthorisationWithTransactionAuthorisation = null;

  @JsonProperty("authoriseTransaction")
  private HrefType authoriseTransaction = null;

  @JsonProperty("self")
  private HrefType self = null;

  @JsonProperty("status")
  private HrefType status = null;

  @JsonProperty("scaStatus")
  private HrefType scaStatus = null;

  @JsonProperty("account")
  private HrefType account = null;

  @JsonProperty("balances")
  private HrefType balances = null;

  @JsonProperty("transactions")
  private HrefType transactions = null;

  @JsonProperty("transactionDetails")
  private HrefType transactionDetails = null;

  @JsonProperty("cardAccount")
  private HrefType cardAccount = null;

  @JsonProperty("cardTransactions")
  private HrefType cardTransactions = null;

  @JsonProperty("first")
  private HrefType first = null;

  @JsonProperty("next")
  private HrefType next = null;

  @JsonProperty("previous")
  private HrefType previous = null;

  @JsonProperty("last")
  private HrefType last = null;

  @JsonProperty("download")
  private HrefType download = null;

  public LinksAll scaRedirect(HrefType scaRedirect) {
    this.scaRedirect = scaRedirect;
    return this;
  }

  /**
   * Get scaRedirect
   * @return scaRedirect
   **/
  @Schema(description = "")

    @Valid
    public HrefType getScaRedirect() {
    return scaRedirect;
  }

  public void setScaRedirect(HrefType scaRedirect) {
    this.scaRedirect = scaRedirect;
  }

  public LinksAll scaOAuth(HrefType scaOAuth) {
    this.scaOAuth = scaOAuth;
    return this;
  }

  /**
   * Get scaOAuth
   * @return scaOAuth
   **/
  @Schema(description = "")

    @Valid
    public HrefType getScaOAuth() {
    return scaOAuth;
  }

  public void setScaOAuth(HrefType scaOAuth) {
    this.scaOAuth = scaOAuth;
  }

  public LinksAll confirmation(HrefType confirmation) {
    this.confirmation = confirmation;
    return this;
  }

  /**
   * Get confirmation
   * @return confirmation
   **/
  @Schema(description = "")

    @Valid
    public HrefType getConfirmation() {
    return confirmation;
  }

  public void setConfirmation(HrefType confirmation) {
    this.confirmation = confirmation;
  }

  public LinksAll startAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisation
   * @return startAuthorisation
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisation() {
    return startAuthorisation;
  }

  public void setStartAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
  }

  public LinksAll startAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuIdentification
   * @return startAuthorisationWithPsuIdentification
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithPsuIdentification() {
    return startAuthorisationWithPsuIdentification;
  }

  public void setStartAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
  }

  public LinksAll updatePsuIdentification(HrefType updatePsuIdentification) {
    this.updatePsuIdentification = updatePsuIdentification;
    return this;
  }

  /**
   * Get updatePsuIdentification
   * @return updatePsuIdentification
   **/
  @Schema(description = "")

    @Valid
    public HrefType getUpdatePsuIdentification() {
    return updatePsuIdentification;
  }

  public void setUpdatePsuIdentification(HrefType updatePsuIdentification) {
    this.updatePsuIdentification = updatePsuIdentification;
  }

  public LinksAll startAuthorisationWithProprietaryData(HrefType startAuthorisationWithProprietaryData) {
    this.startAuthorisationWithProprietaryData = startAuthorisationWithProprietaryData;
    return this;
  }

  /**
   * Get startAuthorisationWithProprietaryData
   * @return startAuthorisationWithProprietaryData
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithProprietaryData() {
    return startAuthorisationWithProprietaryData;
  }

  public void setStartAuthorisationWithProprietaryData(HrefType startAuthorisationWithProprietaryData) {
    this.startAuthorisationWithProprietaryData = startAuthorisationWithProprietaryData;
  }

  public LinksAll updateProprietaryData(HrefType updateProprietaryData) {
    this.updateProprietaryData = updateProprietaryData;
    return this;
  }

  /**
   * Get updateProprietaryData
   * @return updateProprietaryData
   **/
  @Schema(description = "")

    @Valid
    public HrefType getUpdateProprietaryData() {
    return updateProprietaryData;
  }

  public void setUpdateProprietaryData(HrefType updateProprietaryData) {
    this.updateProprietaryData = updateProprietaryData;
  }

  public LinksAll startAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuAuthentication
   * @return startAuthorisationWithPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithPsuAuthentication() {
    return startAuthorisationWithPsuAuthentication;
  }

  public void setStartAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
  }

  public LinksAll updatePsuAuthentication(HrefType updatePsuAuthentication) {
    this.updatePsuAuthentication = updatePsuAuthentication;
    return this;
  }

  /**
   * Get updatePsuAuthentication
   * @return updatePsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getUpdatePsuAuthentication() {
    return updatePsuAuthentication;
  }

  public void setUpdatePsuAuthentication(HrefType updatePsuAuthentication) {
    this.updatePsuAuthentication = updatePsuAuthentication;
  }

  public LinksAll startAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithEncryptedPsuAuthentication
   * @return startAuthorisationWithEncryptedPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithEncryptedPsuAuthentication() {
    return startAuthorisationWithEncryptedPsuAuthentication;
  }

  public void setStartAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
  }

  public LinksAll updateEncryptedPsuAuthentication(HrefType updateEncryptedPsuAuthentication) {
    this.updateEncryptedPsuAuthentication = updateEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get updateEncryptedPsuAuthentication
   * @return updateEncryptedPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getUpdateEncryptedPsuAuthentication() {
    return updateEncryptedPsuAuthentication;
  }

  public void setUpdateEncryptedPsuAuthentication(HrefType updateEncryptedPsuAuthentication) {
    this.updateEncryptedPsuAuthentication = updateEncryptedPsuAuthentication;
  }

  public LinksAll updateAdditionalPsuAuthentication(HrefType updateAdditionalPsuAuthentication) {
    this.updateAdditionalPsuAuthentication = updateAdditionalPsuAuthentication;
    return this;
  }

  /**
   * Get updateAdditionalPsuAuthentication
   * @return updateAdditionalPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getUpdateAdditionalPsuAuthentication() {
    return updateAdditionalPsuAuthentication;
  }

  public void setUpdateAdditionalPsuAuthentication(HrefType updateAdditionalPsuAuthentication) {
    this.updateAdditionalPsuAuthentication = updateAdditionalPsuAuthentication;
  }

  public LinksAll updateAdditionalEncryptedPsuAuthentication(HrefType updateAdditionalEncryptedPsuAuthentication) {
    this.updateAdditionalEncryptedPsuAuthentication = updateAdditionalEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get updateAdditionalEncryptedPsuAuthentication
   * @return updateAdditionalEncryptedPsuAuthentication
   **/
  @Schema(description = "")

    @Valid
    public HrefType getUpdateAdditionalEncryptedPsuAuthentication() {
    return updateAdditionalEncryptedPsuAuthentication;
  }

  public void setUpdateAdditionalEncryptedPsuAuthentication(HrefType updateAdditionalEncryptedPsuAuthentication) {
    this.updateAdditionalEncryptedPsuAuthentication = updateAdditionalEncryptedPsuAuthentication;
  }

  public LinksAll startAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
    return this;
  }

  /**
   * Get startAuthorisationWithAuthenticationMethodSelection
   * @return startAuthorisationWithAuthenticationMethodSelection
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithAuthenticationMethodSelection() {
    return startAuthorisationWithAuthenticationMethodSelection;
  }

  public void setStartAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
  }

  public LinksAll selectAuthenticationMethod(HrefType selectAuthenticationMethod) {
    this.selectAuthenticationMethod = selectAuthenticationMethod;
    return this;
  }

  /**
   * Get selectAuthenticationMethod
   * @return selectAuthenticationMethod
   **/
  @Schema(description = "")

    @Valid
    public HrefType getSelectAuthenticationMethod() {
    return selectAuthenticationMethod;
  }

  public void setSelectAuthenticationMethod(HrefType selectAuthenticationMethod) {
    this.selectAuthenticationMethod = selectAuthenticationMethod;
  }

  public LinksAll startAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisationWithTransactionAuthorisation
   * @return startAuthorisationWithTransactionAuthorisation
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStartAuthorisationWithTransactionAuthorisation() {
    return startAuthorisationWithTransactionAuthorisation;
  }

  public void setStartAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
  }

  public LinksAll authoriseTransaction(HrefType authoriseTransaction) {
    this.authoriseTransaction = authoriseTransaction;
    return this;
  }

  /**
   * Get authoriseTransaction
   * @return authoriseTransaction
   **/
  @Schema(description = "")

    @Valid
    public HrefType getAuthoriseTransaction() {
    return authoriseTransaction;
  }

  public void setAuthoriseTransaction(HrefType authoriseTransaction) {
    this.authoriseTransaction = authoriseTransaction;
  }

  public LinksAll self(HrefType self) {
    this.self = self;
    return this;
  }

  /**
   * Get self
   * @return self
   **/
  @Schema(description = "")

    @Valid
    public HrefType getSelf() {
    return self;
  }

  public void setSelf(HrefType self) {
    this.self = self;
  }

  public LinksAll status(HrefType status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
   **/
  @Schema(description = "")

    @Valid
    public HrefType getStatus() {
    return status;
  }

  public void setStatus(HrefType status) {
    this.status = status;
  }

  public LinksAll scaStatus(HrefType scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
   **/
  @Schema(description = "")

    @Valid
    public HrefType getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(HrefType scaStatus) {
    this.scaStatus = scaStatus;
  }

  public LinksAll account(HrefType account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
   **/
  @Schema(description = "")

    @Valid
    public HrefType getAccount() {
    return account;
  }

  public void setAccount(HrefType account) {
    this.account = account;
  }

  public LinksAll balances(HrefType balances) {
    this.balances = balances;
    return this;
  }

  /**
   * Get balances
   * @return balances
   **/
  @Schema(description = "")

    @Valid
    public HrefType getBalances() {
    return balances;
  }

  public void setBalances(HrefType balances) {
    this.balances = balances;
  }

  public LinksAll transactions(HrefType transactions) {
    this.transactions = transactions;
    return this;
  }

  /**
   * Get transactions
   * @return transactions
   **/
  @Schema(description = "")

    @Valid
    public HrefType getTransactions() {
    return transactions;
  }

  public void setTransactions(HrefType transactions) {
    this.transactions = transactions;
  }

  public LinksAll transactionDetails(HrefType transactionDetails) {
    this.transactionDetails = transactionDetails;
    return this;
  }

  /**
   * Get transactionDetails
   * @return transactionDetails
   **/
  @Schema(description = "")

    @Valid
    public HrefType getTransactionDetails() {
    return transactionDetails;
  }

  public void setTransactionDetails(HrefType transactionDetails) {
    this.transactionDetails = transactionDetails;
  }

  public LinksAll cardAccount(HrefType cardAccount) {
    this.cardAccount = cardAccount;
    return this;
  }

  /**
   * Get cardAccount
   * @return cardAccount
   **/
  @Schema(description = "")

    @Valid
    public HrefType getCardAccount() {
    return cardAccount;
  }

  public void setCardAccount(HrefType cardAccount) {
    this.cardAccount = cardAccount;
  }

  public LinksAll cardTransactions(HrefType cardTransactions) {
    this.cardTransactions = cardTransactions;
    return this;
  }

  /**
   * Get cardTransactions
   * @return cardTransactions
   **/
  @Schema(description = "")

    @Valid
    public HrefType getCardTransactions() {
    return cardTransactions;
  }

  public void setCardTransactions(HrefType cardTransactions) {
    this.cardTransactions = cardTransactions;
  }

  public LinksAll first(HrefType first) {
    this.first = first;
    return this;
  }

  /**
   * Get first
   * @return first
   **/
  @Schema(description = "")

    @Valid
    public HrefType getFirst() {
    return first;
  }

  public void setFirst(HrefType first) {
    this.first = first;
  }

  public LinksAll next(HrefType next) {
    this.next = next;
    return this;
  }

  /**
   * Get next
   * @return next
   **/
  @Schema(description = "")

    @Valid
    public HrefType getNext() {
    return next;
  }

  public void setNext(HrefType next) {
    this.next = next;
  }

  public LinksAll previous(HrefType previous) {
    this.previous = previous;
    return this;
  }

  /**
   * Get previous
   * @return previous
   **/
  @Schema(description = "")

    @Valid
    public HrefType getPrevious() {
    return previous;
  }

  public void setPrevious(HrefType previous) {
    this.previous = previous;
  }

  public LinksAll last(HrefType last) {
    this.last = last;
    return this;
  }

  /**
   * Get last
   * @return last
   **/
  @Schema(description = "")

    @Valid
    public HrefType getLast() {
    return last;
  }

  public void setLast(HrefType last) {
    this.last = last;
  }

  public LinksAll download(HrefType download) {
    this.download = download;
    return this;
  }

  /**
   * Get download
   * @return download
   **/
  @Schema(description = "")

    @Valid
    public HrefType getDownload() {
    return download;
  }

  public void setDownload(HrefType download) {
    this.download = download;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LinksAll _linksAll = (LinksAll) o;
    return Objects.equals(this.scaRedirect, _linksAll.scaRedirect) &&
        Objects.equals(this.scaOAuth, _linksAll.scaOAuth) &&
        Objects.equals(this.confirmation, _linksAll.confirmation) &&
        Objects.equals(this.startAuthorisation, _linksAll.startAuthorisation) &&
        Objects.equals(this.startAuthorisationWithPsuIdentification, _linksAll.startAuthorisationWithPsuIdentification) &&
        Objects.equals(this.updatePsuIdentification, _linksAll.updatePsuIdentification) &&
        Objects.equals(this.startAuthorisationWithProprietaryData, _linksAll.startAuthorisationWithProprietaryData) &&
        Objects.equals(this.updateProprietaryData, _linksAll.updateProprietaryData) &&
        Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksAll.startAuthorisationWithPsuAuthentication) &&
        Objects.equals(this.updatePsuAuthentication, _linksAll.updatePsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, _linksAll.startAuthorisationWithEncryptedPsuAuthentication) &&
        Objects.equals(this.updateEncryptedPsuAuthentication, _linksAll.updateEncryptedPsuAuthentication) &&
        Objects.equals(this.updateAdditionalPsuAuthentication, _linksAll.updateAdditionalPsuAuthentication) &&
        Objects.equals(this.updateAdditionalEncryptedPsuAuthentication, _linksAll.updateAdditionalEncryptedPsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, _linksAll.startAuthorisationWithAuthenticationMethodSelection) &&
        Objects.equals(this.selectAuthenticationMethod, _linksAll.selectAuthenticationMethod) &&
        Objects.equals(this.startAuthorisationWithTransactionAuthorisation, _linksAll.startAuthorisationWithTransactionAuthorisation) &&
        Objects.equals(this.authoriseTransaction, _linksAll.authoriseTransaction) &&
        Objects.equals(this.self, _linksAll.self) &&
        Objects.equals(this.status, _linksAll.status) &&
        Objects.equals(this.scaStatus, _linksAll.scaStatus) &&
        Objects.equals(this.account, _linksAll.account) &&
        Objects.equals(this.balances, _linksAll.balances) &&
        Objects.equals(this.transactions, _linksAll.transactions) &&
        Objects.equals(this.transactionDetails, _linksAll.transactionDetails) &&
        Objects.equals(this.cardAccount, _linksAll.cardAccount) &&
        Objects.equals(this.cardTransactions, _linksAll.cardTransactions) &&
        Objects.equals(this.first, _linksAll.first) &&
        Objects.equals(this.next, _linksAll.next) &&
        Objects.equals(this.previous, _linksAll.previous) &&
        Objects.equals(this.last, _linksAll.last) &&
        Objects.equals(this.download, _linksAll.download) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaRedirect, scaOAuth, confirmation, startAuthorisation, startAuthorisationWithPsuIdentification, updatePsuIdentification, startAuthorisationWithProprietaryData, updateProprietaryData, startAuthorisationWithPsuAuthentication, updatePsuAuthentication, startAuthorisationWithEncryptedPsuAuthentication, updateEncryptedPsuAuthentication, updateAdditionalPsuAuthentication, updateAdditionalEncryptedPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection, selectAuthenticationMethod, startAuthorisationWithTransactionAuthorisation, authoriseTransaction, self, status, scaStatus, account, balances, transactions, transactionDetails, cardAccount, cardTransactions, first, next, previous, last, download, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksAll {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
    sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
    sb.append("    confirmation: ").append(toIndentedString(confirmation)).append("\n");
    sb.append("    startAuthorisation: ").append(toIndentedString(startAuthorisation)).append("\n");
    sb.append("    startAuthorisationWithPsuIdentification: ").append(toIndentedString(startAuthorisationWithPsuIdentification)).append("\n");
    sb.append("    updatePsuIdentification: ").append(toIndentedString(updatePsuIdentification)).append("\n");
    sb.append("    startAuthorisationWithProprietaryData: ").append(toIndentedString(startAuthorisationWithProprietaryData)).append("\n");
    sb.append("    updateProprietaryData: ").append(toIndentedString(updateProprietaryData)).append("\n");
    sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
    sb.append("    updatePsuAuthentication: ").append(toIndentedString(updatePsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithEncryptedPsuAuthentication: ").append(toIndentedString(startAuthorisationWithEncryptedPsuAuthentication)).append("\n");
    sb.append("    updateEncryptedPsuAuthentication: ").append(toIndentedString(updateEncryptedPsuAuthentication)).append("\n");
    sb.append("    updateAdditionalPsuAuthentication: ").append(toIndentedString(updateAdditionalPsuAuthentication)).append("\n");
    sb.append("    updateAdditionalEncryptedPsuAuthentication: ").append(toIndentedString(updateAdditionalEncryptedPsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithAuthenticationMethodSelection: ").append(toIndentedString(startAuthorisationWithAuthenticationMethodSelection)).append("\n");
    sb.append("    selectAuthenticationMethod: ").append(toIndentedString(selectAuthenticationMethod)).append("\n");
    sb.append("    startAuthorisationWithTransactionAuthorisation: ").append(toIndentedString(startAuthorisationWithTransactionAuthorisation)).append("\n");
    sb.append("    authoriseTransaction: ").append(toIndentedString(authoriseTransaction)).append("\n");
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
    sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
    sb.append("    transactionDetails: ").append(toIndentedString(transactionDetails)).append("\n");
    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
    sb.append("    cardTransactions: ").append(toIndentedString(cardTransactions)).append("\n");
    sb.append("    first: ").append(toIndentedString(first)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("    last: ").append(toIndentedString(last)).append("\n");
    sb.append("    download: ").append(toIndentedString(download)).append("\n");
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
