/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Generic JSON response body consistion of the corresponding bulk payment initation JSON body together with an optional transaction status field.
 */
@Schema(description = "Generic JSON response body consistion of the corresponding bulk payment initation JSON body together with an optional transaction status field. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class BulkPaymentInitiationWithStatusResponse {
    @JsonProperty("batchBookingPreferred")
    private Boolean batchBookingPreferred = null;

    @JsonProperty("requestedExecutionDate")
    private LocalDate requestedExecutionDate = null;

    @JsonProperty("acceptorTransactionDateTime")
    private OffsetDateTime acceptorTransactionDateTime = null;

    @JsonProperty("debtorAccount")
  private AccountReference debtorAccount = null;

  @JsonProperty("paymentInformationId")
  private String paymentInformationId = null;

  @JsonProperty("payments")
  @Valid
  private List<PaymentInitiationBulkElementJson> payments = new ArrayList<>();

  @JsonProperty("transactionStatus")
  private TransactionStatus transactionStatus = null;

  @JsonProperty("tppMessages")
  @Valid
  private List<TppMessageGeneric> tppMessages = null;

  public BulkPaymentInitiationWithStatusResponse batchBookingPreferred(Boolean batchBookingPreferred) {
    this.batchBookingPreferred = batchBookingPreferred;
    return this;
  }

    /**
     * If this element equals 'true', the PSU prefers only one booking entry.  If this element equals 'false', the PSU prefers individual booking of all contained individual transactions.   The ASPSP will follow this preference according to contracts agreed on with the PSU.
     *
     * @return batchBookingPreferred
     **/
    @Schema(example = "false", description = "If this element equals 'true', the PSU prefers only one booking entry.  If this element equals 'false', the PSU prefers individual booking of all contained individual transactions.   The ASPSP will follow this preference according to contracts agreed on with the PSU. ")
    @JsonProperty("batchBookingPreferred")

    public Boolean isBatchBookingPreferred() {
        return batchBookingPreferred;
    }

  public void setBatchBookingPreferred(Boolean batchBookingPreferred) {
    this.batchBookingPreferred = batchBookingPreferred;
  }

  public BulkPaymentInitiationWithStatusResponse requestedExecutionDate(LocalDate requestedExecutionDate) {
    this.requestedExecutionDate = requestedExecutionDate;
    return this;
  }

    /**
     * Get requestedExecutionDate
     *
     * @return requestedExecutionDate
     **/
    @Schema(description = "")
    @JsonProperty("requestedExecutionDate")

    @Valid
    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
  }

  public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
    this.requestedExecutionDate = requestedExecutionDate;
  }

  public BulkPaymentInitiationWithStatusResponse acceptorTransactionDateTime(OffsetDateTime acceptorTransactionDateTime) {
    this.acceptorTransactionDateTime = acceptorTransactionDateTime;
      return this;
  }

    /**
     * Get acceptorTransactionDateTime
     *
     * @return acceptorTransactionDateTime
     **/
    @Schema(description = "")
    @JsonProperty("acceptorTransactionDateTime")

    @Valid
    public OffsetDateTime getAcceptorTransactionDateTime() {
        return acceptorTransactionDateTime;
  }

  public void setAcceptorTransactionDateTime(OffsetDateTime acceptorTransactionDateTime) {
    this.acceptorTransactionDateTime = acceptorTransactionDateTime;
  }

  public BulkPaymentInitiationWithStatusResponse debtorAccount(AccountReference debtorAccount) {
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

  public BulkPaymentInitiationWithStatusResponse paymentInformationId(String paymentInformationId) {
    this.paymentInformationId = paymentInformationId;
      return this;
  }

    /**
     * Get paymentInformationId
     *
     * @return paymentInformationId
     **/
    @Schema(description = "")
    @JsonProperty("paymentInformationId")

    @Size(max = 35)
    public String getPaymentInformationId() {
    return paymentInformationId;
  }

  public void setPaymentInformationId(String paymentInformationId) {
    this.paymentInformationId = paymentInformationId;
  }

  public BulkPaymentInitiationWithStatusResponse payments(List<PaymentInitiationBulkElementJson> payments) {
    this.payments = payments;
    return this;
  }

  public BulkPaymentInitiationWithStatusResponse addPaymentsItem(PaymentInitiationBulkElementJson paymentsItem) {
      this.payments.add(paymentsItem);
      return this;
  }

    /**
     * A list of generic JSON bodies payment initations for bulk payments via JSON.  Note: Some fields from single payments do not occcur in a bulk payment element
     *
     * @return payments
     **/
    @Schema(required = true, description = "A list of generic JSON bodies payment initations for bulk payments via JSON.  Note: Some fields from single payments do not occcur in a bulk payment element ")
    @JsonProperty("payments")
    @NotNull
    @Valid
    public List<PaymentInitiationBulkElementJson> getPayments() {
    return payments;
  }

  public void setPayments(List<PaymentInitiationBulkElementJson> payments) {
    this.payments = payments;
  }

  public BulkPaymentInitiationWithStatusResponse transactionStatus(TransactionStatus transactionStatus) {
      this.transactionStatus = transactionStatus;
      return this;
  }

    /**
     * Get transactionStatus
     *
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

  public BulkPaymentInitiationWithStatusResponse tppMessages(List<TppMessageGeneric> tppMessages) {
    this.tppMessages = tppMessages;
    return this;
  }

  public BulkPaymentInitiationWithStatusResponse addTppMessagesItem(TppMessageGeneric tppMessagesItem) {
    if (this.tppMessages == null) {
      this.tppMessages = new ArrayList<>();
    }
      this.tppMessages.add(tppMessagesItem);
      return this;
  }

    /**
     * Messages to the TPP on operational issues.
     *
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
    BulkPaymentInitiationWithStatusResponse bulkPaymentInitiationWithStatusResponse = (BulkPaymentInitiationWithStatusResponse) o;
    return Objects.equals(this.batchBookingPreferred, bulkPaymentInitiationWithStatusResponse.batchBookingPreferred) &&
        Objects.equals(this.requestedExecutionDate, bulkPaymentInitiationWithStatusResponse.requestedExecutionDate) &&
        Objects.equals(this.acceptorTransactionDateTime, bulkPaymentInitiationWithStatusResponse.acceptorTransactionDateTime) &&
        Objects.equals(this.debtorAccount, bulkPaymentInitiationWithStatusResponse.debtorAccount) &&
        Objects.equals(this.paymentInformationId, bulkPaymentInitiationWithStatusResponse.paymentInformationId) &&
        Objects.equals(this.payments, bulkPaymentInitiationWithStatusResponse.payments) &&
        Objects.equals(this.transactionStatus, bulkPaymentInitiationWithStatusResponse.transactionStatus) &&
        Objects.equals(this.tppMessages, bulkPaymentInitiationWithStatusResponse.tppMessages);
  }

  @Override
  public int hashCode() {
    return Objects.hash(batchBookingPreferred, requestedExecutionDate, acceptorTransactionDateTime, debtorAccount, paymentInformationId, payments, transactionStatus, tppMessages);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BulkPaymentInitiationWithStatusResponse {\n");

    sb.append("    batchBookingPreferred: ").append(toIndentedString(batchBookingPreferred)).append("\n");
    sb.append("    requestedExecutionDate: ").append(toIndentedString(requestedExecutionDate)).append("\n");
    sb.append("    acceptorTransactionDateTime: ").append(toIndentedString(acceptorTransactionDateTime)).append("\n");
    sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
    sb.append("    paymentInformationId: ").append(toIndentedString(paymentInformationId)).append("\n");
    sb.append("    payments: ").append(toIndentedString(payments)).append("\n");
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
