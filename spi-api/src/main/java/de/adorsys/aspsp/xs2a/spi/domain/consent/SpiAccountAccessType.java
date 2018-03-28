package de.adorsys.aspsp.xs2a.spi.domain.consent;

public enum SpiAccountAccessType {
	ALL_ACCOUNTS("all-accounts");

    private String description;

    SpiAccountAccessType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
