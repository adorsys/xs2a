package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Other ISO-related basic Types\\\" for more details.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum CashAccountType {

  CACC("CACC"),

  CARD("CARD"),

  CASH("CASH"),

  CHAR("CHAR"),

  CISH("CISH"),

  COMM("COMM"),

  CPAC("CPAC"),

  LLSV("LLSV"),

  LOAN("LOAN"),

  MGLD("MGLD"),

  MOMA("MOMA"),

  NREX("NREX"),

  ODFT("ODFT"),

  ONDP("ONDP"),

  OTHR("OTHR"),

  SACC("SACC"),

  SLRY("SLRY"),

  SVGS("SVGS"),

  TAXE("TAXE"),

  TRAN("TRAN"),

  TRAS("TRAS"),

  VACC("VACC"),

  NFCA("NFCA");

  private final String value;

  CashAccountType(String value) {
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
  public static CashAccountType fromValue(String value) {
    for (CashAccountType b : CashAccountType.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

