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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes defined for AIS for HTTP Error code 400 (BAD_REQUEST).
 */
public enum MessageCode400AIS {
  
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
  
  SESSIONS_NOT_SUPPORTED("SESSIONS_NOT_SUPPORTED");

  private String value;

  MessageCode400AIS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode400AIS fromValue(String text) {
    for (MessageCode400AIS b : MessageCode400AIS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}

