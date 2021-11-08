package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ExternalServiceLevel1Code from ISO 20022.  Values from ISO 20022 External Code List ExternalCodeSets_1Q2021 May 2021.
 */
public enum ServiceLevelCode {

  BKTR("BKTR"),

  G001("G001"),

  G002("G002"),

  G003("G003"),

  G004("G004"),

  NPCA("NPCA"),

  NUGP("NUGP"),

  NURG("NURG"),

  PRPT("PRPT"),

  SDVA("SDVA"),

  SEPA("SEPA"),

  SVDE("SVDE"),

  URGP("URGP"),

  URNS("URNS");

  private String value;

  ServiceLevelCode(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ServiceLevelCode fromValue(String text) {
    for (ServiceLevelCode b : ServiceLevelCode.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

