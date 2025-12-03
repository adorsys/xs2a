package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 *
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum OrganisationIdentificationCode {

  BANK("BANK"),

  CBID("CBID"),

  CHID("CHID"),

  CINC("CINC"),

  COID("COID"),

  CUST("CUST"),

  DUNS("DUNS"),

  EMPL("EMPL"),

  GS1_G("GS1G"),

  SREN("SREN"),

  SRET("SRET"),

  TXID("TXID"),

  BDID("BDID"),

  BOID("BOID");

  private final String value;

  OrganisationIdentificationCode(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static OrganisationIdentificationCode fromValue(String value) {
    for (OrganisationIdentificationCode b : OrganisationIdentificationCode.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

