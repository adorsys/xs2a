/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.model;

import java.util.Objects;
import io.swagger.annotations.ApiModel;
import com.fasterxml.jackson.annotation.JsonValue;
import org.springframework.validation.annotation.Validated;
import javax.validation.Valid;
import javax.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * This is the overall lifecycle status of the consent.  Valid values are:   - 'received': The consent data have been received and are technically correct.      The data is not authorised yet.   - 'rejected': The consent data have been rejected e.g. since no successful authorisation has taken place.   - 'valid': The consent is accepted and valid for GET account data calls and others as specified in the consent object.   - 'revokedByPsu': The consent has been revoked by the PSU towards the ASPSP.   - 'expired': The consent expired.   - 'terminatedByTpp': The corresponding TPP has terminated the consent by applying the DELETE method to the consent resource.  The ASPSP might add further codes. These codes then shall be contained in the ASPSP's documentation of the XS2A interface  and has to be added to this API definition as well. 
 */
public enum ConsentStatus {
  
  RECEIVED("received"),
  
  REJECTED("rejected"),
  
  VALID("valid"),
  
  REVOKEDBYPSU("revokedByPsu"),
  
  EXPIRED("expired"),
  
  TERMINATEDBYTPP("terminatedByTpp");

  private String value;

  ConsentStatus(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ConsentStatus fromValue(String text) {
    for (ConsentStatus b : ConsentStatus.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

