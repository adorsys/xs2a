package de.adorsys.psd2.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Day of execution as string in the form DD.  Thes string consists always of two characters.  31 is ultimo of the month.
 */
public enum DayOfExecution {

    _01("01"),

    _02("02"),

    _03("03"),

    _04("04"),

    _05("05"),

    _06("06"),

    _07("07"),

    _08("08"),

    _09("09"),

    _10("10"),

    _11("11"),

    _12("12"),

    _13("13"),

    _14("14"),

    _15("15"),

    _16("16"),

    _17("17"),

    _18("18"),

    _19("19"),

    _20("20"),

    _21("21"),

    _22("22"),

    _23("23"),

    _24("24"),

    _25("25"),

    _26("26"),

    _27("27"),

    _28("28"),

    _29("29"),

    _30("30"),

    _31("31");

    private String value;

    DayOfExecution(String value) {
        this.value = value;
    }

    @JsonCreator
    public static DayOfExecution fromValue(String text) {
        for (DayOfExecution b : DayOfExecution.values()) {
            if (String.valueOf(b.value).equals(text)) {
                return b;
            }
        }
        return null;
    }

    @Override
    @JsonValue
    public String toString() {
        return String.valueOf(value);
    }
}

