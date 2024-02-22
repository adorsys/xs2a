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
