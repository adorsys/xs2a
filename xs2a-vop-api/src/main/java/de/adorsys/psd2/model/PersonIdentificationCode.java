package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Other ISO-related basic Types\\\" for more details.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum PersonIdentificationCode {

  ARNU("ARNU"),

  CCPT("CCPT"),

  CUST("CUST"),

  DRLC("DRLC"),

  EMPL("EMPL"),

  NIDN("NIDN"),

  SOSE("SOSE"),

  TELE("TELE"),

  TXID("TXID"),

  POID("POID");

  private final String value;

  PersonIdentificationCode(String value) {
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
  public static PersonIdentificationCode fromValue(String value) {
    for (PersonIdentificationCode b : PersonIdentificationCode.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

