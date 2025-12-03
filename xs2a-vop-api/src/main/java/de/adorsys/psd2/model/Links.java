package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Links\\\&quot; for more details.
 */

@Schema(name = "links", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Links\\\" for more details. ")
@JsonTypeName("links")
@SuppressWarnings("PMD")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class Links {

  private Optional<HrefType> scaRedirect = Optional.empty();

  private Optional<HrefType> scaOAuth = Optional.empty();

  private Optional<HrefType> confirmation = Optional.empty();

  private Optional<HrefType> creditorNameConfirmation = Optional.empty();

  private Optional<HrefType> startAuthorisation = Optional.empty();

  private Optional<HrefType> startAuthorisationWithPsuIdentification = Optional.empty();

  private Optional<HrefType> updatePsuIdentification = Optional.empty();

  private Optional<HrefType> startAuthorisationWithProprietaryData = Optional.empty();

  private Optional<HrefType> updateProprietaryData = Optional.empty();

  private Optional<HrefType> startAuthorisationWithPsuAuthentication = Optional.empty();

  private Optional<HrefType> updatePsuAuthentication = Optional.empty();

  private Optional<HrefType> startAuthorisationWithEncryptedPsuAuthentication = Optional.empty();

  private Optional<HrefType> updateEncryptedPsuAuthentication = Optional.empty();

  private Optional<HrefType> startAuthorisationWithAuthenticationMethodSelection = Optional.empty();

  private Optional<HrefType> selectAuthenticationMethod = Optional.empty();

  private Optional<HrefType> startAuthorisationWithTransactionAuthorisation = Optional.empty();

  private Optional<HrefType> authoriseTransaction = Optional.empty();

  private Optional<HrefType> updateResourceByDebtorAccountResource = Optional.empty();

  private Optional<HrefType> self = Optional.empty();

  private Optional<HrefType> status = Optional.empty();

  private Optional<HrefType> transactionfees = Optional.empty();

  private Optional<HrefType> scaStatus = Optional.empty();

  private Optional<HrefType> account = Optional.empty();

  private Optional<HrefType> savingsAccount = Optional.empty();

  private Optional<HrefType> loanAccount = Optional.empty();

  private Optional<HrefType> balances = Optional.empty();

  private Optional<HrefType> transactions = Optional.empty();

  private Optional<HrefType> cardAccount = Optional.empty();

  private Optional<HrefType> cardTransactions = Optional.empty();

  private Optional<HrefType> transactionDetails = Optional.empty();

  private Optional<HrefType> ibanCheck = Optional.empty();

  private Optional<HrefType> paymentInitiation = Optional.empty();

  private Optional<HrefType> securitiesAccount = Optional.empty();

  private Optional<HrefType> positions = Optional.empty();

  private Optional<HrefType> orders = Optional.empty();

  private Optional<HrefType> orderDetails = Optional.empty();

  @Valid
  private List<@Valid HrefType> relatedOrders = new ArrayList<>();

  @Valid
  private List<@Valid HrefType> relatedTransactions = new ArrayList<>();

  private Optional<HrefType> subscription = Optional.empty();

  @Valid
  private List<@Valid HrefType> entryStatusRevoked = new ArrayList<>();

  private Optional<HrefType> first = Optional.empty();

  private Optional<HrefType> next = Optional.empty();

  private Optional<HrefType> previous = Optional.empty();

  private Optional<HrefType> last = Optional.empty();

  private Optional<HrefType> download = Optional.empty();

  private Optional<HrefType> confirmInitiation = Optional.empty();

  private Optional<HrefType> aspspParameters = Optional.empty();

  private Optional<HrefType> aspspContacts = Optional.empty();

  private Optional<HrefType> aspspDowntimes = Optional.empty();

  private Optional<HrefType> onboardings = Optional.empty();

  private Optional<HrefType> readConditions = Optional.empty();

  private Optional<HrefType> confirmConditions = Optional.empty();

  public Links scaRedirect(HrefType scaRedirect) {
    this.scaRedirect = Optional.ofNullable(scaRedirect);
    return this;
  }

  /**
   * A link to an ASPSP site where SCA is performed within the Redirect SCA approach.
   * @return scaRedirect
   */
  @Valid
  @Schema(name = "scaRedirect", description = "A link to an ASPSP site where SCA is performed within the Redirect SCA approach. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scaRedirect")
  public Optional<HrefType> getScaRedirect() {
    return scaRedirect;
  }

  public void setScaRedirect(Optional<HrefType> scaRedirect) {
    this.scaRedirect = scaRedirect;
  }

  public Links scaOAuth(HrefType scaOAuth) {
    this.scaOAuth = Optional.ofNullable(scaOAuth);
    return this;
  }

  /**
   * The link refers to a JSON document specifying the OAuth details of the ASPSP's authorisation server. JSON document follows the definition given in [RFC 8414].
   * @return scaOAuth
   */
  @Valid
  @Schema(name = "scaOAuth", description = "The link refers to a JSON document specifying the OAuth details of the ASPSP's authorisation server. JSON document follows the definition given in [RFC 8414]. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scaOAuth")
  public Optional<HrefType> getScaOAuth() {
    return scaOAuth;
  }

  public void setScaOAuth(Optional<HrefType> scaOAuth) {
    this.scaOAuth = scaOAuth;
  }

  public Links confirmation(HrefType confirmation) {
    this.confirmation = Optional.ofNullable(confirmation);
    return this;
  }

  /**
   * \\\"confirmation\\\": Might be added by the ASPSP if either the \\\"scaRedirect\\\" or \\\"scaOAuth\\\" hyperlink is returned in the same response message.  This hyperlink defines the URL to the resource which needs to be updated with  * a confirmation code as retrieved after the plain redirect authentication process with the ASPSP authentication server or * an access token as retrieved by submitting an authorization code after the integrated Oauth based authentication process with the ASPSP authentication server.
   * @return confirmation
   */
  @Valid
  @Schema(name = "confirmation", description = "\\\"confirmation\\\": Might be added by the ASPSP if either the \\\"scaRedirect\\\" or \\\"scaOAuth\\\" hyperlink is returned in the same response message.  This hyperlink defines the URL to the resource which needs to be updated with  * a confirmation code as retrieved after the plain redirect authentication process with the ASPSP authentication server or * an access token as retrieved by submitting an authorization code after the integrated Oauth based authentication process with the ASPSP authentication server. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("confirmation")
  public Optional<HrefType> getConfirmation() {
    return confirmation;
  }

  public void setConfirmation(Optional<HrefType> confirmation) {
    this.confirmation = confirmation;
  }

  public Links creditorNameConfirmation(HrefType creditorNameConfirmation) {
    this.creditorNameConfirmation = Optional.ofNullable(creditorNameConfirmation);
    return this;
  }

  /**
   * A link to an endpoint, where the submitted payment needs to be confirmed explicitly for execution after a verification of payee process has detected a deviation in the creditor account/creditor name pair. If this hyperlink is provided, the addressed payment will not be executed without a related confirmation.
   * @return creditorNameConfirmation
   */
  @Valid
  @Schema(name = "creditorNameConfirmation", description = "A link to an endpoint, where the submitted payment needs to be confirmed explicitly for execution after a verification of payee process has detected a deviation in the creditor account/creditor name pair. If this hyperlink is provided, the addressed payment will not be executed without a related confirmation. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("creditorNameConfirmation")
  public Optional<HrefType> getCreditorNameConfirmation() {
    return creditorNameConfirmation;
  }

  public void setCreditorNameConfirmation(Optional<HrefType> creditorNameConfirmation) {
    this.creditorNameConfirmation = creditorNameConfirmation;
  }

  public Links startAuthorisation(HrefType startAuthorisation) {
    this.startAuthorisation = Optional.ofNullable(startAuthorisation);
    return this;
  }

  /**
   * A link to an endpoint, where the authorisation of a transaction or the authorisation of a transaction cancellation shall be started with a POST command.  No specific data is needed for this process start.
   * @return startAuthorisation
   */
  @Valid
  @Schema(name = "startAuthorisation", description = "A link to an endpoint, where the authorisation of a transaction or the authorisation of a transaction cancellation shall be started with a POST command.  No specific data is needed for this process start. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisation")
  public Optional<HrefType> getStartAuthorisation() {
    return startAuthorisation;
  }

  public void setStartAuthorisation(Optional<HrefType> startAuthorisation) {
    this.startAuthorisation = startAuthorisation;
  }

  public Links startAuthorisationWithPsuIdentification(HrefType startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = Optional.ofNullable(startAuthorisationWithPsuIdentification);
    return this;
  }

  /**
   * The link to an endpoint where the authorisation of a transaction or of a transaction cancellation shall be started, where PSU identification shall be uploaded  with the corresponding call.
   * @return startAuthorisationWithPsuIdentification
   */
  @Valid
  @Schema(name = "startAuthorisationWithPsuIdentification", description = "The link to an endpoint where the authorisation of a transaction or of a transaction cancellation shall be started, where PSU identification shall be uploaded  with the corresponding call. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithPsuIdentification")
  public Optional<HrefType> getStartAuthorisationWithPsuIdentification() {
    return startAuthorisationWithPsuIdentification;
  }

  public void setStartAuthorisationWithPsuIdentification(Optional<HrefType> startAuthorisationWithPsuIdentification) {
    this.startAuthorisationWithPsuIdentification = startAuthorisationWithPsuIdentification;
  }

  public Links updatePsuIdentification(HrefType updatePsuIdentification) {
    this.updatePsuIdentification = Optional.ofNullable(updatePsuIdentification);
    return this;
  }

  /**
   * The link to the payment initiation or account information resource, which needs to be updated by the PSU identification if not delivered yet.
   * @return updatePsuIdentification
   */
  @Valid
  @Schema(name = "updatePsuIdentification", description = "The link to the payment initiation or account information resource, which needs to be updated by the PSU identification if not delivered yet. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatePsuIdentification")
  public Optional<HrefType> getUpdatePsuIdentification() {
    return updatePsuIdentification;
  }

  public void setUpdatePsuIdentification(Optional<HrefType> updatePsuIdentification) {
    this.updatePsuIdentification = updatePsuIdentification;
  }

  public Links startAuthorisationWithProprietaryData(HrefType startAuthorisationWithProprietaryData) {
    this.startAuthorisationWithProprietaryData = Optional.ofNullable(startAuthorisationWithProprietaryData);
    return this;
  }

  /**
   * A link to the endpoint, where the authorisation of a transaction or of a transaction cancellation shall be started, and where proprietary data needs to be updated with this call.  The TPP can find the scope of missing proprietary data in the ASPSP documentation. The usage of this hyperlink is not further specified in the specification but is used analogously to e.g. the startAuthorisation withPsuIdentification hyperlink.
   * @return startAuthorisationWithProprietaryData
   */
  @Valid
  @Schema(name = "startAuthorisationWithProprietaryData", description = "A link to the endpoint, where the authorisation of a transaction or of a transaction cancellation shall be started, and where proprietary data needs to be updated with this call.  The TPP can find the scope of missing proprietary data in the ASPSP documentation. The usage of this hyperlink is not further specified in the specification but is used analogously to e.g. the startAuthorisation withPsuIdentification hyperlink. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithProprietaryData")
  public Optional<HrefType> getStartAuthorisationWithProprietaryData() {
    return startAuthorisationWithProprietaryData;
  }

  public void setStartAuthorisationWithProprietaryData(Optional<HrefType> startAuthorisationWithProprietaryData) {
    this.startAuthorisationWithProprietaryData = startAuthorisationWithProprietaryData;
  }

  public Links updateProprietaryData(HrefType updateProprietaryData) {
    this.updateProprietaryData = Optional.ofNullable(updateProprietaryData);
    return this;
  }

  /**
   * The link to the payment initiation or account information resource, which needs to be updated by the proprietary data.
   * @return updateProprietaryData
   */
  @Valid
  @Schema(name = "updateProprietaryData", description = "The link to the payment initiation or account information resource, which needs to be updated by the proprietary data. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updateProprietaryData")
  public Optional<HrefType> getUpdateProprietaryData() {
    return updateProprietaryData;
  }

  public void setUpdateProprietaryData(Optional<HrefType> updateProprietaryData) {
    this.updateProprietaryData = updateProprietaryData;
  }

  public Links startAuthorisationWithPsuAuthentication(HrefType startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = Optional.ofNullable(startAuthorisationWithPsuAuthentication);
    return this;
  }

  /**
   * The link to an endpoint where the authorisation of a transaction or of a transaction cancellation shall be started, where PSU authentication data shall be uploaded  with the corresponding call.
   * @return startAuthorisationWithPsuAuthentication
   */
  @Valid
  @Schema(name = "startAuthorisationWithPsuAuthentication", description = "The link to an endpoint where the authorisation of a transaction or of a transaction cancellation shall be started, where PSU authentication data shall be uploaded  with the corresponding call. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithPsuAuthentication")
  public Optional<HrefType> getStartAuthorisationWithPsuAuthentication() {
    return startAuthorisationWithPsuAuthentication;
  }

  public void setStartAuthorisationWithPsuAuthentication(Optional<HrefType> startAuthorisationWithPsuAuthentication) {
    this.startAuthorisationWithPsuAuthentication = startAuthorisationWithPsuAuthentication;
  }

  public Links updatePsuAuthentication(HrefType updatePsuAuthentication) {
    this.updatePsuAuthentication = Optional.ofNullable(updatePsuAuthentication);
    return this;
  }

  /**
   * The link to the payment initiation or account information resource, which needs to be updated by a PSU password and eventually the PSU identification if not delivered yet.
   * @return updatePsuAuthentication
   */
  @Valid
  @Schema(name = "updatePsuAuthentication", description = "The link to the payment initiation or account information resource, which needs to be updated by a PSU password and eventually the PSU identification if not delivered yet.  ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updatePsuAuthentication")
  public Optional<HrefType> getUpdatePsuAuthentication() {
    return updatePsuAuthentication;
  }

  public void setUpdatePsuAuthentication(Optional<HrefType> updatePsuAuthentication) {
    this.updatePsuAuthentication = updatePsuAuthentication;
  }

  public Links startAuthorisationWithEncryptedPsuAuthentication(HrefType startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = Optional.ofNullable(startAuthorisationWithEncryptedPsuAuthentication);
    return this;
  }

  /**
   * The link to an endpoint where the authorisation of a transaction or of a transaction cancellation shall be started, where encrypted PSU authentication data shall be uploaded  with the corresponding call.
   * @return startAuthorisationWithEncryptedPsuAuthentication
   */
  @Valid
  @Schema(name = "startAuthorisationWithEncryptedPsuAuthentication", description = "The link to an endpoint where the authorisation of a transaction or of a transaction cancellation shall be started, where encrypted PSU authentication data shall be uploaded  with the corresponding call. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithEncryptedPsuAuthentication")
  public Optional<HrefType> getStartAuthorisationWithEncryptedPsuAuthentication() {
    return startAuthorisationWithEncryptedPsuAuthentication;
  }

  public void setStartAuthorisationWithEncryptedPsuAuthentication(Optional<HrefType> startAuthorisationWithEncryptedPsuAuthentication) {
    this.startAuthorisationWithEncryptedPsuAuthentication = startAuthorisationWithEncryptedPsuAuthentication;
  }

  public Links updateEncryptedPsuAuthentication(HrefType updateEncryptedPsuAuthentication) {
    this.updateEncryptedPsuAuthentication = Optional.ofNullable(updateEncryptedPsuAuthentication);
    return this;
  }

  /**
   * The link to the payment initiation or account information resource, which needs to be updated by an encrypted PSU password and eventually the PSU identification if not delivered yet.
   * @return updateEncryptedPsuAuthentication
   */
  @Valid
  @Schema(name = "updateEncryptedPsuAuthentication", description = "The link to the payment initiation or account information resource, which needs to be updated by an encrypted PSU password and eventually the PSU identification if not delivered yet.  ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updateEncryptedPsuAuthentication")
  public Optional<HrefType> getUpdateEncryptedPsuAuthentication() {
    return updateEncryptedPsuAuthentication;
  }

  public void setUpdateEncryptedPsuAuthentication(Optional<HrefType> updateEncryptedPsuAuthentication) {
    this.updateEncryptedPsuAuthentication = updateEncryptedPsuAuthentication;
  }

  public Links startAuthorisationWithAuthenticationMethodSelection(HrefType startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = Optional.ofNullable(startAuthorisationWithAuthenticationMethodSelection);
    return this;
  }

  /**
   * This is a link to and endpoint where the authorisation of a transaction or of a transaction cancellation shall be started,  where the selected SCA method shall be uploaded  with the corresponding call.
   * @return startAuthorisationWithAuthenticationMethodSelection
   */
  @Valid
  @Schema(name = "startAuthorisationWithAuthenticationMethodSelection", description = "This is a link to and endpoint where the authorisation of a transaction or of a transaction cancellation shall be started,  where the selected SCA method shall be uploaded  with the corresponding call. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithAuthenticationMethodSelection")
  public Optional<HrefType> getStartAuthorisationWithAuthenticationMethodSelection() {
    return startAuthorisationWithAuthenticationMethodSelection;
  }

  public void setStartAuthorisationWithAuthenticationMethodSelection(Optional<HrefType> startAuthorisationWithAuthenticationMethodSelection) {
    this.startAuthorisationWithAuthenticationMethodSelection = startAuthorisationWithAuthenticationMethodSelection;
  }

  public Links selectAuthenticationMethod(HrefType selectAuthenticationMethod) {
    this.selectAuthenticationMethod = Optional.ofNullable(selectAuthenticationMethod);
    return this;
  }

  /**
   * This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there were several available authentication methods.
   * @return selectAuthenticationMethod
   */
  @Valid
  @Schema(name = "selectAuthenticationMethod", description = "This is a link to a resource, where the TPP can select the applicable second factor authentication methods for the PSU, if there were several available authentication methods.  ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("selectAuthenticationMethod")
  public Optional<HrefType> getSelectAuthenticationMethod() {
    return selectAuthenticationMethod;
  }

  public void setSelectAuthenticationMethod(Optional<HrefType> selectAuthenticationMethod) {
    this.selectAuthenticationMethod = selectAuthenticationMethod;
  }

  public Links startAuthorisationWithTransactionAuthorisation(HrefType startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = Optional.ofNullable(startAuthorisationWithTransactionAuthorisation);
    return this;
  }

  /**
   * A link to an endpoint, where an authorisation of a transaction or a cancellation can be started, and where the response data for the challenge is uploaded in the same call for the  transaction authorisation or transaction cancellation at the same time in the Embedded SCA Approach.
   * @return startAuthorisationWithTransactionAuthorisation
   */
  @Valid
  @Schema(name = "startAuthorisationWithTransactionAuthorisation", description = "A link to an endpoint, where an authorisation of a transaction or a cancellation can be started, and where the response data for the challenge is uploaded in the same call for the  transaction authorisation or transaction cancellation at the same time in the Embedded SCA Approach. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("startAuthorisationWithTransactionAuthorisation")
  public Optional<HrefType> getStartAuthorisationWithTransactionAuthorisation() {
    return startAuthorisationWithTransactionAuthorisation;
  }

  public void setStartAuthorisationWithTransactionAuthorisation(Optional<HrefType> startAuthorisationWithTransactionAuthorisation) {
    this.startAuthorisationWithTransactionAuthorisation = startAuthorisationWithTransactionAuthorisation;
  }

  public Links authoriseTransaction(HrefType authoriseTransaction) {
    this.authoriseTransaction = Optional.ofNullable(authoriseTransaction);
    return this;
  }

  /**
   * The link to the payment initiation or consent resource, where the \\\"Transaction Authorisation\\\"Request\\\" is sent to. This is the link to the resource which will authorise the payment or the consent by  checking the SCA authentication data within the Embedded SCA approach.
   * @return authoriseTransaction
   */
  @Valid
  @Schema(name = "authoriseTransaction", description = "The link to the payment initiation or consent resource, where the \\\"Transaction Authorisation\\\"Request\\\" is sent to. This is the link to the resource which will authorise the payment or the consent by  checking the SCA authentication data within the Embedded SCA approach. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("authoriseTransaction")
  public Optional<HrefType> getAuthoriseTransaction() {
    return authoriseTransaction;
  }

  public void setAuthoriseTransaction(Optional<HrefType> authoriseTransaction) {
    this.authoriseTransaction = authoriseTransaction;
  }

  public Links updateResourceByDebtorAccountResource(HrefType updateResourceByDebtorAccountResource) {
    this.updateResourceByDebtorAccountResource = Optional.ofNullable(updateResourceByDebtorAccountResource);
    return this;
  }

  /**
   * A link to a payment related resource which needs to be updated by an account, referenced by a resource identification.
   * @return updateResourceByDebtorAccountResource
   */
  @Valid
  @Schema(name = "updateResourceByDebtorAccountResource", description = "A link to a payment related resource which needs to be updated by an account, referenced by a resource identification. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("updateResourceByDebtorAccountResource")
  public Optional<HrefType> getUpdateResourceByDebtorAccountResource() {
    return updateResourceByDebtorAccountResource;
  }

  public void setUpdateResourceByDebtorAccountResource(Optional<HrefType> updateResourceByDebtorAccountResource) {
    this.updateResourceByDebtorAccountResource = updateResourceByDebtorAccountResource;
  }

  public Links self(HrefType self) {
    this.self = Optional.ofNullable(self);
    return this;
  }

  /**
   * The link to the payment initiation resource created by the request itself.  This link can be used later to retrieve the transaction status of the payment initiation.
   * @return self
   */
  @Valid
  @Schema(name = "self", description = "The link to the payment initiation resource created by the request itself.  This link can be used later to retrieve the transaction status of the payment initiation. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("self")
  public Optional<HrefType> getSelf() {
    return self;
  }

  public void setSelf(Optional<HrefType> self) {
    this.self = self;
  }

  public Links status(HrefType status) {
    this.status = Optional.ofNullable(status);
    return this;
  }

  /**
   * A link to retrieve the status of the transaction resource.
   * @return status
   */
  @Valid
  @Schema(name = "status", description = "A link to retrieve the status of the transaction resource. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("status")
  public Optional<HrefType> getStatus() {
    return status;
  }

  public void setStatus(Optional<HrefType> status) {
    this.status = status;
  }

  public Links transactionfees(HrefType transactionfees) {
    this.transactionfees = Optional.ofNullable(transactionfees);
    return this;
  }

  /**
   * The link is to the status resource.   This link is only added within the authorisation process in case fee information is available via the status resource.
   * @return transactionfees
   */
  @Valid
  @Schema(name = "transactionfees", description = "The link is to the status resource.   This link is only added within the authorisation process in case fee information is available via the status resource. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("transactionfees")
  public Optional<HrefType> getTransactionfees() {
    return transactionfees;
  }

  public void setTransactionfees(Optional<HrefType> transactionfees) {
    this.transactionfees = transactionfees;
  }

  public Links scaStatus(HrefType scaStatus) {
    this.scaStatus = Optional.ofNullable(scaStatus);
    return this;
  }

  /**
   * A link to retrieve the status of the authorisation or cancellation-authorisation sub-resource.
   * @return scaStatus
   */
  @Valid
  @Schema(name = "scaStatus", description = "A link to retrieve the status of the authorisation or cancellation-authorisation sub-resource. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("scaStatus")
  public Optional<HrefType> getScaStatus() {
    return scaStatus;
  }

  public void setScaStatus(Optional<HrefType> scaStatus) {
    this.scaStatus = scaStatus;
  }

  public Links account(HrefType account) {
    this.account = Optional.ofNullable(account);
    return this;
  }

  /**
   * A link to the resource providing the details of one account
   * @return account
   */
  @Valid
  @Schema(name = "account", description = "A link to the resource providing the details of one account ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("account")
  public Optional<HrefType> getAccount() {
    return account;
  }

  public void setAccount(Optional<HrefType> account) {
    this.account = account;
  }

  public Links savingsAccount(HrefType savingsAccount) {
    this.savingsAccount = Optional.ofNullable(savingsAccount);
    return this;
  }

  /**
   * A link to the resource providing the details of a savings account.
   * @return savingsAccount
   */
  @Valid
  @Schema(name = "savingsAccount", description = "A link to the resource providing the details of a savings account. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("savingsAccount")
  public Optional<HrefType> getSavingsAccount() {
    return savingsAccount;
  }

  public void setSavingsAccount(Optional<HrefType> savingsAccount) {
    this.savingsAccount = savingsAccount;
  }

  public Links loanAccount(HrefType loanAccount) {
    this.loanAccount = Optional.ofNullable(loanAccount);
    return this;
  }

  /**
   * A link to the resource providing the details of a loan account.
   * @return loanAccount
   */
  @Valid
  @Schema(name = "loanAccount", description = "A link to the resource providing the details of a loan account. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("loanAccount")
  public Optional<HrefType> getLoanAccount() {
    return loanAccount;
  }

  public void setLoanAccount(Optional<HrefType> loanAccount) {
    this.loanAccount = loanAccount;
  }

  public Links balances(HrefType balances) {
    this.balances = Optional.ofNullable(balances);
    return this;
  }

  /**
   * A link to the resource providing the balance of a dedicated account.
   * @return balances
   */
  @Valid
  @Schema(name = "balances", description = "A link to the resource providing the balance of a dedicated account. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("balances")
  public Optional<HrefType> getBalances() {
    return balances;
  }

  public void setBalances(Optional<HrefType> balances) {
    this.balances = balances;
  }

  public Links transactions(HrefType transactions) {
    this.transactions = Optional.ofNullable(transactions);
    return this;
  }

  /**
   * A link to the resource providing the transaction history of a dedicated account.
   * @return transactions
   */
  @Valid
  @Schema(name = "transactions", description = "A link to the resource providing the transaction history of a dedicated account. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("transactions")
  public Optional<HrefType> getTransactions() {
    return transactions;
  }

  public void setTransactions(Optional<HrefType> transactions) {
    this.transactions = transactions;
  }

  public Links cardAccount(HrefType cardAccount) {
    this.cardAccount = Optional.ofNullable(cardAccount);
    return this;
  }

  /**
   * A link to the resource providing the details of one card account.
   * @return cardAccount
   */
  @Valid
  @Schema(name = "cardAccount", description = "A link to the resource providing the details of one card account. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("cardAccount")
  public Optional<HrefType> getCardAccount() {
    return cardAccount;
  }

  public void setCardAccount(Optional<HrefType> cardAccount) {
    this.cardAccount = cardAccount;
  }

  public Links cardTransactions(HrefType cardTransactions) {
    this.cardTransactions = Optional.ofNullable(cardTransactions);
    return this;
  }

  /**
   * A link to the resource providing the transaction history of a dedicated card account.
   * @return cardTransactions
   */
  @Valid
  @Schema(name = "cardTransactions", description = "A link to the resource providing the transaction history of a dedicated card account. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("cardTransactions")
  public Optional<HrefType> getCardTransactions() {
    return cardTransactions;
  }

  public void setCardTransactions(Optional<HrefType> cardTransactions) {
    this.cardTransactions = cardTransactions;
  }

  public Links transactionDetails(HrefType transactionDetails) {
    this.transactionDetails = Optional.ofNullable(transactionDetails);
    return this;
  }

  /**
   * A link to the resource providing details of a dedicated transaction.
   * @return transactionDetails
   */
  @Valid
  @Schema(name = "transactionDetails", description = "A link to the resource providing details of a dedicated transaction. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("transactionDetails")
  public Optional<HrefType> getTransactionDetails() {
    return transactionDetails;
  }

  public void setTransactionDetails(Optional<HrefType> transactionDetails) {
    this.transactionDetails = transactionDetails;
  }

  public Links ibanCheck(HrefType ibanCheck) {
    this.ibanCheck = Optional.ofNullable(ibanCheck);
    return this;
  }

  /**
   * A link to the endpoint offering the addressed iban check result.
   * @return ibanCheck
   */
  @Valid
  @Schema(name = "ibanCheck", description = "A link to the endpoint offering the addressed iban check result. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("ibanCheck")
  public Optional<HrefType> getIbanCheck() {
    return ibanCheck;
  }

  public void setIbanCheck(Optional<HrefType> ibanCheck) {
    this.ibanCheck = ibanCheck;
  }

  public Links paymentInitiation(HrefType paymentInitiation) {
    this.paymentInitiation = Optional.ofNullable(paymentInitiation);
    return this;
  }

  /**
   * A link to an initiation related to a payment resource.
   * @return paymentInitiation
   */
  @Valid
  @Schema(name = "paymentInitiation", description = "A link to an initiation related to a payment resource. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("paymentInitiation")
  public Optional<HrefType> getPaymentInitiation() {
    return paymentInitiation;
  }

  public void setPaymentInitiation(Optional<HrefType> paymentInitiation) {
    this.paymentInitiation = paymentInitiation;
  }

  public Links securitiesAccount(HrefType securitiesAccount) {
    this.securitiesAccount = Optional.ofNullable(securitiesAccount);
    return this;
  }

  /**
   * A link to the resource providing the details of one securitiesAccount.
   * @return securitiesAccount
   */
  @Valid
  @Schema(name = "securitiesAccount", description = "A link to the resource providing the details of one securitiesAccount. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("securitiesAccount")
  public Optional<HrefType> getSecuritiesAccount() {
    return securitiesAccount;
  }

  public void setSecuritiesAccount(Optional<HrefType> securitiesAccount) {
    this.securitiesAccount = securitiesAccount;
  }

  public Links positions(HrefType positions) {
    this.positions = Optional.ofNullable(positions);
    return this;
  }

  /**
   * A link to the resource providing the list of positions of one securitiesAccount.
   * @return positions
   */
  @Valid
  @Schema(name = "positions", description = "A link to the resource providing the list of positions of one securitiesAccount. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("positions")
  public Optional<HrefType> getPositions() {
    return positions;
  }

  public void setPositions(Optional<HrefType> positions) {
    this.positions = positions;
  }

  public Links orders(HrefType orders) {
    this.orders = Optional.ofNullable(orders);
    return this;
  }

  /**
   * A link to the resource providing the list of orders of one securitiesAccount.
   * @return orders
   */
  @Valid
  @Schema(name = "orders", description = "A link to the resource providing the list of orders of one securitiesAccount. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("orders")
  public Optional<HrefType> getOrders() {
    return orders;
  }

  public void setOrders(Optional<HrefType> orders) {
    this.orders = orders;
  }

  public Links orderDetails(HrefType orderDetails) {
    this.orderDetails = Optional.ofNullable(orderDetails);
    return this;
  }

  /**
   * A link to the resource providing details of one specific order.
   * @return orderDetails
   */
  @Valid
  @Schema(name = "orderDetails", description = "A link to the resource providing details of one specific order. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("orderDetails")
  public Optional<HrefType> getOrderDetails() {
    return orderDetails;
  }

  public void setOrderDetails(Optional<HrefType> orderDetails) {
    this.orderDetails = orderDetails;
  }

  public Links relatedOrders(List<@Valid HrefType> relatedOrders) {
    this.relatedOrders = relatedOrders;
    return this;
  }

  public Links addRelatedOrdersItem(HrefType relatedOrdersItem) {
    if (this.relatedOrders == null) {
      this.relatedOrders = new ArrayList<>();
    }
    this.relatedOrders.add(relatedOrdersItem);
    return this;
  }

  /**
   * An array of links to request order details of related orders related to the order that is represented by the data structure containing this element  (e.g. other orders that originated from the same order split as this order).
   * @return relatedOrders
   */
  @Valid
  @Schema(name = "relatedOrders", description = "An array of links to request order details of related orders related to the order that is represented by the data structure containing this element  (e.g. other orders that originated from the same order split as this order).  ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("relatedOrders")
  public List<@Valid HrefType> getRelatedOrders() {
    return relatedOrders;
  }

  public void setRelatedOrders(List<@Valid HrefType> relatedOrders) {
    this.relatedOrders = relatedOrders;
  }

  public Links relatedTransactions(List<@Valid HrefType> relatedTransactions) {
    this.relatedTransactions = relatedTransactions;
    return this;
  }

  public Links addRelatedTransactionsItem(HrefType relatedTransactionsItem) {
    if (this.relatedTransactions == null) {
      this.relatedTransactions = new ArrayList<>();
    }
    this.relatedTransactions.add(relatedTransactionsItem);
    return this;
  }

  /**
   * An array of links to request securities transaction details of transactions that resulted from this order.
   * @return relatedTransactions
   */
  @Valid
  @Schema(name = "relatedTransactions", description = "An array of links to request securities transaction details of transactions that resulted from this order. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("relatedTransactions")
  public List<@Valid HrefType> getRelatedTransactions() {
    return relatedTransactions;
  }

  public void setRelatedTransactions(List<@Valid HrefType> relatedTransactions) {
    this.relatedTransactions = relatedTransactions;
  }

  public Links subscription(HrefType subscription) {
    this.subscription = Optional.ofNullable(subscription);
    return this;
  }

  /**
   * A link to the resource providing the details of a subscription for Push AIS Services.
   * @return subscription
   */
  @Valid
  @Schema(name = "subscription", description = "A link to the resource providing the details of a subscription for Push AIS Services. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("subscription")
  public Optional<HrefType> getSubscription() {
    return subscription;
  }

  public void setSubscription(Optional<HrefType> subscription) {
    this.subscription = subscription;
  }

  public Links entryStatusRevoked(List<@Valid HrefType> entryStatusRevoked) {
    this.entryStatusRevoked = entryStatusRevoked;
    return this;
  }

  public Links addEntryStatusRevokedItem(HrefType entryStatusRevokedItem) {
    if (this.entryStatusRevoked == null) {
      this.entryStatusRevoked = new ArrayList<>();
    }
    this.entryStatusRevoked.add(entryStatusRevokedItem);
    return this;
  }

  /**
   * Links to entry endpoints where the entry status is revoked.
   * @return entryStatusRevoked
   */
  @Valid
  @Schema(name = "entryStatusRevoked", description = "Links to entry endpoints where the entry status is revoked. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("entryStatusRevoked")
  public List<@Valid HrefType> getEntryStatusRevoked() {
    return entryStatusRevoked;
  }

  public void setEntryStatusRevoked(List<@Valid HrefType> entryStatusRevoked) {
    this.entryStatusRevoked = entryStatusRevoked;
  }

  public Links first(HrefType first) {
    this.first = Optional.ofNullable(first);
    return this;
  }

  /**
   * Navigation link for paginated account reports.
   * @return first
   */
  @Valid
  @Schema(name = "first", description = "Navigation link for paginated account reports. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("first")
  public Optional<HrefType> getFirst() {
    return first;
  }

  public void setFirst(Optional<HrefType> first) {
    this.first = first;
  }

  public Links next(HrefType next) {
    this.next = Optional.ofNullable(next);
    return this;
  }

  /**
   * Navigation link for paginated account reports.
   * @return next
   */
  @Valid
  @Schema(name = "next", description = "Navigation link for paginated account reports. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("next")
  public Optional<HrefType> getNext() {
    return next;
  }

  public void setNext(Optional<HrefType> next) {
    this.next = next;
  }

  public Links previous(HrefType previous) {
    this.previous = Optional.ofNullable(previous);
    return this;
  }

  /**
   * Navigation link for paginated account reports.
   * @return previous
   */
  @Valid
  @Schema(name = "previous", description = "Navigation link for paginated account reports. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("previous")
  public Optional<HrefType> getPrevious() {
    return previous;
  }

  public void setPrevious(Optional<HrefType> previous) {
    this.previous = previous;
  }

  public Links last(HrefType last) {
    this.last = Optional.ofNullable(last);
    return this;
  }

  /**
   * Navigation link for paginated account reports.
   * @return last
   */
  @Valid
  @Schema(name = "last", description = "Navigation link for paginated account reports. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("last")
  public Optional<HrefType> getLast() {
    return last;
  }

  public void setLast(Optional<HrefType> last) {
    this.last = last;
  }

  public Links download(HrefType download) {
    this.download = Optional.ofNullable(download);
    return this;
  }

  /**
   * Download link for huge AIS data packages.
   * @return download
   */
  @Valid
  @Schema(name = "download", description = "Download link for huge AIS data packages. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("download")
  public Optional<HrefType> getDownload() {
    return download;
  }

  public void setDownload(Optional<HrefType> download) {
    this.download = download;
  }

  public Links confirmInitiation(HrefType confirmInitiation) {
    this.confirmInitiation = Optional.ofNullable(confirmInitiation);
    return this;
  }

  /**
   * link to a confirmation endpoint, where a payment initiation needs to be confirmed explicitly by the API Client for execution.
   * @return confirmInitiation
   */
  @Valid
  @Schema(name = "confirmInitiation", description = "link to a confirmation endpoint, where a payment initiation needs to be confirmed explicitly by the API Client for execution. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("confirmInitiation")
  public Optional<HrefType> getConfirmInitiation() {
    return confirmInitiation;
  }

  public void setConfirmInitiation(Optional<HrefType> confirmInitiation) {
    this.confirmInitiation = confirmInitiation;
  }

  public Links aspspParameters(HrefType aspspParameters) {
    this.aspspParameters = Optional.ofNullable(aspspParameters);
    return this;
  }

  /**
   *
   * @return aspspParameters
   */
  @Valid
  @Schema(name = "aspspParameters", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("aspspParameters")
  public Optional<HrefType> getAspspParameters() {
    return aspspParameters;
  }

  public void setAspspParameters(Optional<HrefType> aspspParameters) {
    this.aspspParameters = aspspParameters;
  }

  public Links aspspContacts(HrefType aspspContacts) {
    this.aspspContacts = Optional.ofNullable(aspspContacts);
    return this;
  }

  /**
   *
   * @return aspspContacts
   */
  @Valid
  @Schema(name = "aspspContacts", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("aspspContacts")
  public Optional<HrefType> getAspspContacts() {
    return aspspContacts;
  }

  public void setAspspContacts(Optional<HrefType> aspspContacts) {
    this.aspspContacts = aspspContacts;
  }

  public Links aspspDowntimes(HrefType aspspDowntimes) {
    this.aspspDowntimes = Optional.ofNullable(aspspDowntimes);
    return this;
  }

  /**
   *
   * @return aspspDowntimes
   */
  @Valid
  @Schema(name = "aspspDowntimes", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("aspspDowntimes")
  public Optional<HrefType> getAspspDowntimes() {
    return aspspDowntimes;
  }

  public void setAspspDowntimes(Optional<HrefType> aspspDowntimes) {
    this.aspspDowntimes = aspspDowntimes;
  }

  public Links onboardings(HrefType onboardings) {
    this.onboardings = Optional.ofNullable(onboardings);
    return this;
  }

  /**
   * A link to commercially onboard to the related API, see openFinance, Admin Services for more details.
   * @return onboardings
   */
  @Valid
  @Schema(name = "onboardings", description = "A link to commercially onboard to the related API, see openFinance, Admin Services for more details. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("onboardings")
  public Optional<HrefType> getOnboardings() {
    return onboardings;
  }

  public void setOnboardings(Optional<HrefType> onboardings) {
    this.onboardings = onboardings;
  }

  public Links readConditions(HrefType readConditions) {
    this.readConditions = Optional.ofNullable(readConditions);
    return this;
  }

  /**
   *
   * @return readConditions
   */
  @Valid
  @Schema(name = "readConditions", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("readConditions")
  public Optional<HrefType> getReadConditions() {
    return readConditions;
  }

  public void setReadConditions(Optional<HrefType> readConditions) {
    this.readConditions = readConditions;
  }

  public Links confirmConditions(HrefType confirmConditions) {
    this.confirmConditions = Optional.ofNullable(confirmConditions);
    return this;
  }

  /**
   *
   * @return confirmConditions
   */
  @Valid
  @Schema(name = "confirmConditions", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("confirmConditions")
  public Optional<HrefType> getConfirmConditions() {
    return confirmConditions;
  }

  public void setConfirmConditions(Optional<HrefType> confirmConditions) {
    this.confirmConditions = confirmConditions;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Links links = (Links) o;
    return Objects.equals(this.scaRedirect, links.scaRedirect) &&
        Objects.equals(this.scaOAuth, links.scaOAuth) &&
        Objects.equals(this.confirmation, links.confirmation) &&
        Objects.equals(this.creditorNameConfirmation, links.creditorNameConfirmation) &&
        Objects.equals(this.startAuthorisation, links.startAuthorisation) &&
        Objects.equals(this.startAuthorisationWithPsuIdentification, links.startAuthorisationWithPsuIdentification) &&
        Objects.equals(this.updatePsuIdentification, links.updatePsuIdentification) &&
        Objects.equals(this.startAuthorisationWithProprietaryData, links.startAuthorisationWithProprietaryData) &&
        Objects.equals(this.updateProprietaryData, links.updateProprietaryData) &&
        Objects.equals(this.startAuthorisationWithPsuAuthentication, links.startAuthorisationWithPsuAuthentication) &&
        Objects.equals(this.updatePsuAuthentication, links.updatePsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithEncryptedPsuAuthentication, links.startAuthorisationWithEncryptedPsuAuthentication) &&
        Objects.equals(this.updateEncryptedPsuAuthentication, links.updateEncryptedPsuAuthentication) &&
        Objects.equals(this.startAuthorisationWithAuthenticationMethodSelection, links.startAuthorisationWithAuthenticationMethodSelection) &&
        Objects.equals(this.selectAuthenticationMethod, links.selectAuthenticationMethod) &&
        Objects.equals(this.startAuthorisationWithTransactionAuthorisation, links.startAuthorisationWithTransactionAuthorisation) &&
        Objects.equals(this.authoriseTransaction, links.authoriseTransaction) &&
        Objects.equals(this.updateResourceByDebtorAccountResource, links.updateResourceByDebtorAccountResource) &&
        Objects.equals(this.self, links.self) &&
        Objects.equals(this.status, links.status) &&
        Objects.equals(this.transactionfees, links.transactionfees) &&
        Objects.equals(this.scaStatus, links.scaStatus) &&
        Objects.equals(this.account, links.account) &&
        Objects.equals(this.savingsAccount, links.savingsAccount) &&
        Objects.equals(this.loanAccount, links.loanAccount) &&
        Objects.equals(this.balances, links.balances) &&
        Objects.equals(this.transactions, links.transactions) &&
        Objects.equals(this.cardAccount, links.cardAccount) &&
        Objects.equals(this.cardTransactions, links.cardTransactions) &&
        Objects.equals(this.transactionDetails, links.transactionDetails) &&
        Objects.equals(this.ibanCheck, links.ibanCheck) &&
        Objects.equals(this.paymentInitiation, links.paymentInitiation) &&
        Objects.equals(this.securitiesAccount, links.securitiesAccount) &&
        Objects.equals(this.positions, links.positions) &&
        Objects.equals(this.orders, links.orders) &&
        Objects.equals(this.orderDetails, links.orderDetails) &&
        Objects.equals(this.relatedOrders, links.relatedOrders) &&
        Objects.equals(this.relatedTransactions, links.relatedTransactions) &&
        Objects.equals(this.subscription, links.subscription) &&
        Objects.equals(this.entryStatusRevoked, links.entryStatusRevoked) &&
        Objects.equals(this.first, links.first) &&
        Objects.equals(this.next, links.next) &&
        Objects.equals(this.previous, links.previous) &&
        Objects.equals(this.last, links.last) &&
        Objects.equals(this.download, links.download) &&
        Objects.equals(this.confirmInitiation, links.confirmInitiation) &&
        Objects.equals(this.aspspParameters, links.aspspParameters) &&
        Objects.equals(this.aspspContacts, links.aspspContacts) &&
        Objects.equals(this.aspspDowntimes, links.aspspDowntimes) &&
        Objects.equals(this.onboardings, links.onboardings) &&
        Objects.equals(this.readConditions, links.readConditions) &&
        Objects.equals(this.confirmConditions, links.confirmConditions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scaRedirect, scaOAuth, confirmation, creditorNameConfirmation, startAuthorisation, startAuthorisationWithPsuIdentification, updatePsuIdentification, startAuthorisationWithProprietaryData, updateProprietaryData, startAuthorisationWithPsuAuthentication, updatePsuAuthentication, startAuthorisationWithEncryptedPsuAuthentication, updateEncryptedPsuAuthentication, startAuthorisationWithAuthenticationMethodSelection, selectAuthenticationMethod, startAuthorisationWithTransactionAuthorisation, authoriseTransaction, updateResourceByDebtorAccountResource, self, status, transactionfees, scaStatus, account, savingsAccount, loanAccount, balances, transactions, cardAccount, cardTransactions, transactionDetails, ibanCheck, paymentInitiation, securitiesAccount, positions, orders, orderDetails, relatedOrders, relatedTransactions, subscription, entryStatusRevoked, first, next, previous, last, download, confirmInitiation, aspspParameters, aspspContacts, aspspDowntimes, onboardings, readConditions, confirmConditions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Links {\n");
    sb.append("    scaRedirect: ").append(toIndentedString(scaRedirect)).append("\n");
    sb.append("    scaOAuth: ").append(toIndentedString(scaOAuth)).append("\n");
    sb.append("    confirmation: ").append(toIndentedString(confirmation)).append("\n");
    sb.append("    creditorNameConfirmation: ").append(toIndentedString(creditorNameConfirmation)).append("\n");
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
    sb.append("    updateResourceByDebtorAccountResource: ").append(toIndentedString(updateResourceByDebtorAccountResource)).append("\n");
    sb.append("    self: ").append(toIndentedString(self)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    transactionfees: ").append(toIndentedString(transactionfees)).append("\n");
    sb.append("    scaStatus: ").append(toIndentedString(scaStatus)).append("\n");
    sb.append("    account: ").append(toIndentedString(account)).append("\n");
    sb.append("    savingsAccount: ").append(toIndentedString(savingsAccount)).append("\n");
    sb.append("    loanAccount: ").append(toIndentedString(loanAccount)).append("\n");
    sb.append("    balances: ").append(toIndentedString(balances)).append("\n");
    sb.append("    transactions: ").append(toIndentedString(transactions)).append("\n");
    sb.append("    cardAccount: ").append(toIndentedString(cardAccount)).append("\n");
    sb.append("    cardTransactions: ").append(toIndentedString(cardTransactions)).append("\n");
    sb.append("    transactionDetails: ").append(toIndentedString(transactionDetails)).append("\n");
    sb.append("    ibanCheck: ").append(toIndentedString(ibanCheck)).append("\n");
    sb.append("    paymentInitiation: ").append(toIndentedString(paymentInitiation)).append("\n");
    sb.append("    securitiesAccount: ").append(toIndentedString(securitiesAccount)).append("\n");
    sb.append("    positions: ").append(toIndentedString(positions)).append("\n");
    sb.append("    orders: ").append(toIndentedString(orders)).append("\n");
    sb.append("    orderDetails: ").append(toIndentedString(orderDetails)).append("\n");
    sb.append("    relatedOrders: ").append(toIndentedString(relatedOrders)).append("\n");
    sb.append("    relatedTransactions: ").append(toIndentedString(relatedTransactions)).append("\n");
    sb.append("    subscription: ").append(toIndentedString(subscription)).append("\n");
    sb.append("    entryStatusRevoked: ").append(toIndentedString(entryStatusRevoked)).append("\n");
    sb.append("    first: ").append(toIndentedString(first)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("    last: ").append(toIndentedString(last)).append("\n");
    sb.append("    download: ").append(toIndentedString(download)).append("\n");
    sb.append("    confirmInitiation: ").append(toIndentedString(confirmInitiation)).append("\n");
    sb.append("    aspspParameters: ").append(toIndentedString(aspspParameters)).append("\n");
    sb.append("    aspspContacts: ").append(toIndentedString(aspspContacts)).append("\n");
    sb.append("    aspspDowntimes: ").append(toIndentedString(aspspDowntimes)).append("\n");
    sb.append("    onboardings: ").append(toIndentedString(onboardings)).append("\n");
    sb.append("    readConditions: ").append(toIndentedString(readConditions)).append("\n");
    sb.append("    confirmConditions: ").append(toIndentedString(confirmConditions)).append("\n");
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

