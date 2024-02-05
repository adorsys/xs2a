/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * Trusted Beneficiary
 */
@Schema(description = "Trusted Beneficiary")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-05-06T12:59:08.054254+03:00[Europe/Kiev]")


public class TrustedBeneficiary   {
  @JsonProperty("trustedBeneficiaryId")
  private String trustedBeneficiaryId = null;

  @JsonProperty("debtorAccount")
  private AccountReference debtorAccount = null;

  @JsonProperty("creditorAccount")
  private AccountReference creditorAccount = null;

  @JsonProperty("creditorAgent")
  private String creditorAgent = null;

  @JsonProperty("creditorName")
  private String creditorName = null;

  @JsonProperty("creditorAlias")
  private String creditorAlias = null;

  @JsonProperty("creditorId")
  private String creditorId = null;

  @JsonProperty("creditorAddress")
  private Address creditorAddress = null;

  public TrustedBeneficiary trustedBeneficiaryId(String trustedBeneficiaryId) {
    this.trustedBeneficiaryId = trustedBeneficiaryId;
    return this;
  }

  /**
   * Resource identification of the list entry.
   * @return trustedBeneficiaryId
   **/
  @Schema(example = "74a55404-4ad0-4432-bcf4-93fb94b81e94", required = true, description = "Resource identification of the list entry. ")
      @NotNull

  @Pattern(regexp="[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")   public String getTrustedBeneficiaryId() {
    return trustedBeneficiaryId;
  }

  public void setTrustedBeneficiaryId(String trustedBeneficiaryId) {
    this.trustedBeneficiaryId = trustedBeneficiaryId;
  }

  public TrustedBeneficiary debtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
    return this;
  }

  /**
   * Get debtorAccount
   * @return debtorAccount
   **/
  @Schema(description = "")

    @Valid
    public AccountReference getDebtorAccount() {
    return debtorAccount;
  }

  public void setDebtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
  }

  public TrustedBeneficiary creditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
    return this;
  }

  /**
   * Get creditorAccount
   * @return creditorAccount
   **/
  @Schema(required = true, description = "")
      @NotNull

    @Valid
    public AccountReference getCreditorAccount() {
    return creditorAccount;
  }

  public void setCreditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
  }

  public TrustedBeneficiary creditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
    return this;
  }

  /**
   * BICFI
   * @return creditorAgent
   **/
  @Schema(example = "AAAADEBBXXX", description = "BICFI ")

  @Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")   public String getCreditorAgent() {
    return creditorAgent;
  }

  public void setCreditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
  }

  public TrustedBeneficiary creditorName(String creditorName) {
    this.creditorName = creditorName;
    return this;
  }

  /**
   * Creditor name.
   * @return creditorName
   **/
  @Schema(example = "Creditor Name", required = true, description = "Creditor name.")
      @NotNull

  @Size(max=70)   public String getCreditorName() {
    return creditorName;
  }

  public void setCreditorName(String creditorName) {
    this.creditorName = creditorName;
  }

  public TrustedBeneficiary creditorAlias(String creditorAlias) {
    this.creditorAlias = creditorAlias;
    return this;
  }

  /**
   * An alias for the creditor as defined by the PSU as an alias when displaying the list of trusted beneficiaries in online channels of the ASPSP.
   * @return creditorAlias
   **/
  @Schema(example = "Creditor Alias", description = "An alias for the creditor as defined by the PSU as an alias when displaying the list of trusted beneficiaries in online channels of the ASPSP.")

  @Size(max=70)   public String getCreditorAlias() {
    return creditorAlias;
  }

  public void setCreditorAlias(String creditorAlias) {
    this.creditorAlias = creditorAlias;
  }

  public TrustedBeneficiary creditorId(String creditorId) {
    this.creditorId = creditorId;
    return this;
  }

  /**
   * Identification of Creditors, e.g. a SEPA Creditor ID.
   * @return creditorId
   **/
  @Schema(example = "Creditor Id 5678", description = "Identification of Creditors, e.g. a SEPA Creditor ID.")

  @Size(max=35)   public String getCreditorId() {
    return creditorId;
  }

  public void setCreditorId(String creditorId) {
    this.creditorId = creditorId;
  }

  public TrustedBeneficiary creditorAddress(Address creditorAddress) {
    this.creditorAddress = creditorAddress;
    return this;
  }

  /**
   * Get creditorAddress
   * @return creditorAddress
   **/
  @Schema(description = "")

    @Valid
    public Address getCreditorAddress() {
    return creditorAddress;
  }

  public void setCreditorAddress(Address creditorAddress) {
    this.creditorAddress = creditorAddress;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TrustedBeneficiary trustedBeneficiary = (TrustedBeneficiary) o;
    return Objects.equals(this.trustedBeneficiaryId, trustedBeneficiary.trustedBeneficiaryId) &&
        Objects.equals(this.debtorAccount, trustedBeneficiary.debtorAccount) &&
        Objects.equals(this.creditorAccount, trustedBeneficiary.creditorAccount) &&
        Objects.equals(this.creditorAgent, trustedBeneficiary.creditorAgent) &&
        Objects.equals(this.creditorName, trustedBeneficiary.creditorName) &&
        Objects.equals(this.creditorAlias, trustedBeneficiary.creditorAlias) &&
        Objects.equals(this.creditorId, trustedBeneficiary.creditorId) &&
        Objects.equals(this.creditorAddress, trustedBeneficiary.creditorAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trustedBeneficiaryId, debtorAccount, creditorAccount, creditorAgent, creditorName, creditorAlias, creditorId, creditorAddress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TrustedBeneficiary {\n");

    sb.append("    trustedBeneficiaryId: ").append(toIndentedString(trustedBeneficiaryId)).append("\n");
    sb.append("    debtorAccount: ").append(toIndentedString(debtorAccount)).append("\n");
    sb.append("    creditorAccount: ").append(toIndentedString(creditorAccount)).append("\n");
    sb.append("    creditorAgent: ").append(toIndentedString(creditorAgent)).append("\n");
    sb.append("    creditorName: ").append(toIndentedString(creditorName)).append("\n");
    sb.append("    creditorAlias: ").append(toIndentedString(creditorAlias)).append("\n");
    sb.append("    creditorId: ").append(toIndentedString(creditorId)).append("\n");
    sb.append("    creditorAddress: ").append(toIndentedString(creditorAddress)).append("\n");
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
