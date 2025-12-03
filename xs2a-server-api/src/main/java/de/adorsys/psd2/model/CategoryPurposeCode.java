package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * ExternalCategoryPurpose1Code from ISO 20022.  Values from ISO 20022 External Code List ExternalCodeSets_2Q2024 August 2024.
 */
public enum CategoryPurposeCode {
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

    private String value;

    CategoryPurposeCode(String value) {
        this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static CategoryPurposeCode fromValue(String text) {
        for (CategoryPurposeCode b : CategoryPurposeCode.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }
}
