package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JSON response body consistion of the corresponding bulk SCT INST payment initation JSON body together with an
 * optional transaction status field.
 */
@ApiModel(description = "JSON response body consistion of the corresponding bulk SCT INST payment initation JSON body" +
    " together with an optional transaction status field. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class BulkPaymentInitiationSctInstWithStatusResponse {
    @JsonProperty("batchBookingPreferred")
    private Boolean batchBookingPreferred = null;
    @JsonProperty("requestedExecutionDate")
    private LocalDate requestedExecutionDate = null;
    @JsonProperty("debtorAccount")
    private Object debtorAccount = null;
    @JsonProperty("payments")
    @Valid
    private List<PaymentInitiationSctInstJson> payments = new ArrayList<>();
    @JsonProperty("transactionStatus")
    private TransactionStatus transactionStatus = null;

    public BulkPaymentInitiationSctInstWithStatusResponse batchBookingPreferred(Boolean batchBookingPreferred) {
        this.batchBookingPreferred = batchBookingPreferred;
        return this;
    }

    /**
     * Get batchBookingPreferred
     *
     * @return batchBookingPreferred
     **/
    @ApiModelProperty(value = "")
    public Boolean getBatchBookingPreferred() {
        return batchBookingPreferred;
    }

    public void setBatchBookingPreferred(Boolean batchBookingPreferred) {
        this.batchBookingPreferred = batchBookingPreferred;
    }

    public BulkPaymentInitiationSctInstWithStatusResponse requestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
        return this;
    }

    /**
     * Get requestedExecutionDate
     *
     * @return requestedExecutionDate
     **/
    @ApiModelProperty(value = "")
    @Valid
    public LocalDate getRequestedExecutionDate() {
        return requestedExecutionDate;
    }

    public void setRequestedExecutionDate(LocalDate requestedExecutionDate) {
        this.requestedExecutionDate = requestedExecutionDate;
    }

    public BulkPaymentInitiationSctInstWithStatusResponse debtorAccount(Object debtorAccount) {
        this.debtorAccount = debtorAccount;
        return this;
    }

    /**
     * Get debtorAccount
     *
     * @return debtorAccount
     **/
    @ApiModelProperty(required = true, value = "")
    @NotNull
    public Object getDebtorAccount() {
        return debtorAccount;
    }

    public void setDebtorAccount(Object debtorAccount) {
        this.debtorAccount = debtorAccount;
    }

    public BulkPaymentInitiationSctInstWithStatusResponse payments(List<PaymentInitiationSctInstJson> payments) {
        this.payments = payments;
        return this;
    }

    public BulkPaymentInitiationSctInstWithStatusResponse addPaymentsItem(PaymentInitiationSctInstJson paymentsItem) {
        this.payments.add(paymentsItem);
        return this;
    }

    /**
     * A list of JSON bodies for SCT INST payments.
     *
     * @return payments
     **/
    @ApiModelProperty(required = true, value = "A list of JSON bodies for SCT INST payments.")
    @NotNull
    @Valid
    public List<PaymentInitiationSctInstJson> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentInitiationSctInstJson> payments) {
        this.payments = payments;
    }

    public BulkPaymentInitiationSctInstWithStatusResponse transactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
        return this;
    }

    /**
     * Get transactionStatus
     *
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
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BulkPaymentInitiationSctInstWithStatusResponse bulkPaymentInitiationSctInstWithStatusResponse =
            (BulkPaymentInitiationSctInstWithStatusResponse) o;
        return Objects.equals(this.batchBookingPreferred,
            bulkPaymentInitiationSctInstWithStatusResponse.batchBookingPreferred) && Objects.equals(this.requestedExecutionDate, bulkPaymentInitiationSctInstWithStatusResponse.requestedExecutionDate) && Objects.equals(this.debtorAccount, bulkPaymentInitiationSctInstWithStatusResponse.debtorAccount) && Objects.equals(this.payments, bulkPaymentInitiationSctInstWithStatusResponse.payments) && Objects.equals(this.transactionStatus, bulkPaymentInitiationSctInstWithStatusResponse.transactionStatus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchBookingPreferred, requestedExecutionDate, debtorAccount, payments, transactionStatus);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BulkPaymentInitiationSctInstWithStatusResponse {\n");
        sb.append("    batchBookingPreferred: ").append(toIndentedString(batchBookingPreferred)).append("\n");
        sb.append("    requestedExecutionDate: ").append(toIndentedString(requestedExecutionDate)).append("\n");
        sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
        sb.append("    payments: ").append(toIndentedString(payments)).append("\n");
        sb.append("    transactionStatus: ").append(toIndentedString(transactionStatus)).append("\n");
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

