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

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * A _link object with all availabel link types 
 */
@ApiModel(description = "A _link object with all availabel link types ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2019-01-11T12:48:04.675377+02:00[Europe/Kiev]")

public class LinksAll extends HashMap<String, String>  {
  @JsonProperty("scaRedirect")
  private String scaRedirect = null;

  @JsonProperty("scaOAuth")
  private String scaOAuth = null;

  @JsonProperty("startAuthorisation")
  private String startAuthorisation = null;

  @JsonProperty("startAuthorisationWithPsuIdentification")
  private String startAuthorisationWithPsuIdentification = null;

  @JsonProperty("updatePsuIdentification")
  private String updatePsuIdentification = null;

  @JsonProperty("startAuthorisationWithProprietaryData")
  private String startAuthorisationWithProprietaryData = null;

  @JsonProperty("updateProprietaryData")
  private String updateProprietaryData = null;

  @JsonProperty("startAuthorisationWithPsuAuthentication")
  private String startAuthorisationWithPsuAuthentication = null;

  @JsonProperty("updatePsuAuthentication")
  private String updatePsuAuthentication = null;

  @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
  private String startAuthorisationWithEncryptedPsuAuthentication = null;

  @JsonProperty("updateEncryptedPsuAuthentication")
  private String updateEncryptedPsuAuthentication = null;

  @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
  private String startAuthorisationWithAuthenticationMethodSelection = null;

  @JsonProperty("selectAuthenticationMethod")
  private String selectAuthenticationMethod = null;

  @JsonProperty("startAuthorisationWithTransactionAuthorisation")
  private String startAuthorisationWithTransactionAuthorisation = null;

  @JsonProperty("authoriseTransaction")
  private String authoriseTransaction = null;

  @JsonProperty("self")
  private String self = null;

  @JsonProperty("status")
  private String status = null;

  @JsonProperty("scaStatus")
  private String scaStatus = null;

  @JsonProperty("account")
  private String account = null;

  @JsonProperty("balances")
  private String balances = null;

  @JsonProperty("transactions")
  private String transactions = null;

  @JsonProperty("transactionDetails")
  private String transactionDetails = null;

  @JsonProperty("cardAccount")
  private String cardAccount = null;

  @JsonProperty("cardTransactions")
  private String cardTransactions = null;

  @JsonProperty("first")
  private String first = null;

  @JsonProperty("next")
  private String next = null;

  @JsonProperty("previous")
  private String previous = null;

  @JsonProperty("last")
  private String last = null;

  @JsonProperty("download")
  private String download = null;

  public LinksAll scaRedirect(String scaRedirect) {
    this.scaRedirect = scaRedirect;
    return this;
  }

  /**
   * Get scaRedirect
   * @return scaRedirect
  **/
  @ApiModelProperty(value = "")


  public String getScaRedirect() {
    return scaRedirect;
  }

  public void setScaRedirect(String scaRedirect) {
    this.scaRedirect = scaRedirect;
  }

  public LinksAll scaOAuth(String scaOAuth) {
    this.scaOAuth = scaOAuth;
    return this;
  }

  /**
   * Get scaOAuth
   * @return scaOAuth
  **/
  @ApiModelProperty(value = "")


  public String getScaOAuth() {
    return scaOAuth;
  }

  public void setScaOAuth(String scaOAuth) {
    this.scaOAuth = scaOAuth;
  }

