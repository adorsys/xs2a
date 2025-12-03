package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * See document \\\"openFinance API Framework Data Dictionary\\\", section \\\"Status Reason Code\\\" for more details.
 */
public enum StatusReasonCode {
  AM04("AM04"),
    AM21("AM21"),
    BEXX("BEXX"),
    CN01("CN01"),
    DS0C("DS0C"),
    DS0D("DS0D"),
    DS0A("DS0A"),
    DS04("DS04"),
    FOCR("FOCR"),
    SL11("SL11"),
    TKSP("TKSP"),
    TKXP("TKXP"),
    AM02("AM02"),
    DT05("DT05"),
    AC05("AC05"),
    AC06("AC06"),
    DS0K("DS0K"),
    DT01("DT01"),
    UPAY("UPAY"),
    EOL1("EOL1");

  private String value;

  StatusReasonCode(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static StatusReasonCode fromValue(String text) {
    for (StatusReasonCode b : StatusReasonCode.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
