package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.PaymentInitiationBulkElementJson;
import de.adorsys.psd2.model.TransactionStatus;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Generic JSON response body consistion of the corresponding bulk payment initation JSON body together with an optional transaction status field.
 */
@ApiModel(description = "Generic JSON response body consistion of the corresponding bulk payment initation JSON body together with an optional transaction status field. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-08-31T16:39:54.348465+03:00[Europe/Kiev]")

public class BulkPaymentInitiationWithStatusResponse   {
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

  public BulkPaymentInitiationWithStatusResponse batchBookingPreferred(Boolean batchBookingPreferred) {
    this.batchBookingPreferred = batchBookingPreferred;
    return this;
  }

  /**
   * Get batchBookingPreferred
   * @return batchBookingPreferred
  **/
  @ApiModelProperty(value = "")



  @JsonProperty("batchBookingPreferred")
  public Boolean getBatchBookingPreferred() {
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
   * @return requestedExecutionDate
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("requestedExecutionDate")
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
   * @return acceptorTransactionDateTime
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("acceptorTransactionDateTime")
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

  public BulkPaymentInitiationWithStatusResponse paymentInformationId(String paymentInformationId) {
    this.paymentInformationId = paymentInformationId;
    return this;
  }

  /**
   * Get paymentInformationId
   * @return paymentInformationId
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("paymentInformationId")
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
   * @return payments
  **/
  @ApiModelProperty(required = true, value = "A list of generic JSON bodies payment initations for bulk payments via JSON.  Note: Some fields from single payments do not occcur in a bulk payment element ")
  @NotNull

  @Valid


  @JsonProperty("payments")
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
}    BulkPaymentInitiationWithStatusResponse bulkPaymentInitiationWithStatusResponse = (BulkPaymentInitiationWithStatusResponse) o;
    return Objects.equals(this.batchBookingPreferred, bulkPaymentInitiationWithStatusResponse.batchBookingPreferred) &&
    Objects.equals(this.requestedExecutionDate, bulkPaymentInitiationWithStatusResponse.requestedExecutionDate) &&
    Objects.equals(this.acceptorTransactionDateTime, bulkPaymentInitiationWithStatusResponse.acceptorTransactionDateTime) &&
    Objects.equals(this.debtorAccount, bulkPaymentInitiationWithStatusResponse.debtorAccount) &&
    Objects.equals(this.paymentInformationId, bulkPaymentInitiationWithStatusResponse.paymentInformationId) &&
    Objects.equals(this.payments, bulkPaymentInitiationWithStatusResponse.payments) &&
    Objects.equals(this.transactionStatus, bulkPaymentInitiationWithStatusResponse.transactionStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(batchBookingPreferred, requestedExecutionDate, acceptorTransactionDateTime, debtorAccount, paymentInformationId, payments, transactionStatus);
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

