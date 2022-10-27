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
 * Generic Body for a bulk payment initation via JSON.  paymentInformationId is contained in code but commented since it is n.a.  and not all ASPSP are able to support this field now. In a later version the field will be mandatory.
 */
@Schema(description = "Generic Body for a bulk payment initation via JSON.  paymentInformationId is contained in code but commented since it is n.a.  and not all ASPSP are able to support this field now. In a later version the field will be mandatory. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class BulkPaymentInitiationJson   {
  @JsonProperty("batchBookingPreferred")
  private Boolean batchBookingPreferred = null;

  @JsonProperty("debtorAccount")
  private AccountReference debtorAccount = null;

  @JsonProperty("requestedExecutionDate")
  private LocalDate requestedExecutionDate = null;

  @JsonProperty("requestedExecutionTime")
  private OffsetDateTime requestedExecutionTime = null;

  @JsonProperty("payments")
  @Valid
  private List<PaymentInitiationBulkElementJson> payments = new ArrayList<>();

  @JsonProperty("debtorName")
  private String debtorName = null;

  public BulkPaymentInitiationJson batchBookingPreferred(Boolean batchBookingPreferred) {
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

  public BulkPaymentInitiationJson debtorAccount(AccountReference debtorAccount) {
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

  public BulkPaymentInitiationJson requestedExecutionDate(LocalDate requestedExecutionDate) {
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

  public BulkPaymentInitiationJson requestedExecutionTime(OffsetDateTime requestedExecutionTime) {
    this.requestedExecutionTime = requestedExecutionTime;
      return this;
  }

    /**
     * Get requestedExecutionTime
     *
     * @return requestedExecutionTime
     **/
    @Schema(description = "")
    @JsonProperty("requestedExecutionTime")

    @Valid
    public OffsetDateTime getRequestedExecutionTime() {
    return requestedExecutionTime;
  }

  public void setRequestedExecutionTime(OffsetDateTime requestedExecutionTime) {
    this.requestedExecutionTime = requestedExecutionTime;
  }

  public BulkPaymentInitiationJson payments(List<PaymentInitiationBulkElementJson> payments) {
    this.payments = payments;
    return this;
  }

  public BulkPaymentInitiationJson addPaymentsItem(PaymentInitiationBulkElementJson paymentsItem) {
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

  public BulkPaymentInitiationJson debtorName(String debtorName) {
      this.debtorName = debtorName;
      return this;
  }

    /**
     * Debtor name.
     *
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


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BulkPaymentInitiationJson bulkPaymentInitiationJson = (BulkPaymentInitiationJson) o;
    return Objects.equals(this.batchBookingPreferred, bulkPaymentInitiationJson.batchBookingPreferred) &&
        Objects.equals(this.debtorAccount, bulkPaymentInitiationJson.debtorAccount) &&
        Objects.equals(this.requestedExecutionDate, bulkPaymentInitiationJson.requestedExecutionDate) &&
        Objects.equals(this.requestedExecutionTime, bulkPaymentInitiationJson.requestedExecutionTime) &&
        Objects.equals(this.payments, bulkPaymentInitiationJson.payments) &&
        Objects.equals(this.debtorName, bulkPaymentInitiationJson.debtorName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(batchBookingPreferred, debtorAccount, requestedExecutionDate, requestedExecutionTime, payments, debtorName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BulkPaymentInitiationJson {\n");

    sb.append("    batchBookingPreferred: ").append(toIndentedString(batchBookingPreferred)).append("\n");
    sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
    sb.append("    requestedExecutionDate: ").append(toIndentedString(requestedExecutionDate)).append("\n");
    sb.append("    requestedExecutionTime: ").append(toIndentedString(requestedExecutionTime)).append("\n");
    sb.append("    payments: ").append(toIndentedString(payments)).append("\n");
    sb.append("    debtorName: ").append(toIndentedString(debtorName)).append("\n");
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
