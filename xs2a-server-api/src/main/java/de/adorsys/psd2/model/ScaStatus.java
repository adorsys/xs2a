package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * This data element is containing information about the status of the SCA method applied.   The following codes are defined for this data type.    * 'received':     An authorisation or cancellation-authorisation resource has been created successfully.   * 'psuIdentified':     The PSU related to the authorisation or cancellation-authorisation resource has been identified.   * 'psuAuthenticated':     The PSU related to the authorisation or cancellation-authorisation resource has been identified and authenticated e.g. by a password or by an access token.   * 'scaMethodSelected':     The PSU/TPP has selected the related SCA routine.      If the SCA method is chosen implicitly since only one SCA method is available,      then this is the first status to be reported instead of 'received'.   * 'unconfirmed':     SCA is technically successfully finalised by the PSU, but the authorisation resource needs a confirmation command by the TPP yet.    * 'started':     The addressed SCA routine has been started.   * 'finalised':     The SCA routine has been finalised successfully (including a potential confirmation command).      This is a final status of the authorisation resource.   * 'failed':     The SCA routine failed.     This is a final status of the authorisation resource.   * 'exempted':     SCA was exempted for the related transaction, the related authorisation is successful.     This is a final status of the authorisation resource. 
 */
public enum ScaStatus {
  
  RECEIVED("received"),
  
  PSUIDENTIFIED("psuIdentified"),
  
  PSUAUTHENTICATED("psuAuthenticated"),
  
  SCAMETHODSELECTED("scaMethodSelected"),
  
  STARTED("started"),
  
  UNCONFIRMED("unconfirmed"),
  
  FINALISED("finalised"),
  
  FAILED("failed"),
  
  EXEMPTED("exempted");

  private String value;

  ScaStatus(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ScaStatus fromValue(String text) {
    for (ScaStatus b : ScaStatus.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

