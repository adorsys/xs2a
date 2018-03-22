package de.adorsys.aspsp.xs2a.spi.domain.consent;

public enum AccountAccessType {
	ALL_ACCOUNTS("all-accounts");

    private String description;

    AccountAccessType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
