package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * See document \\\"openFinance API Framework Extended Services Verification of Party (VOP) \\\",  section \\\"Party Identification Match Code\\\" for more details. Code    Desription  MTCH     Match  NMTC     No match  NOAP     Validation check is not applicable  NOTC     Validation check has not been carried out.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum PartyIdentificationMatchCode {

  MTCH("MTCH"),

  NMTC("NMTC"),

  NOAP("NOAP");

  private final String value;

  PartyIdentificationMatchCode(String value) {
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
  public static PartyIdentificationMatchCode fromValue(String value) {
    for (PartyIdentificationMatchCode b : PartyIdentificationMatchCode.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

