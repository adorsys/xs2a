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
 * ExternalServiceLevel1Code from ISO 20022.  Values from ISO 20022 External Code List ExternalCodeSets_1Q2021 May 2021.
 */
public enum ServiceLevelCode {
  BKTR("BKTR"),
    G001("G001"),
    G002("G002"),
    G003("G003"),
    G004("G004"),
    NPCA("NPCA"),
    NUGP("NUGP"),
    NURG("NURG"),
    PRPT("PRPT"),
    SDVA("SDVA"),
    SEPA("SEPA"),
    SVDE("SVDE"),
    URGP("URGP"),
    URNS("URNS");

  private String value;

  ServiceLevelCode(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static ServiceLevelCode fromValue(String text) {
    for (ServiceLevelCode b : ServiceLevelCode.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
