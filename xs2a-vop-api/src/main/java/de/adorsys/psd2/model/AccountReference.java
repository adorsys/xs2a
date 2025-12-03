package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;


import java.util.*;
import jakarta.annotation.Generated;

/**
 * See document \\\&quot;openFinance API Framework Data Dictionary\\\&quot;, section \\\&quot;Account Reference\\\&quot; for more details.
 */

@Schema(name = "accountReference", description = "See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Account Reference\\\" for more details. ")
@JsonTypeName("accountReference")
@SuppressWarnings("PMD")
@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public class AccountReference {

  private Optional<@Pattern(regexp = "[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}") String> iban = Optional.empty();

  private Optional<@Pattern(regexp = "[a-zA-Z0-9]{1,30}") String> bban = Optional.empty();

  private Optional<@Size(max = 35) String> pan = Optional.empty();

  private Optional<@Size(max = 35) String> maskedPan = Optional.empty();

  private Optional<@Size(max = 35) String> msisdn = Optional.empty();

  private Optional<GenericAccountIdentification> other = Optional.empty();

  private Optional<CashAccountType> typeCode = Optional.empty();

  private Optional<@Size(max = 35) String> typeProprietary = Optional.empty();

  private Optional<@Pattern(regexp = "[A-Z]{3}") String> currency = Optional.empty();

  private Optional<ProxyAccountIdentification> proxy = Optional.empty();

  private Optional<@Size(max = 70) String> name = Optional.empty();

  private Optional<PartyDescription2> owner = Optional.empty();

  private Optional<FinancialInstitutionIdentification> servicer = Optional.empty();

  public AccountReference iban(String iban) {
    this.iban = Optional.ofNullable(iban);
    return this;
  }

  /**
   *
   * @return iban
   */

  @Schema(name = "iban", example = "FR7612345987650123456789014", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("iban")
  public Optional<@Pattern(regexp = "[A-Z]{2,2}[0-9]{2,2}[a-zA-Z0-9]{1,30}") String> getIban() {
    return iban;
  }

  public void setIban(Optional<String> iban) {
    this.iban = iban;
  }

  public AccountReference bban(String bban) {
    this.bban = Optional.ofNullable(bban);
    return this;
  }

  /**
   * BBAN of the account. This data elements is used for payment accounts which have no IBAN.
   * @return bban
   */

  @Schema(name = "bban", example = "BARC12345612345678", description = "BBAN of the account. This data elements is used for payment accounts which have no IBAN. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("bban")
  public Optional<@Pattern(regexp = "[a-zA-Z0-9]{1,30}") String> getBban() {
    return bban;
  }

  public void setBban(Optional<String> bban) {
    this.bban = bban;
  }

  public AccountReference pan(String pan) {
    this.pan = Optional.ofNullable(pan);
    return this;
  }

  /**
   * Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements.
   * @return pan
   */

  @Schema(name = "pan", example = "Text, maximum of 35 characters.", description = "Primary Account Number (PAN) of a card, can be tokenised by the ASPSP due to PCI DSS requirements.  ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("pan")
  public Optional<@Size(max = 35) String> getPan() {
    return pan;
  }

  public void setPan(Optional<String> pan) {
    this.pan = pan;
  }

  public AccountReference maskedPan(String maskedPan) {
    this.maskedPan = Optional.ofNullable(maskedPan);
    return this;
  }

  /**
   * Primary Account Number (PAN) of a card in a masked form.
   * @return maskedPan
   */

  @Schema(name = "maskedPan", example = "Text, maximum of 35 characters.", description = "Primary Account Number (PAN) of a card in a masked form. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("maskedPan")
  public Optional<@Size(max = 35) String> getMaskedPan() {
    return maskedPan;
  }

  public void setMaskedPan(Optional<String> maskedPan) {
    this.maskedPan = maskedPan;
  }

  public AccountReference msisdn(String msisdn) {
    this.msisdn = Optional.ofNullable(msisdn);
    return this;
  }

  /**
   * An alias to access a payment account via a registered mobile phone number.
   * @return msisdn
   */

  @Schema(name = "msisdn", example = "Text, maximum of 35 characters.", description = "An alias to access a payment account via a registered mobile phone number. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("msisdn")
  public Optional<@Size(max = 35) String> getMsisdn() {
    return msisdn;
  }

  public void setMsisdn(Optional<String> msisdn) {
    this.msisdn = msisdn;
  }

  public AccountReference other(GenericAccountIdentification other) {
    this.other = Optional.ofNullable(other);
    return this;
  }

  /**
   * An alias with a proprietary coding
   * @return other
   */
  @Valid
  @Schema(name = "other", description = "An alias with a proprietary coding ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("other")
  public Optional<GenericAccountIdentification> getOther() {
    return other;
  }

  public void setOther(Optional<GenericAccountIdentification> other) {
    this.other = other;
  }

  public AccountReference typeCode(CashAccountType typeCode) {
    this.typeCode = Optional.ofNullable(typeCode);
    return this;
  }

  /**
   * Remark: Shall not be used in the consent model, since account category provides enough information for the ASPSP.
   * @return typeCode
   */
  @Valid
  @Schema(name = "typeCode", description = "Remark: Shall not be used in the consent model, since account category provides enough information for the ASPSP. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("typeCode")
  public Optional<CashAccountType> getTypeCode() {
    return typeCode;
  }

  public void setTypeCode(Optional<CashAccountType> typeCode) {
    this.typeCode = typeCode;
  }

  public AccountReference typeProprietary(String typeProprietary) {
    this.typeProprietary = Optional.ofNullable(typeProprietary);
    return this;
  }

  /**
   * Remark: Shall not be used in the consent model, since account category provides enough information for the ASPSP.
   * @return typeProprietary
   */

  @Schema(name = "typeProprietary", example = "Text, maximum of 35 characters.", description = "Remark: Shall not be used in the consent model, since account category provides enough information for the ASPSP. ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("typeProprietary")
  public Optional<@Size(max = 35) String> getTypeProprietary() {
    return typeProprietary;
  }

  public void setTypeProprietary(Optional<String> typeProprietary) {
    this.typeProprietary = typeProprietary;
  }

  public AccountReference currency(String currency) {
    this.currency = Optional.ofNullable(currency);
    return this;
  }

  /**
   * ISO 4217 Alpha 3 currency code
   * @return currency
   */

  @Schema(name = "currency", example = "EUR", description = "ISO 4217 Alpha 3 currency code ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("currency")
  public Optional<@Pattern(regexp = "[A-Z]{3}") String> getCurrency() {
    return currency;
  }

  public void setCurrency(Optional<String> currency) {
    this.currency = currency;
  }

  public AccountReference proxy(ProxyAccountIdentification proxy) {
    this.proxy = Optional.ofNullable(proxy);
    return this;
  }

  /**
   *
   * @return proxy
   */
  @Valid
  @Schema(name = "proxy", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("proxy")
  public Optional<ProxyAccountIdentification> getProxy() {
    return proxy;
  }

  public void setProxy(Optional<ProxyAccountIdentification> proxy) {
    this.proxy = proxy;
  }

  public AccountReference name(String name) {
    this.name = Optional.ofNullable(name);
    return this;
  }

  /**
   * Name of the account
   * @return name
   */

  @Schema(name = "name", example = "Text, maximum of 70 characters.", description = "Name of the account ", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("name")
  public Optional<@Size(max = 70) String> getName() {
    return name;
  }

  public void setName(Optional<String> name) {
    this.name = name;
  }

  public AccountReference owner(PartyDescription2 owner) {
    this.owner = Optional.ofNullable(owner);
    return this;
  }

  /**
   *
   * @return owner
   */
  @Valid
  @Schema(name = "owner", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("owner")
  public Optional<PartyDescription2> getOwner() {
    return owner;
  }

  public void setOwner(Optional<PartyDescription2> owner) {
    this.owner = owner;
  }

  public AccountReference servicer(FinancialInstitutionIdentification servicer) {
    this.servicer = Optional.ofNullable(servicer);
    return this;
  }

  /**
   *
   * @return servicer
   */
  @Valid
  @Schema(name = "servicer", description = "", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
  @JsonProperty("servicer")
  public Optional<FinancialInstitutionIdentification> getServicer() {
    return servicer;
  }

  public void setServicer(Optional<FinancialInstitutionIdentification> servicer) {
    this.servicer = servicer;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AccountReference accountReference = (AccountReference) o;
    return Objects.equals(this.iban, accountReference.iban) &&
        Objects.equals(this.bban, accountReference.bban) &&
        Objects.equals(this.pan, accountReference.pan) &&
        Objects.equals(this.maskedPan, accountReference.maskedPan) &&
        Objects.equals(this.msisdn, accountReference.msisdn) &&
        Objects.equals(this.other, accountReference.other) &&
        Objects.equals(this.typeCode, accountReference.typeCode) &&
        Objects.equals(this.typeProprietary, accountReference.typeProprietary) &&
        Objects.equals(this.currency, accountReference.currency) &&
        Objects.equals(this.proxy, accountReference.proxy) &&
        Objects.equals(this.name, accountReference.name) &&
        Objects.equals(this.owner, accountReference.owner) &&
        Objects.equals(this.servicer, accountReference.servicer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(iban, bban, pan, maskedPan, msisdn, other, typeCode, typeProprietary, currency, proxy, name, owner, servicer);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AccountReference {\n");
    sb.append("    iban: ").append(toIndentedString(iban)).append("\n");
    sb.append("    bban: ").append(toIndentedString(bban)).append("\n");
    sb.append("    pan: ").append(toIndentedString(pan)).append("\n");
    sb.append("    maskedPan: ").append(toIndentedString(maskedPan)).append("\n");
    sb.append("    msisdn: ").append(toIndentedString(msisdn)).append("\n");
    sb.append("    other: ").append(toIndentedString(other)).append("\n");
    sb.append("    typeCode: ").append(toIndentedString(typeCode)).append("\n");
    sb.append("    typeProprietary: ").append(toIndentedString(typeProprietary)).append("\n");
    sb.append("    currency: ").append(toIndentedString(currency)).append("\n");
    sb.append("    proxy: ").append(toIndentedString(proxy)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    owner: ").append(toIndentedString(owner)).append("\n");
    sb.append("    servicer: ").append(toIndentedString(servicer)).append("\n");
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

