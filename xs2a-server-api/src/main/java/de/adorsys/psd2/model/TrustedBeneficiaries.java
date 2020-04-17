package de.adorsys.psd2.model;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.Address;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * Requested trusted beneficiaries services for a consent.
 */
@ApiModel(description = "Requested trusted beneficiaries services for a consent. ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-04-15T15:50:07.478677+03:00[Europe/Kiev]")

public class TrustedBeneficiaries   {
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

  public TrustedBeneficiaries trustedBeneficiaryId(String trustedBeneficiaryId) {
    this.trustedBeneficiaryId = trustedBeneficiaryId;
    return this;
  }

  /**
   * Resource identification of the list entry.
   * @return trustedBeneficiaryId
  **/
  @ApiModelProperty(required = true, value = "Resource identification of the list entry.")
  @NotNull



  @JsonProperty("trustedBeneficiaryId")
  public String getTrustedBeneficiaryId() {
    return trustedBeneficiaryId;
  }

  public void setTrustedBeneficiaryId(String trustedBeneficiaryId) {
    this.trustedBeneficiaryId = trustedBeneficiaryId;
  }

  public TrustedBeneficiaries debtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
    return this;
  }

  /**
   * Get debtorAccount
   * @return debtorAccount
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("debtorAccount")
  public AccountReference getDebtorAccount() {
    return debtorAccount;
  }

  public void setDebtorAccount(AccountReference debtorAccount) {
    this.debtorAccount = debtorAccount;
  }

  public TrustedBeneficiaries creditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
    return this;
  }

  /**
   * Get creditorAccount
   * @return creditorAccount
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

  @Valid


  @JsonProperty("creditorAccount")
  public AccountReference getCreditorAccount() {
    return creditorAccount;
  }

  public void setCreditorAccount(AccountReference creditorAccount) {
    this.creditorAccount = creditorAccount;
  }

  public TrustedBeneficiaries creditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
    return this;
  }

  /**
   * It is mandated where the information is mandated for related credit transfers.
   * @return creditorAgent
  **/
  @ApiModelProperty(value = "It is mandated where the information is mandated for related credit transfers.")



  @JsonProperty("creditorAgent")
  public String getCreditorAgent() {
    return creditorAgent;
  }

  public void setCreditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
  }

  public TrustedBeneficiaries creditorName(String creditorName) {
    this.creditorName = creditorName;
    return this;
  }

  /**
   * Name of the creditor as provided by the PSU.
   * @return creditorName
  **/
  @ApiModelProperty(required = true, value = "Name of the creditor as provided by the PSU.")
  @NotNull

@Size(max=70)

  @JsonProperty("creditorName")
  public String getCreditorName() {
    return creditorName;
  }

  public void setCreditorName(String creditorName) {
    this.creditorName = creditorName;
  }

  public TrustedBeneficiaries creditorAlias(String creditorAlias) {
    this.creditorAlias = creditorAlias;
    return this;
  }

  /**
   * An alias for the creditor as defined by the PSU as an alias when displaying the list of trusted beneficiaries in online channels of the ASPSP.
   * @return creditorAlias
  **/
  @ApiModelProperty(value = "An alias for the creditor as defined by the PSU as an alias when displaying the list of trusted beneficiaries in online channels of the ASPSP.")

@Size(max=70)

  @JsonProperty("creditorAlias")
  public String getCreditorAlias() {
    return creditorAlias;
  }

  public void setCreditorAlias(String creditorAlias) {
    this.creditorAlias = creditorAlias;
  }

  public TrustedBeneficiaries creditorId(String creditorId) {
    this.creditorId = creditorId;
    return this;
  }

  /**
   * Identification of Creditors.
   * @return creditorId
  **/
  @ApiModelProperty(value = "Identification of Creditors.")

@Size(max=35)

  @JsonProperty("creditorId")
  public String getCreditorId() {
    return creditorId;
  }

  public void setCreditorId(String creditorId) {
    this.creditorId = creditorId;
  }

  public TrustedBeneficiaries creditorAddress(Address creditorAddress) {
    this.creditorAddress = creditorAddress;
    return this;
  }

  /**
   * Get creditorAddress
   * @return creditorAddress
  **/
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("creditorAddress")
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
}    TrustedBeneficiaries trustedBeneficiaries = (TrustedBeneficiaries) o;
    return Objects.equals(this.trustedBeneficiaryId, trustedBeneficiaries.trustedBeneficiaryId) &&
    Objects.equals(this.debtorAccount, trustedBeneficiaries.debtorAccount) &&
    Objects.equals(this.creditorAccount, trustedBeneficiaries.creditorAccount) &&
    Objects.equals(this.creditorAgent, trustedBeneficiaries.creditorAgent) &&
    Objects.equals(this.creditorName, trustedBeneficiaries.creditorName) &&
    Objects.equals(this.creditorAlias, trustedBeneficiaries.creditorAlias) &&
    Objects.equals(this.creditorId, trustedBeneficiaries.creditorId) &&
    Objects.equals(this.creditorAddress, trustedBeneficiaries.creditorAddress);
  }

  @Override
  public int hashCode() {
    return Objects.hash(trustedBeneficiaryId, debtorAccount, creditorAccount, creditorAgent, creditorName, creditorAlias, creditorId, creditorAddress);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TrustedBeneficiaries {\n");

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

