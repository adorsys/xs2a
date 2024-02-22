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
 * The following balance types are defined:   - \"closingBooked\":      Balance of the account at the end of the pre-agreed account reporting period.      It is the sum of the opening booked balance at the beginning of the period and all entries booked      to the account during the pre-agreed account reporting period.          For card-accounts, this is composed of            - invoiced, but not yet paid entries        - \"expected\":     Balance composed of booked entries and pending items known at the time of calculation,      which projects the end of day balance if everything is booked on the account and no other entry is posted.          For card accounts, this is composed of:       - invoiced, but not yet paid entries       - not yet invoiced but already booked entries and       - pending items (not yet booked)          For card-accounts:          \"money to spend with the value of a pre-approved credit limit on the card account\"        - \"openingBooked\":     Book balance of the account at the beginning of the account reporting period.      It always equals the closing book balance from the previous report.   - \"interimAvailable\":     Available balance calculated in the course of the account ?servicer?s business day,      at the time specified, and subject to further changes during the business day.      The interim balance is calculated on the basis of booked credit and debit items during the calculation      time/period specified.          For card-accounts, this is composed of:       - invoiced, but not yet paid entries       - not yet invoiced but already booked entries   - \"interimBooked\":     Balance calculated in the course of the account servicer's business day, at the time specified,      and subject to further changes during the business day.      The interim balance is calculated on the basis of booked credit and debit items during the calculation time/period      specified.   - \"forwardAvailable\":     Forward available balance of money that is at the disposal of the account owner on the date specified.   - \"nonInvoiced\":      Only for card accounts, to be checked yet.
 */
public enum BalanceType {
  CLOSINGBOOKED("closingBooked"),
    EXPECTED("expected"),
    OPENINGBOOKED("openingBooked"),
    INTERIMAVAILABLE("interimAvailable"),
    INTERIMBOOKED("interimBooked"),
    FORWARDAVAILABLE("forwardAvailable"),
    NONINVOICED("nonInvoiced");

  private String value;

  BalanceType(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static BalanceType fromValue(String text) {
    for (BalanceType b : BalanceType.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
