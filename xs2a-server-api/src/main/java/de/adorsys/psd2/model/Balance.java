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
import java.util.Objects;

/**
 * A single balance element.
 */
@Schema(description = "A single balance element. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class Balance   {
  @JsonProperty("balanceAmount")
  private Amount balanceAmount = null;

  @JsonProperty("balanceType")
  private BalanceType balanceType = null;

  @JsonProperty("creditLimitIncluded")
  private Boolean creditLimitIncluded = null;

  @JsonProperty("lastChangeDateTime")
  private OffsetDateTime lastChangeDateTime = null;

  @JsonProperty("referenceDate")
  private LocalDate referenceDate = null;

  @JsonProperty("lastCommittedTransaction")
  private String lastCommittedTransaction = null;

  public Balance balanceAmount(Amount balanceAmount) {
    this.balanceAmount = balanceAmount;
    return this;
  }

    /**
     * Get balanceAmount
     *
     * @return balanceAmount
     **/
    @Schema(required = true, description = "")
    @JsonProperty("balanceAmount")
    @NotNull

    @Valid
    public Amount getBalanceAmount() {
        return balanceAmount;
    }

  public void setBalanceAmount(Amount balanceAmount) {
    this.balanceAmount = balanceAmount;
  }

  public Balance balanceType(BalanceType balanceType) {
    this.balanceType = balanceType;
    return this;
  }

    /**
     * Get balanceType
     *
     * @return balanceType
     **/
    @Schema(required = true, description = "")
    @JsonProperty("balanceType")
    @NotNull

    @Valid
    public BalanceType getBalanceType() {
        return balanceType;
  }

  public void setBalanceType(BalanceType balanceType) {
    this.balanceType = balanceType;
  }

  public Balance creditLimitIncluded(Boolean creditLimitIncluded) {
    this.creditLimitIncluded = creditLimitIncluded;
      return this;
  }

    /**
     * A flag indicating if the credit limit of the corresponding account  is included in the calculation of the balance, where applicable.
     *
     * @return creditLimitIncluded
     **/
    @Schema(example = "false", description = "A flag indicating if the credit limit of the corresponding account  is included in the calculation of the balance, where applicable. ")
    @JsonProperty("creditLimitIncluded")

    public Boolean isCreditLimitIncluded() {
        return creditLimitIncluded;
  }

  public void setCreditLimitIncluded(Boolean creditLimitIncluded) {
    this.creditLimitIncluded = creditLimitIncluded;
  }

  public Balance lastChangeDateTime(OffsetDateTime lastChangeDateTime) {
    this.lastChangeDateTime = lastChangeDateTime;
      return this;
  }

    /**
     * This data element might be used to indicate e.g. with the expected or booked balance that no action is known  on the account, which is not yet booked.
     *
     * @return lastChangeDateTime
     **/
    @Schema(description = "This data element might be used to indicate e.g. with the expected or booked balance that no action is known  on the account, which is not yet booked. ")
    @JsonProperty("lastChangeDateTime")

    @Valid
    public OffsetDateTime getLastChangeDateTime() {
    return lastChangeDateTime;
  }

  public void setLastChangeDateTime(OffsetDateTime lastChangeDateTime) {
    this.lastChangeDateTime = lastChangeDateTime;
  }

  public Balance referenceDate(LocalDate referenceDate) {
    this.referenceDate = referenceDate;
      return this;
  }

    /**
     * Indicates the date of the balance.
     *
     * @return referenceDate
     **/
    @Schema(description = "Indicates the date of the balance.")
    @JsonProperty("referenceDate")

    @Valid
    public LocalDate getReferenceDate() {
    return referenceDate;
  }

  public void setReferenceDate(LocalDate referenceDate) {
    this.referenceDate = referenceDate;
  }

  public Balance lastCommittedTransaction(String lastCommittedTransaction) {
    this.lastCommittedTransaction = lastCommittedTransaction;
      return this;
  }

    /**
     * \"entryReference\" of the last commited transaction to support the TPP in identifying whether all  PSU transactions are already known.
     *
     * @return lastCommittedTransaction
     **/
    @Schema(description = "\"entryReference\" of the last commited transaction to support the TPP in identifying whether all  PSU transactions are already known. ")
    @JsonProperty("lastCommittedTransaction")

    @Size(max = 35)
    public String getLastCommittedTransaction() {
    return lastCommittedTransaction;
  }

  public void setLastCommittedTransaction(String lastCommittedTransaction) {
    this.lastCommittedTransaction = lastCommittedTransaction;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Balance balance = (Balance) o;
    return Objects.equals(this.balanceAmount, balance.balanceAmount) &&
        Objects.equals(this.balanceType, balance.balanceType) &&
        Objects.equals(this.creditLimitIncluded, balance.creditLimitIncluded) &&
        Objects.equals(this.lastChangeDateTime, balance.lastChangeDateTime) &&
        Objects.equals(this.referenceDate, balance.referenceDate) &&
        Objects.equals(this.lastCommittedTransaction, balance.lastCommittedTransaction);
  }

  @Override
  public int hashCode() {
    return Objects.hash(balanceAmount, balanceType, creditLimitIncluded, lastChangeDateTime, referenceDate, lastCommittedTransaction);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Balance {\n");

    sb.append("    balanceAmount: ").append(toIndentedString(balanceAmount)).append("\n");
    sb.append("    balanceType: ").append(toIndentedString(balanceType)).append("\n");
    sb.append("    creditLimitIncluded: ").append(toIndentedString(creditLimitIncluded)).append("\n");
    sb.append("    lastChangeDateTime: ").append(toIndentedString(lastChangeDateTime)).append("\n");
    sb.append("    referenceDate: ").append(toIndentedString(referenceDate)).append("\n");
    sb.append("    lastCommittedTransaction: ").append(toIndentedString(lastCommittedTransaction)).append("\n");
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
