package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Message codes defined for payment cancelations PIS for HTTP Error code 405 (METHOD NOT ALLOWED).
 */
public enum MessageCode405PISCANC {
  
  CANCELLATION_INVALID("CANCELLATION_INVALID"),
  
  SERVICE_INVALID("SERVICE_INVALID");

  private String value;

  MessageCode405PISCANC(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode405PISCANC fromValue(String text) {
    for (MessageCode405PISCANC b : MessageCode405PISCANC.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

