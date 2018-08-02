package de.adorsys.aspsp.xs2a.spi.domain.consent;

import lombok.Value;

import java.util.Arrays;

@Value
public class AspspConsentData {
    private byte[] aspspConsentData;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AspspConsentData that = (AspspConsentData) o;

        return Arrays.equals(aspspConsentData, that.aspspConsentData);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(aspspConsentData);
    }
}
