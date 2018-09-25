package de.adorsys.aspsp.xs2a.spi.domain.consent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Arrays;
import java.util.Objects;

@Value
@RequiredArgsConstructor
public class AspspConsentData {
    private byte[] body;
    @JsonIgnore
    private String consentId;
    // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Remove Value

    public AspspConsentData(String consentId) {
        this.body = "ewogIHBheW1lbnRUb2tlbjogQUJDRDEyMzE0MSwKICBzeXN0ZW1JZDogREVEQUlKRUosCiAgbXVsdGl1c2U6IHRydWUsCiAgZXhwaXJlczogMCwKICB0cmFuc2FjdGlvbnM6IFsKICAgIHsKICAgICAgdHJhbnNhY3Rpb25JZDogaWppZWpmaWUyM3IyLAogICAgICBzdGF0dXM6IE9LCiAgICB9LAogICAgewogICAgICB0cmFuc2FjdGlvbklkOiBpamllamZ3cndpZTIzcjIsCiAgICAgIHN0YXR1czogRkFJTEVECiAgICB9LAogICAgewogICAgICB0cmFuc2FjdGlvbklkOiBpamllcnQyamZpZTIzcjIsCiAgICAgIHN0YXR1czogT0sKICAgIH0sCiAgICB7CiAgICAgIHRyYW5zYWN0aW9uSWQ6IGlqMzI0MzJpZWpmaWUyM3IyLAogICAgICBzdGF0dXM6IE9LCiAgICB9CiAgXQp9Cg==".getBytes();
        this.consentId = consentId;
    }

    public AspspConsentData() {
        this(null);
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

        return Arrays.equals(body, that.body)&& Objects.equals(consentId, that.getConsentId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, consentId);
    }
}
