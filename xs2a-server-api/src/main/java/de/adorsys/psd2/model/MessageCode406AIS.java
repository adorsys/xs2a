package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Message codes defined for AIS for HTTP Error code 406 (NOT ACCEPTABLE).
 */
public enum MessageCode406AIS {
  
  INVALID("REQUESTED_FORMATS_INVALID");

  private String value;

  MessageCode406AIS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode406AIS fromValue(String text) {
    for (MessageCode406AIS b : MessageCode406AIS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

