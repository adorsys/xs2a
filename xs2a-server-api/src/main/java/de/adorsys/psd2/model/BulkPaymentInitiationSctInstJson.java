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
 * JSON Body for a bulk SCT INST payment initation.
 */
@ApiModel(description = "JSON Body for a bulk SCT INST payment initation. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2018-10-11T14:55" +
    ":45.627+02:00[Europe/Berlin]")
public class BulkPaymentInitiationSctInstJson {
    @JsonProperty("batchBookingPreferred")
    private Boolean batchBookingPreferred = null;
    @JsonProperty("requestedExecutionDate")
    private LocalDate requestedExecutionDate = null;
    @JsonProperty("debtorAccount")
    private Object debtorAccount = null;
    @JsonProperty("payments")
    @Valid
    private List<PaymentInitiationSctInstBulkElementJson> payments = new ArrayList<>();

    public BulkPaymentInitiationSctInstJson batchBookingPreferred(Boolean batchBookingPreferred) {
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

    public BulkPaymentInitiationSctInstJson requestedExecutionDate(LocalDate requestedExecutionDate) {
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

    public BulkPaymentInitiationSctInstJson debtorAccount(Object debtorAccount) {
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

    public BulkPaymentInitiationSctInstJson payments(List<PaymentInitiationSctInstBulkElementJson> payments) {
        this.payments = payments;
        return this;
    }

    public BulkPaymentInitiationSctInstJson addPaymentsItem(PaymentInitiationSctInstBulkElementJson paymentsItem) {
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
    public List<PaymentInitiationSctInstBulkElementJson> getPayments() {
        return payments;
    }

    public void setPayments(List<PaymentInitiationSctInstBulkElementJson> payments) {
        this.payments = payments;
    }

    @Override
    public boolean equals(java.lang.Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BulkPaymentInitiationSctInstJson bulkPaymentInitiationSctInstJson = (BulkPaymentInitiationSctInstJson) o;
        return Objects.equals(this.batchBookingPreferred, bulkPaymentInitiationSctInstJson.batchBookingPreferred) && Objects.equals(this.requestedExecutionDate, bulkPaymentInitiationSctInstJson.requestedExecutionDate) && Objects.equals(this.debtorAccount, bulkPaymentInitiationSctInstJson.debtorAccount) && Objects.equals(this.payments, bulkPaymentInitiationSctInstJson.payments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(batchBookingPreferred, requestedExecutionDate, debtorAccount, payments);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BulkPaymentInitiationSctInstJson {\n");
        sb.append("    batchBookingPreferred: ").append(toIndentedString(batchBookingPreferred)).append("\n");
        sb.append("    requestedExecutionDate: ").append(toIndentedString(requestedExecutionDate)).append("\n");
        sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
        sb.append("    payments: ").append(toIndentedString(payments)).append("\n");
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