  public LinksAll startAuthorisation(String startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisation
   * @return startAuthorisation
  **/
  @ApiModelProperty(value = "")


  public String getStartAuthorisation() {
    return startAuthorisation;
  }

  public void setStartAuthorisation(String startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
  }

  public LinksAll startAuthorisationWithPsuIdentification(String startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuIdentification
   * @return startAuthorisationWithPsuIdentification
  **/
  @ApiModelProperty(value = "")


  public String getStartAuthorisationWithPsuIdentification() {
    return startAuthorisationWithPsuIdentification;
  }

  public void setStartAuthorisationWithPsuIdentification(String startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
  }

  public LinksAll updatePsuIdentification(String updatePsuIdentification) {
    this.updatePsuIdentification = updatePsuIdentification;
    return this;
  }

  /**
   * Get updatePsuIdentification
   * @return updatePsuIdentification
  **/
  @ApiModelProperty(value = "")


  public String getUpdatePsuIdentification() {
    return updatePsuIdentification;
  }

  public void setUpdatePsuIdentification(String updatePsuIdentification) {
    this.updatePsuIdentification = updatePsuIdentification;
  }

  public LinksAll startAuthorisationWithProprietaryData(String startAuthorisationWithProprietaryData) {
    this.startAuthorisationWithProprietaryData = startAuthorisationWithProprietaryData;
    return this;
  }

  /**
   * Get startAuthorisationWithProprietaryData
   * @return startAuthorisationWithProprietaryData
  **/
  @ApiModelProperty(value = "")


  public String getStartAuthorisationWithProprietaryData() {
    return startAuthorisationWithProprietaryData;
  }

  public void setStartAuthorisationWithProprietaryData(String startAuthorisationWithProprietaryData) {
    this.startAuthorisationWithProprietaryData = startAuthorisationWithProprietaryData;
  }

  public LinksAll updateProprietaryData(String updateProprietaryData) {
    this.updateProprietaryData = updateProprietaryData;
    return this;
  }

  /**
   * Get updateProprietaryData
   * @return updateProprietaryData
  **/
  @ApiModelProperty(value = "")


  public String getUpdateProprietaryData() {
    return updateProprietaryData;
  }

  public void setUpdateProprietaryData(String updateProprietaryData) {
    this.updateProprietaryData = updateProprietaryData;
  }

  public LinksAll startAuthorisationWithPsuAuthentication(String startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithPsuAuthentication
   * @return startAuthorisationWithPsuAuthentication
  **/
  @ApiModelProperty(value = "")


  public String getStartAuthorisationWithPsuAuthentication() {
    return startAuthorisationWithPsuAuthentication;
  }

  public void setStartAuthorisationWithPsuAuthentication(String startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
  }

  public LinksAll updatePsuAuthentication(String updatePsuAuthentication) {
    this.updatePsuAuthentication = updatePsuAuthentication;
    return this;
  }

  /**
   * Get updatePsuAuthentication
   * @return updatePsuAuthentication
  **/
  @ApiModelProperty(value = "")


  public String getUpdatePsuAuthentication() {
    return updatePsuAuthentication;
  }

  public void setUpdatePsuAuthentication(String updatePsuAuthentication) {
    this.updatePsuAuthentication = updatePsuAuthentication;
  }

  public LinksAll startAuthorisationWithEncryptedPsuAuthentication(String startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get startAuthorisationWithEncryptedPsuAuthentication
   * @return startAuthorisationWithEncryptedPsuAuthentication
  **/
  @ApiModelProperty(value = "")


  public String getStartAuthorisationWithEncryptedPsuAuthentication() {
    return startAuthorisationWithEncryptedPsuAuthentication;
  }

  public void setStartAuthorisationWithEncryptedPsuAuthentication(String startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
  }

  public LinksAll updateEncryptedPsuAuthentication(String updateEncryptedPsuAuthentication) {
    this.updateEncryptedPsuAuthentication = updateEncryptedPsuAuthentication;
    return this;
  }

  /**
   * Get updateEncryptedPsuAuthentication
   * @return updateEncryptedPsuAuthentication
  **/
  @ApiModelProperty(value = "")


  public String getUpdateEncryptedPsuAuthentication() {
    return updateEncryptedPsuAuthentication;
  }

  public void setUpdateEncryptedPsuAuthentication(String updateEncryptedPsuAuthentication) {
    this.updateEncryptedPsuAuthentication = updateEncryptedPsuAuthentication;
  }

  public LinksAll startAuthorisationWithAuthenticationMethodSelection(String startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
    return this;
  }

  /**
   * Get startAuthorisationWithAuthenticationMethodSelection
   * @return startAuthorisationWithAuthenticationMethodSelection
  **/
  @ApiModelProperty(value = "")


  public String getStartAuthorisationWithAuthenticationMethodSelection() {
    return startAuthorisationWithAuthenticationMethodSelection;
  }

  public void setStartAuthorisationWithAuthenticationMethodSelection(String startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
  }

  public LinksAll selectAuthenticationMethod(String selectAuthenticationMethod) {
    this.selectAuthenticationMethod = selectAuthenticationMethod;
    return this;
  }

  /**
   * Get selectAuthenticationMethod
   * @return selectAuthenticationMethod
  **/
  @ApiModelProperty(value = "")


  public String getSelectAuthenticationMethod() {
    return selectAuthenticationMethod;
  }

  public void setSelectAuthenticationMethod(String selectAuthenticationMethod) {
    this.selectAuthenticationMethod = selectAuthenticationMethod;
  }

  public LinksAll startAuthorisationWithTransactionAuthorisation(String startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
    return this;
  }

  /**
   * Get startAuthorisationWithTransactionAuthorisation
   * @return startAuthorisationWithTransactionAuthorisation
  **/
  @ApiModelProperty(value = "")


  public String getStartAuthorisationWithTransactionAuthorisation() {
    return startAuthorisationWithTransactionAuthorisation;
  }

  public void setStartAuthorisationWithTransactionAuthorisation(String startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
  }

  public LinksAll authoriseTransaction(String authoriseTransaction) {
    this.authoriseTransaction = authoriseTransaction;
    return this;
  }

  /**
   * Get authoriseTransaction
   * @return authoriseTransaction
  **/
  @ApiModelProperty(value = "")


  public String getAuthoriseTransaction() {
    return authoriseTransaction;
  }

  public void setAuthoriseTransaction(String authoriseTransaction) {
    this.authoriseTransaction = authoriseTransaction;
  }

  public LinksAll self(String self) {
    this.self = self;
    return this;
  }

  /**
   * Get self
   * @return self
  **/
  @ApiModelProperty(value = "")


  public String getSelf() {
    return self;
  }

  public void setSelf(String self) {
    this.self = self;
  }

  public LinksAll status(String status) {
    this.status = status;
    return this;
  }

  /**
   * Get status
   * @return status
  **/
  @ApiModelProperty(value = "")


  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public LinksAll scaStatus(String scaStatus) {
    this.scaStatus = scaStatus;
    return this;
  }

  /**
   * Get scaStatus
   * @return scaStatus
  **/
  @ApiModelProperty(value = "")


  public String getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(String scaStatus) {
    this.scaStatus = scaStatus;
  }

  public LinksAll account(String account) {
    this.account = account;
    return this;
  }

  /**
   * Get account
   * @return account
  **/
  @ApiModelProperty(value = "")


  public String getAccount() {
    return account;
  }

  public void setAccount(String account) {
    this.account = account;
  }

  public LinksAll balances(String balances) {
    this.balances = balances;
    return this;
  }

  /**
   * Get balances
   * @return balances
  **/
  @ApiModelProperty(value = "")


  public String getBalances() {
    return balances;
  }

  public void setBalances(String balances) {
    this.balances = balances;
  }

  public LinksAll transactions(String transactions) {
    this.transactions = transactions;
    return this;
  }

  /**
   * Get transactions
   * @return transactions
  **/
  @ApiModelProperty(value = "")


  public String getTransactions() {
    return transactions;
  }

  public void setTransactions(String transactions) {
    this.transactions = transactions;
  }

  public LinksAll transactionDetails(String transactionDetails) {
    this.transactionDetails = transactionDetails;
    return this;
  }

  /**
   * Get transactionDetails
   * @return transactionDetails
  **/
  @ApiModelProperty(value = "")


  public String getTransactionDetails() {
    return transactionDetails;
  }

  public void setTransactionDetails(String transactionDetails) {
    this.transactionDetails = transactionDetails;
  }

  public LinksAll cardAccount(String cardAccount) {
    this.cardAccount = cardAccount;
    return this;
  }

  /**
   * Get cardAccount
   * @return cardAccount
  **/
  @ApiModelProperty(value = "")


  public String getCardAccount() {
    return cardAccount;
  }

  public void setCardAccount(String cardAccount) {
    this.cardAccount = cardAccount;
  }

  public LinksAll cardTransactions(String cardTransactions) {
    this.cardTransactions = cardTransactions;
    return this;
  }

  /**
   * Get cardTransactions
   * @return cardTransactions
  **/
  @ApiModelProperty(value = "")


  public String getCardTransactions() {
    return cardTransactions;
  }

  public void setCardTransactions(String cardTransactions) {
    this.cardTransactions = cardTransactions;
  }

  public LinksAll first(String first) {
    this.first = first;
    return this;
  }

  /**
   * Get first
   * @return first
  **/
  @ApiModelProperty(value = "")


  public String getFirst() {
    return first;
  }

  public void setFirst(String first) {
    this.first = first;
  }

  public LinksAll next(String next) {
    this.next = next;
    return this;
  }

  /**
   * Get next
   * @return next
  **/
  @ApiModelProperty(value = "")


  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public LinksAll previous(String previous) {
    this.previous = previous;
    return this;
  }

  /**
   * Get previous
   * @return previous
  **/
  @ApiModelProperty(value = "")


  public String getPrevious() {
    return previous;
  }

  public void setPrevious(String previous) {
    this.previous = previous;
  }

  public LinksAll last(String last) {
    this.last = last;
    return this;
  }

  /**
   * Get last
   * @return last
  **/
  @ApiModelProperty(value = "")


  public String getLast() {
    return last;
  }

  public void setLast(String last) {
    this.last = last;
  }

  public LinksAll download(String download) {
    this.download = download;
    return this;
  }

  /**
   * Get download
   * @return download
  **/
  @ApiModelProperty(value = "")


  public String getDownload() {
    return download;
  }

  public void setDownload(String download) {
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
        Objects.equals(this.startAuthorisation, _linksAll.startAuthorisation) &&
        Objects.equals(this.startAuthorisationWithPsuIdentification, _linksAll.startAuthorisationWithPsuIdentification) &&
        Objects.equals(this.updatePsuIdentification, _linksAll.updatePsuIdentification) &&
        Objects.equals(this.startAuthorisationWithProprietaryData, _linksAll.startAuthorisationWithProprietaryData) &&
        Objects.equals(this.updateProprietaryData, _linksAll.updateProprietaryData) &&
        Objects.equals(this.startAuthorisationWithPsuAuthentication, _linksAll.startAuthorisationWithPsuAuthentication) &&
        Objects.equals(this.updatePsuAuthentication, _linksAll.updatePsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, _linksAll.startAuthorisationWithEncryptedPsuAuthentication) &&
        Objects.equals(this.updateEncryptedPsuAuthentication, _linksAll.updateEncryptedPsuAuthentication) &&
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
    return Objects.hash(scaRedirect, scaOAuth, startAuthorisation, startAuthorisationWithPsuIdentification, updatePsuIdentification, startAuthorisationWithProprietaryData, updateProprietaryData, startAuthorisationWithPsuAuthentication, updatePsuAuthentication, startAuthorisationWithEncryptedPsuAuthentication, updateEncryptedPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection, selectAuthenticationMethod, startAuthorisationWithTransactionAuthorisation, authoriseTransaction, self, status, scaStatus, account, balances, transactions, transactionDetails, cardAccount, cardTransactions, first, next, previous, last, download, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LinksAll {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
    sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
    sb.append("    startAuthorisation: ").append(toIndentedString(startAuthorisation)).append("\n");
    sb.append("    startAuthorisationWithPsuIdentification: ").append(toIndentedString(startAuthorisationWithPsuIdentification)).append("\n");
    sb.append("    updatePsuIdentification: ").append(toIndentedString(updatePsuIdentification)).append("\n");
    sb.append("    startAuthorisationWithProprietaryData: ").append(toIndentedString(startAuthorisationWithProprietaryData)).append("\n");
    sb.append("    updateProprietaryData: ").append(toIndentedString(updateProprietaryData)).append("\n");
    sb.append("    startAuthorisationWithPsuAuthentication: ").append(toIndentedString(startAuthorisationWithPsuAuthentication)).append("\n");
    sb.append("    updatePsuAuthentication: ").append(toIndentedString(updatePsuAuthentication)).append("\n");
    sb.append("    startAuthorisationWithEncryptedPsuAuthentication: ").append(toIndentedString(startAuthorisationWithEncryptedPsuAuthentication)).append("\n");
    sb.append("    updateEncryptedPsuAuthentication: ").append(toIndentedString(updateEncryptedPsuAuthentication)).append("\n");
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

