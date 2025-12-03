package de.adorsys.psd2.model;

import jakarta.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;


/**
 * See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Status Reason Code\\\" for more details.
 */

@Generated(value = "org.openapitools.codegen.languages.SpringCodegen", date = "2025-09-18T08:00:00.499860948+01:00[Africa/Douala]", comments = "Generator version: 7.15.0")
public enum StatusReasonCode {

  AM04("AM04"),

  AM21("AM21"),

  BEXX("BEXX"),

  CN01("CN01"),

  DS0_C("DS0C"),

  DS0_D("DS0D"),

  DS0_A("DS0A"),

  DS04("DS04"),

  FOCR("FOCR"),

  SL11("SL11"),

  TKSP("TKSP"),

  TKXP("TKXP"),

  AM02("AM02"),

  DT05("DT05"),

  AC05("AC05"),

  AC06("AC06"),

  DS0_K("DS0K"),

  DT01("DT01"),

  UPAY("UPAY"),

  EOL1("EOL1");

  private final String value;

  StatusReasonCode(String value) {
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
  public static StatusReasonCode fromValue(String value) {
    for (StatusReasonCode b : StatusReasonCode.values()) {
      if (b.value.equals(value)) {
        return b;
      }
    }
    throw new IllegalArgumentException("Unexpected value '" + value + "'");
  }
}

