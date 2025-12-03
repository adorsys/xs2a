package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * Body of the response for a successful bulk payment initiation extended status request in case of an JSON based endpoint.
 */
@Schema(description = "Body of the response for a successful bulk payment initiation extended status request in case of an JSON based endpoint.")
@Validated
@jakarta.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2025-10-20T11:14:45.442141177+01:00[Africa/Douala]")


public class BulkPaymentExtendedStatusResponse200Json   {
  @JsonProperty("groupStatus")
  private GroupStatus groupStatus = null;

  @JsonProperty("reasonCode")
  private StatusReasonCode reasonCode = null;

  @JsonProperty("reasonPropietary")
  private String reasonPropietary = null;

  @JsonProperty("fundsAvailable")
  private Boolean fundsAvailable = null;

  @JsonProperty("originalTransactionInformationAndStatus")
  private OriginalTransactionInformationAndStatus originalTransactionInformationAndStatus = null;

  @JsonProperty("transactionFees")
  private Amount transactionFees = null;

  @JsonProperty("currencyConversionFees")
  private Amount currencyConversionFees = null;

  @JsonProperty("estimatedTotalAmount")
  private Amount estimatedTotalAmount = null;

  @JsonProperty("estimatedInterbankSettlementAmount")
  private Amount estimatedInterbankSettlementAmount = null;

  @JsonProperty("psuMessage")
  private String psuMessage = null;

  @JsonProperty("ownerNames")
  @Valid
  private List<AccountOwner> ownerNames = null;

  @JsonProperty("_links")
  private Map _links = null;

  @JsonProperty("apiClientMessages")
  @Valid
  private List<TppMessageGeneric> apiClientMessages = null;

  public BulkPaymentExtendedStatusResponse200Json groupStatus(GroupStatus groupStatus) {
    this.groupStatus = groupStatus;
    return this;
  }

  /**
   * Get groupStatus
   * @return groupStatus
   **/
  @Schema(required = true, description = "")
  @JsonProperty("groupStatus")
    @NotNull

  @Valid
  public GroupStatus getGroupStatus() {
    return groupStatus;
  }

  public void setGroupStatus(GroupStatus groupStatus) {
    this.groupStatus = groupStatus;
  }

  public BulkPaymentExtendedStatusResponse200Json reasonCode(StatusReasonCode reasonCode) {
    this.reasonCode = reasonCode;
    return this;
  }

  /**
   * Get reasonCode
   * @return reasonCode
   **/
  @Schema(description = "")
  @JsonProperty("reasonCode")

  @Valid
  public StatusReasonCode getReasonCode() {
    return reasonCode;
  }

  public void setReasonCode(StatusReasonCode reasonCode) {
    this.reasonCode = reasonCode;
  }

  public BulkPaymentExtendedStatusResponse200Json reasonPropietary(String reasonPropietary) {
    this.reasonPropietary = reasonPropietary;
    return this;
  }

  /**
   * Proprietary additional information on the reason for e.g. rejecting the request.
   * @return reasonPropietary
   **/
  @Schema(description = "Proprietary additional information on the reason for e.g. rejecting the request.")
  @JsonProperty("reasonPropietary")

  public String getReasonPropietary() {
    return reasonPropietary;
  }

  public void setReasonPropietary(String reasonPropietary) {
    this.reasonPropietary = reasonPropietary;
  }

  public BulkPaymentExtendedStatusResponse200Json fundsAvailable(Boolean fundsAvailable) {
    this.fundsAvailable = fundsAvailable;
    return this;
  }

  /**
   * Equals true if sufficient funds are available at the time of the request, false otherwise.  This data element is allways contained in a confirmation of funds response.  This data element is contained in a payment status response,  if supported by the ASPSP, if a funds check has been performed and  if the transactionStatus is \"ACTC\", \"ACWC\" or \"ACCP\".
   * @return fundsAvailable
   **/
  @Schema(description = "Equals true if sufficient funds are available at the time of the request, false otherwise.  This data element is allways contained in a confirmation of funds response.  This data element is contained in a payment status response,  if supported by the ASPSP, if a funds check has been performed and  if the transactionStatus is \"ACTC\", \"ACWC\" or \"ACCP\". ")
  @JsonProperty("fundsAvailable")

  public Boolean isFundsAvailable() {
    return fundsAvailable;
  }

  public void setFundsAvailable(Boolean fundsAvailable) {
    this.fundsAvailable = fundsAvailable;
  }

