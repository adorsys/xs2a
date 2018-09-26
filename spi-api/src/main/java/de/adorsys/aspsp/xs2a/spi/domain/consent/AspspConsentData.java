package de.adorsys.aspsp.xs2a.spi.domain.consent;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.Objects;

@Value
@RequiredArgsConstructor
public class AspspConsentData {
    private final byte[] aspspConsentData;
    private final String consentId;

    public AspspConsentData() {
        this(null, null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AspspConsentData that = (AspspConsentData) o;

        return Arrays.equals(aspspConsentData, that.aspspConsentData)&& Objects.equals(consentId, that.getConsentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(aspspConsentData, consentId);
    }
}
