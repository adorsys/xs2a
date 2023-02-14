/*
 * Copyright 2018-2023 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at psd2@adorsys.com.
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
