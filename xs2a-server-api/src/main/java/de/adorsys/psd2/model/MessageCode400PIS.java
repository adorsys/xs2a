package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Message codes defined for PIS for HTTP Error code 400 (BAD_REQUEST).
 */
public enum MessageCode400PIS {
  
  FORMAT_ERROR("FORMAT_ERROR"),
  
  PARAMETER_NOT_CONSISTENT("PARAMETER_NOT_CONSISTENT"),
  
  PARAMETER_NOT_SUPPORTED("PARAMETER_NOT_SUPPORTED"),
  
  SERVICE_INVALID("SERVICE_INVALID"),
  
  RESOURCE_UNKNOWN("RESOURCE_UNKNOWN"),
  
  RESOURCE_EXPIRED("RESOURCE_EXPIRED"),
  
  RESOURCE_BLOCKED("RESOURCE_BLOCKED"),
  
  TIMESTAMP_INVALID("TIMESTAMP_INVALID"),
  
  PERIOD_INVALID("PERIOD_INVALID"),
  
  SCA_METHOD_UNKNOWN("SCA_METHOD_UNKNOWN"),
  
  CONSENT_UNKNOWN("CONSENT_UNKNOWN"),
  
  PAYMENT_FAILED("PAYMENT_FAILED"),
  
  EXECUTION_DATE_INVALID("EXECUTION_DATE_INVALID");

  private String value;

  MessageCode400PIS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode400PIS fromValue(String text) {
    for (MessageCode400PIS b : MessageCode400PIS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

