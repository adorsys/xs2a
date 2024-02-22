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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Optional if supported by API provider.  Is asking for additional information as added within this structured object. The usage of this data element requires at least one of the entries \&quot;accounts\&quot;,  \&quot;transactions\&quot; or \&quot;balances\&quot; also to be contained in the object.  If detailed accounts are referenced, it is required in addition that any account addressed within  the additionalInformation attribute is also addressed by at least one of the attributes \&quot;accounts\&quot;,  \&quot;transactions\&quot; or \&quot;balances\&quot;.
 */
@Schema(description = "Optional if supported by API provider.  Is asking for additional information as added within this structured object. The usage of this data element requires at least one of the entries \"accounts\",  \"transactions\" or \"balances\" also to be contained in the object.  If detailed accounts are referenced, it is required in addition that any account addressed within  the additionalInformation attribute is also addressed by at least one of the attributes \"accounts\",  \"transactions\" or \"balances\". ")
@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2022-10-26T13:16:54.081225+03:00[Europe/Kiev]")


public class AdditionalInformationAccess   {
  @JsonProperty("ownerName")
  @Valid
  private List<AccountReference> ownerName = null;

  @JsonProperty("trustedBeneficiaries")
  @Valid
  private List<AccountReference> trustedBeneficiaries = null;

  public AdditionalInformationAccess ownerName(List<AccountReference> ownerName) {
    this.ownerName = ownerName;
    return this;
  }

  public AdditionalInformationAccess addOwnerNameItem(AccountReference ownerNameItem) {
    if (this.ownerName == null) {
      this.ownerName = new ArrayList<>();
    }
    this.ownerName.add(ownerNameItem);
    return this;
  }

    /**
     * Is asking for account owner name of the accounts referenced within.  If the array is empty in the request, the TPP is asking for the account  owner name of all accessible accounts.  This may be restricted in a PSU/ASPSP authorization dialogue.  If the array is empty, also the arrays for accounts, balances or transactions shall be empty, if used. The ASPSP will indicate in the consent resource after a successful authorisation,  whether the ownerName consent can be accepted by providing the accounts on which the ownerName will  be delivered.  This array can be empty.
     *
     * @return ownerName
     **/
    @Schema(description = "Is asking for account owner name of the accounts referenced within.  If the array is empty in the request, the TPP is asking for the account  owner name of all accessible accounts.  This may be restricted in a PSU/ASPSP authorization dialogue.  If the array is empty, also the arrays for accounts, balances or transactions shall be empty, if used. The ASPSP will indicate in the consent resource after a successful authorisation,  whether the ownerName consent can be accepted by providing the accounts on which the ownerName will  be delivered.  This array can be empty. ")
    @JsonProperty("ownerName")
    @Valid
    public List<AccountReference> getOwnerName() {
        return ownerName;
    }

  public void setOwnerName(List<AccountReference> ownerName) {
    this.ownerName = ownerName;
  }

  public AdditionalInformationAccess trustedBeneficiaries(List<AccountReference> trustedBeneficiaries) {
    this.trustedBeneficiaries = trustedBeneficiaries;
    return this;
  }

  public AdditionalInformationAccess addTrustedBeneficiariesItem(AccountReference trustedBeneficiariesItem) {
    if (this.trustedBeneficiaries == null) {
      this.trustedBeneficiaries = new ArrayList<>();
    }
    this.trustedBeneficiaries.add(trustedBeneficiariesItem);
    return this;
  }

    /**
     * Optional if supported by API provider. Is asking for the trusted beneficiaries related to the accounts referenced within and related to the PSU. If the array is empty in the request, the TPP is asking for the lists of trusted beneficiaries of all accessible accounts.  This may be restricted in a PSU/ASPSP authorization dialogue by the PSU if also the account lists addressed  by the tags “accounts”, “balances” or “transactions” are empty. The ASPSP will indicate in the consent resource after a successful authorisation,  whether the trustedBeneficiaries consent can be accepted by providing the accounts on which the list of trusted beneficiaries will be delivered.  This array can be empty.
     *
     * @return trustedBeneficiaries
     **/
    @Schema(description = "Optional if supported by API provider. Is asking for the trusted beneficiaries related to the accounts referenced within and related to the PSU. If the array is empty in the request, the TPP is asking for the lists of trusted beneficiaries of all accessible accounts.  This may be restricted in a PSU/ASPSP authorization dialogue by the PSU if also the account lists addressed  by the tags “accounts”, “balances” or “transactions” are empty. The ASPSP will indicate in the consent resource after a successful authorisation,  whether the trustedBeneficiaries consent can be accepted by providing the accounts on which the list of trusted beneficiaries will be delivered.  This array can be empty. ")
    @JsonProperty("trustedBeneficiaries")
    @Valid
    public List<AccountReference> getTrustedBeneficiaries() {
        return trustedBeneficiaries;
  }

  public void setTrustedBeneficiaries(List<AccountReference> trustedBeneficiaries) {
    this.trustedBeneficiaries = trustedBeneficiaries;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdditionalInformationAccess additionalInformationAccess = (AdditionalInformationAccess) o;
    return Objects.equals(this.ownerName, additionalInformationAccess.ownerName) &&
        Objects.equals(this.trustedBeneficiaries, additionalInformationAccess.trustedBeneficiaries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ownerName, trustedBeneficiaries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdditionalInformationAccess {\n");

    sb.append("    ownerName: ").append(toIndentedString(ownerName)).append("\n");
    sb.append("    trustedBeneficiaries: ").append(toIndentedString(trustedBeneficiaries)).append("\n");
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
