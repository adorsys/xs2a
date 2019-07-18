package de.adorsys.psd2.xs2a.web.validator.constants;

import java.time.format.DateTimeFormatter;

public enum Xs2aBodyDateFormatter {
    ISO_DATE(DateTimeFormatter.ISO_DATE, "YYYY-MM-DD"),
    ISO_DATE_TIME(DateTimeFormatter.ISO_DATE_TIME, "YYYY-MM-DD'T'HH:mm:ssZ");

    private DateTimeFormatter formatter;
    private String pattern;

    Xs2aBodyDateFormatter(DateTimeFormatter formatter, String pattern) {
        this.formatter = formatter;
        this.pattern = pattern;
    }

    public DateTimeFormatter getFormatter() {
        return formatter;
    }

    public String getPattern() {
        return pattern;
    }
}
