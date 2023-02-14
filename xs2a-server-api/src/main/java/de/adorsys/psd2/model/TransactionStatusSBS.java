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
 * The transaction status is filled with codes of the ISO 20022 data table. Only the codes RCVD, PATC, ACTC, ACWC and RJCT are used: - 'ACSP': 'AcceptedSettlementInProcess' -    All preceding checks such as technical validation and customer profile were successful and therefore the payment initiation has been accepted for execution. - 'ACTC': 'AcceptedTechnicalValidation' -    Authentication and syntactical and semantical validation are successful. - 'ACWC': 'AcceptedWithChange' -    Instruction is accepted but a change will be made, such as date or remittance not sent. - 'RCVD': 'Received' -    Payment initiation has been received by the receiving agent. - 'RJCT': 'Rejected' -    Payment initiation or individual transaction included in the payment initiation has been rejected.
 */
public enum TransactionStatusSBS {
  ACSC("ACSC"),
    ACTC("ACTC"),
    PATC("PATC"),
    RCVD("RCVD"),
    RJCT("RJCT"),
    CANC("CANC");

  private String value;

  TransactionStatusSBS(String value) {
    this.value = value;
  }

  @Override
  @JsonValue
  public String toString() {
    return String.valueOf(value);
  }

  @JsonCreator
  public static TransactionStatusSBS fromValue(String text) {
    for (TransactionStatusSBS b : TransactionStatusSBS.values()) {
      if (String.valueOf(b.value).equals(text)) {
        return b;
      }
    }
    return null;
  }
}
