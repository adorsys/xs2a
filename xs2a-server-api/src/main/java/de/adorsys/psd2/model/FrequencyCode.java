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
 * The following codes from the \"EventFrequency7Code\" of ISO 20022 are supported: - \"Daily\" - \"Weekly\" - \"EveryTwoWeeks\" - \"Monthly\" - \"EveryTwoMonths\" - \"Quarterly\" - \"SemiAnnual\" - \"Annual\" - \"MonthlyVariable\"
 */
public enum FrequencyCode {
  DAILY("Daily"),
    WEEKLY("Weekly"),
    EVERYTWOWEEKS("EveryTwoWeeks"),
    MONTHLY("Monthly"),
    EVERYTWOMONTHS("EveryTwoMonths"),
    QUARTERLY("Quarterly"),
    SEMIANNUAL("SemiAnnual"),
    ANNUAL("Annual"),
    MONTHLYVARIABLE("MonthlyVariable");

  private String value;

  FrequencyCode(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static FrequencyCode fromValue(String text) {
    for (FrequencyCode b : FrequencyCode.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
