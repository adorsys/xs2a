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

package de.adorsys.psd2.xs2a.spi.domain.payment;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * ExternalCategoryPurpose1Code from ISO 20022.
 * Values from ISO 20022 External Code List ExternalCodeSets_2Q2024 August 2024.
 */
@SuppressWarnings("PMD")
public enum SpiCategoryPurposeCode {
    BONU("BONU"),
    CASH("CASH"),
    CBLK("CBLK"),
    CCRD("CCRD"),
    CORT("CORT"),
    DCRD("DCRD"),
    DIVI("DIVI"),
    DVPM("DVPM"),
    EPAY("EPAY"),
    FCIN("FCIN"),
    FCOL("FCOL"),
    GP2P("GP2P"),
    GOVT("GOVT"),
    HEDG("HEDG"),
    ICCP("ICCP"),
    IDCP("IDCP"),
    INTC("INTC"),
    INTE("INTE"),
    LBOX("LBOX"),
    LOAN("LOAN"),
    MP2B("MP2B"),
    MP2P("MP2P"),
    OTHR("OTHR"),
    PENS("PENS"),
    RPRE("RPRE"),
    RRCT("RRCT"),
    RVPM("RVPM"),
    SALA("SALA"),
    SECU("SECU"),
    SSBE("SSBE"),
    SUPP("SUPP"),
    TAXS("TAXS"),
    TRAD("TRAD"),
    TREA("TREA"),
    VATX("VATX"),
    WHLD("WHLD"),
    SWEP("SWEP"),
    TOPG("TOPG"),
    ZABA("ZABA"),
    VOST("VOST"),
    FCDT("FCDT"),
    CIPC("CIPC"),
    CONC("CONC"),
    CGWV("CGWV"),
    SAVG("SAVG"),
    CTDF("CTDF");

    private final String value;

    SpiCategoryPurposeCode(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static SpiCategoryPurposeCode fromValue(String text) {
        for (SpiCategoryPurposeCode b : SpiCategoryPurposeCode.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
