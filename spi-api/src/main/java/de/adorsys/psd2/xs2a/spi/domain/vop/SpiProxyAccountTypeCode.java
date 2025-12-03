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
 * Proxy Account Type Code enumeration.
 * See document "openFinance API Framework Data Dictionary", section "Other ISO-related basic Types" for more details.
 */
public enum SpiProxyAccountTypeCode {

    TELE("TELE"),
    EMAL("EMAL"),
    DNAM("DNAM"),
    CINC("CINC"),
    COTX("COTX"),
    COID("COID"),
    CUST("CUST"),
    DRLC("DRLC"),
    EIDN("EIDN"),
    EWAL("EWAL"),
    PVTX("PVTX"),
    LEIC("LEIC"),
    MBNO("MBNO"),
    NIDN("NIDN"),
    CCPT("CCPT"),
    SHID("SHID"),
    SOSE("SOSE"),
    TOKN("TOKN"),
    UBIL("UBIL"),
    VIPN("VIPN"),
    BIID("BIID");

    private final String value;

    SpiProxyAccountTypeCode(String value) {
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
    public static SpiProxyAccountTypeCode fromValue(String value) {
        for (SpiProxyAccountTypeCode b : SpiProxyAccountTypeCode.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}
