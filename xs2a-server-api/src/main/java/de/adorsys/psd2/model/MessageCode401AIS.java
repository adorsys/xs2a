/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Message codes defined for AIS for HTTP Error code 401 (UNAUTHORIZED).
 */
public enum MessageCode401AIS {
  CERTIFICATE_INVALID("CERTIFICATE_INVALID"),
    CERTIFICATE_EXPIRED("CERTIFICATE_EXPIRED"),
    CERTIFICATE_BLOCKED("CERTIFICATE_BLOCKED"),
    CERTIFICATE_REVOKE("CERTIFICATE_REVOKE"),
    CERTIFICATE_MISSING("CERTIFICATE_MISSING"),
    SIGNATURE_INVALID("SIGNATURE_INVALID"),
    SIGNATURE_MISSING("SIGNATURE_MISSING"),
    CORPORATE_ID_INVALID("CORPORATE_ID_INVALID"),
    PSU_CREDENTIALS_INVALID("PSU_CREDENTIALS_INVALID"),
    CONSENT_INVALID("CONSENT_INVALID"),
    CONSENT_EXPIRED("CONSENT_EXPIRED"),
    TOKEN_UNKNOWN("TOKEN_UNKNOWN"),
    TOKEN_INVALID("TOKEN_INVALID"),
    TOKEN_EXPIRED("TOKEN_EXPIRED");

  private String value;

  MessageCode401AIS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static MessageCode401AIS fromValue(String text) {
    for (MessageCode401AIS b : MessageCode401AIS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
