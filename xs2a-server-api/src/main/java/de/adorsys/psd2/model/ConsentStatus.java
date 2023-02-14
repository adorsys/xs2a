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
 * This is the overall lifecycle status of the consent.  Valid values are:   - 'received': The consent data have been received and are technically correct.      The data is not authorised yet.   - 'rejected': The consent data have been rejected e.g. since no successful authorisation has taken place.   - 'valid': The consent is accepted and valid for GET account data calls and others as specified in the consent object.   - 'revokedByPsu': The consent has been revoked by the PSU towards the ASPSP.   - 'expired': The consent expired.   - 'terminatedByTpp': The corresponding TPP has terminated the consent by applying the DELETE method to the consent resource.   - 'partiallyAuthorised': The consent is due to a multi-level authorisation, some but not all mandated authorisations have been performed yet.  The ASPSP might add further codes. These codes then shall be contained in the ASPSP's documentation of the XS2A interface  and has to be added to this API definition as well.
 */
public enum ConsentStatus {
  RECEIVED("received"),
    REJECTED("rejected"),
    VALID("valid"),
    REVOKEDBYPSU("revokedByPsu"),
    EXPIRED("expired"),
    TERMINATEDBYTPP("terminatedByTpp"),
    PARTIALLYAUTHORISED("partiallyAuthorised");

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
