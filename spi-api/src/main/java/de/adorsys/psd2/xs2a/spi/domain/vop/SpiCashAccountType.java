/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.vop;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Cash Account Type enumeration.
 * See document "openFinance API Framework Data Dictionary", section "Other ISO-related basic Types" for more details.
 */
public enum SpiCashAccountType {

    CACC("CACC"),
    CARD("CARD"),
    CASH("CASH"),
    CHAR("CHAR"),
    CISH("CISH"),
    COMM("COMM"),
    CPAC("CPAC"),
    LLSV("LLSV"),
    LOAN("LOAN"),
    MGLD("MGLD"),
    MOMA("MOMA"),
    NREX("NREX"),
    ODFT("ODFT"),
    ONDP("ONDP"),
    OTHR("OTHR"),
    SACC("SACC"),
    SLRY("SLRY"),
    SVGS("SVGS"),
    TAXE("TAXE"),
    TRAN("TRAN"),
    TRAS("TRAS"),
    VACC("VACC"),
    NFCA("NFCA");

    private final String value;

    SpiCashAccountType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static SpiCashAccountType fromValue(String value) {
        for (SpiCashAccountType b : SpiCashAccountType.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
