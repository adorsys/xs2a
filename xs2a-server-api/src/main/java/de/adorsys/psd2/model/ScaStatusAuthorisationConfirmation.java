package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * This data element is containing information about the status of the SCA method in an authorisation confirmation response.   The following codes are defined for this data type.    * 'finalised': if the transaction authorisation and confirmation was successfule.   * 'failed': if the transaction authorisation or confirmation was not successful. 
 */
public enum ScaStatusAuthorisationConfirmation {
  
  FINALISED("finalised"),
  
  FAILED("failed");

  private String value;

  ScaStatusAuthorisationConfirmation(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ScaStatusAuthorisationConfirmation fromValue(String text) {
    for (ScaStatusAuthorisationConfirmation b : ScaStatusAuthorisationConfirmation.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

