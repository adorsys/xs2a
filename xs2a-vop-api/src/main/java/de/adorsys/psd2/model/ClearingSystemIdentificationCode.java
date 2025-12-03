package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Other ISO-related basic Types\\\" for more detail
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum ClearingSystemIdentificationCode {

  ATBLZ("ATBLZ"),

  AUBSB("AUBSB"),

  CACPA("CACPA"),

  CHBCC("CHBCC"),

  CHSIC("CHSIC"),

  CNAPS("CNAPS"),

  DEBLZ("DEBLZ"),

  ESNCC("ESNCC"),

  GBDSC("GBDSC"),

  GRBIC("GRBIC"),

  HKNCC("HKNCC"),

  IENCC("IENCC"),

  INFSC("INFSC"),

  ITNCC("ITNCC"),

  JPZGN("JPZGN"),

  NZNCC("NZNCC"),

  PLKNR("PLKNR"),

  PTNCC("PTNCC"),

  RUCBC("RUCBC"),

  SESBA("SESBA"),

  SGIBG("SGIBG"),

  THCBC("THCBC"),

  TWNCC("TWNCC"),

  USABA("USABA"),

  USPID("USPID"),

  ZANCC("ZANCC"),

  NZRSA("NZRSA"),

  MZBMO("MZBMO"),

  CNCIP("CNCIP"),

  KRBOK("KRBOK");

  private final String value;

  ClearingSystemIdentificationCode(String value) {
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
  public static ClearingSystemIdentificationCode fromValue(String value) {
    for (ClearingSystemIdentificationCode b : ClearingSystemIdentificationCode.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