  public BulkPaymentExtendedStatusResponse200Json originalTransactionInformationAndStatus(OriginalTransactionInformationAndStatus originalTransactionInformationAndStatus) {
    this.originalTransactionInformationAndStatus = originalTransactionInformationAndStatus;
    return this;
  }

  /**
   * Get originalTransactionInformationAndStatus
   * @return originalTransactionInformationAndStatus
   **/
  @Schema(description = "")
  @JsonProperty("originalTransactionInformationAndStatus")

  @Valid
  public OriginalTransactionInformationAndStatus getOriginalTransactionInformationAndStatus() {
    return originalTransactionInformationAndStatus;
  }

  public void setOriginalTransactionInformationAndStatus(OriginalTransactionInformationAndStatus originalTransactionInformationAndStatus) {
    this.originalTransactionInformationAndStatus = originalTransactionInformationAndStatus;
  }

  public BulkPaymentExtendedStatusResponse200Json transactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
    return this;
  }

  /**
   * Get transactionFees
   * @return transactionFees
   **/
  @Schema(description = "")
  @JsonProperty("transactionFees")

  @Valid
  public Amount getTransactionFees() {
    return transactionFees;
  }

  public void setTransactionFees(Amount transactionFees) {
    this.transactionFees = transactionFees;
  }

  public BulkPaymentExtendedStatusResponse200Json currencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
    return this;
  }

  /**
   * Get currencyConversionFees
   * @return currencyConversionFees
   **/
  @Schema(description = "")
  @JsonProperty("currencyConversionFees")

  @Valid
  public Amount getCurrencyConversionFees() {
    return currencyConversionFees;
  }

  public void setCurrencyConversionFees(Amount currencyConversionFees) {
    this.currencyConversionFees = currencyConversionFees;
  }

  public BulkPaymentExtendedStatusResponse200Json estimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
    return this;
  }

  /**
   * Get estimatedTotalAmount
   * @return estimatedTotalAmount
   **/
  @Schema(description = "")
  @JsonProperty("estimatedTotalAmount")

  @Valid
  public Amount getEstimatedTotalAmount() {
    return estimatedTotalAmount;
  }

  public void setEstimatedTotalAmount(Amount estimatedTotalAmount) {
    this.estimatedTotalAmount = estimatedTotalAmount;
  }

  public BulkPaymentExtendedStatusResponse200Json estimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
    return this;
  }

  /**
   * Get estimatedInterbankSettlementAmount
   * @return estimatedInterbankSettlementAmount
   **/
  @Schema(description = "")
  @JsonProperty("estimatedInterbankSettlementAmount")

  @Valid
  public Amount getEstimatedInterbankSettlementAmount() {
    return estimatedInterbankSettlementAmount;
  }

  public void setEstimatedInterbankSettlementAmount(Amount estimatedInterbankSettlementAmount) {
    this.estimatedInterbankSettlementAmount = estimatedInterbankSettlementAmount;
  }

  public BulkPaymentExtendedStatusResponse200Json psuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
    return this;
  }

  /**
   * Get psuMessage
   * @return psuMessage
   **/
  @Schema(description = "")
  @JsonProperty("psuMessage")

  @Valid
  public String getPsuMessage() {
    return psuMessage;
  }

  public void setPsuMessage(String psuMessage) {
    this.psuMessage = psuMessage;
  }

  public BulkPaymentExtendedStatusResponse200Json ownerNames(List<AccountOwner> ownerNames) {
    this.ownerNames = ownerNames;
    return this;
  }

  public BulkPaymentExtendedStatusResponse200Json addOwnerNamesItem(AccountOwner ownerNamesItem) {
    if (this.ownerNames == null) {
      this.ownerNames = new ArrayList<>();
    }
    this.ownerNames.add(ownerNamesItem);
    return this;
  }

  /**
   * List of owner names. Should only be delivered after successful SCA. Could be restricted to the current PSU by the ASPSP.
   * @return ownerNames
   **/
  @Schema(description = "List of owner names. Should only be delivered after successful SCA. Could be restricted to the current PSU by the ASPSP. ")
  @JsonProperty("ownerNames")
    @Valid
  public List<AccountOwner> getOwnerNames() {
    return ownerNames;
  }

  public void setOwnerNames(List<AccountOwner> ownerNames) {
    this.ownerNames = ownerNames;
  }

  public BulkPaymentExtendedStatusResponse200Json _links(Map _links) {
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

  public BulkPaymentExtendedStatusResponse200Json apiClientMessages(List<TppMessageGeneric> apiClientMessages) {
    this.apiClientMessages = apiClientMessages;
    return this;
  }

  public BulkPaymentExtendedStatusResponse200Json addApiClientMessagesItem(TppMessageGeneric apiClientMessagesItem) {
    if (this.apiClientMessages == null) {
      this.apiClientMessages = new ArrayList<>();
    }
    this.apiClientMessages.add(apiClientMessagesItem);
    return this;
  }

  /**
   * Messages to the TPP on operational issues.
   * @return apiClientMessages
   **/
  @Schema(description = "Messages to the TPP on operational issues.")
  @JsonProperty("apiClientMessages")
    @Valid
  public List<TppMessageGeneric> getApiClientMessages() {
    return apiClientMessages;
  }

  public void setApiClientMessages(List<TppMessageGeneric> apiClientMessages) {
    this.apiClientMessages = apiClientMessages;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BulkPaymentExtendedStatusResponse200Json bulkPaymentExtendedStatusResponse200Json = (BulkPaymentExtendedStatusResponse200Json) o;
    return Objects.equals(this.groupStatus, bulkPaymentExtendedStatusResponse200Json.groupStatus) &&
        Objects.equals(this.reasonCode, bulkPaymentExtendedStatusResponse200Json.reasonCode) &&
        Objects.equals(this.reasonPropietary, bulkPaymentExtendedStatusResponse200Json.reasonPropietary) &&
        Objects.equals(this.fundsAvailable, bulkPaymentExtendedStatusResponse200Json.fundsAvailable) &&
        Objects.equals(this.originalTransactionInformationAndStatus, bulkPaymentExtendedStatusResponse200Json.originalTransactionInformationAndStatus) &&
        Objects.equals(this.transactionFees, bulkPaymentExtendedStatusResponse200Json.transactionFees) &&
        Objects.equals(this.currencyConversionFees, bulkPaymentExtendedStatusResponse200Json.currencyConversionFees) &&
        Objects.equals(this.estimatedTotalAmount, bulkPaymentExtendedStatusResponse200Json.estimatedTotalAmount) &&
        Objects.equals(this.estimatedInterbankSettlementAmount, bulkPaymentExtendedStatusResponse200Json.estimatedInterbankSettlementAmount) &&
        Objects.equals(this.psuMessage, bulkPaymentExtendedStatusResponse200Json.psuMessage) &&
        Objects.equals(this.ownerNames, bulkPaymentExtendedStatusResponse200Json.ownerNames) &&
        Objects.equals(this._links, bulkPaymentExtendedStatusResponse200Json._links) &&
        Objects.equals(this.apiClientMessages, bulkPaymentExtendedStatusResponse200Json.apiClientMessages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(groupStatus, reasonCode, reasonPropietary, fundsAvailable, originalTransactionInformationAndStatus, transactionFees, currencyConversionFees, estimatedTotalAmount, estimatedInterbankSettlementAmount, psuMessage, ownerNames, _links, apiClientMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BulkPaymentExtendedStatusResponse200Json {\n");

    sb.append("    groupStatus: ").append(toIndentedString(groupStatus)).append("\n");
    sb.append("    reasonCode: ").append(toIndentedString(reasonCode)).append("\n");
    sb.append("    reasonPropietary: ").append(toIndentedString(reasonPropietary)).append("\n");
    sb.append("    fundsAvailable: ").append(toIndentedString(fundsAvailable)).append("\n");
    sb.append("    originalTransactionInformationAndStatus: ").append(toIndentedString(originalTransactionInformationAndStatus)).append("\n");
    sb.append("    transactionFees: ").append(toIndentedString(transactionFees)).append("\n");
    sb.append("    currencyConversionFees: ").append(toIndentedString(currencyConversionFees)).append("\n");
    sb.append("    estimatedTotalAmount: ").append(toIndentedString(estimatedTotalAmount)).append("\n");
    sb.append("    estimatedInterbankSettlementAmount: ").append(toIndentedString(estimatedInterbankSettlementAmount)).append("\n");
    sb.append("    psuMessage: ").append(toIndentedString(psuMessage)).append("\n");
    sb.append("    ownerNames: ").append(toIndentedString(ownerNames)).append("\n");
    sb.append("    _links: ").append(toIndentedString(_links)).append("\n");
    sb.append("    apiClientMessages: ").append(toIndentedString(apiClientMessages)).append("\n");
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
