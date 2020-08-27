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
 * Trusted Beneficiary
 */
@ApiModel(description = "Trusted Beneficiary")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2020-08-25T18:03:04.675305+03:00[Europe/Kiev]")

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
   * Get trustedBeneficiaryId
   * @return trustedBeneficiaryId
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Pattern(regexp="[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

  @JsonProperty("trustedBeneficiaryId")
  public String getTrustedBeneficiaryId() {
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
  @ApiModelProperty(value = "")

  @Valid


  @JsonProperty("debtorAccount")
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

  public TrustedBeneficiary creditorAgent(String creditorAgent) {
    this.creditorAgent = creditorAgent;
    return this;
  }

  /**
   * Get creditorAgent
   * @return creditorAgent
  **/
  @ApiModelProperty(value = "")

@Pattern(regexp="[A-Z]{6,6}[A-Z2-9][A-NP-Z0-9]([A-Z0-9]{3,3}){0,1}")

  @JsonProperty("creditorAgent")
  public String getCreditorAgent() {
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
   * Get creditorName
   * @return creditorName
  **/
  @ApiModelProperty(required = true, value = "")
  @NotNull

@Size(max=70)

  @JsonProperty("creditorName")
  public String getCreditorName() {
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
   * Get creditorAlias
   * @return creditorAlias
  **/
  @ApiModelProperty(value = "")

@Size(max=70)

  @JsonProperty("creditorAlias")
  public String getCreditorAlias() {
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
   * Get creditorId
   * @return creditorId
  **/
  @ApiModelProperty(value = "")

@Size(max=35)

  @JsonProperty("creditorId")
  public String getCreditorId() {
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
}    TrustedBeneficiary trustedBeneficiary = (TrustedBeneficiary) o;
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

