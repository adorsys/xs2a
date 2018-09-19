package de.adorsys.aspsp.xs2a.spi.domain.consent;

import lombok.RequiredArgsConstructor;
import lombok.Value;

import java.util.Arrays;

@Value
@RequiredArgsConstructor
public class AspspConsentData {
    private byte[] body;
    // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/191 Remove Value

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AspspConsentData that = (AspspConsentData) o;

        return Arrays.equals(body, that.body);
    }

    public AspspConsentData() {
        this.body = "ewogIHBheW1lbnRUb2tlbjogQUJDRDEyMzE0MSwKICBzeXN0ZW1JZDogREVEQUlKRUosCiAgbXVsdGl1c2U6IHRydWUsCiAgZXhwaXJlczogMCwKICB0cmFuc2FjdGlvbnM6IFsKICAgIHsKICAgICAgdHJhbnNhY3Rpb25JZDogaWppZWpmaWUyM3IyLAogICAgICBzdGF0dXM6IE9LCiAgICB9LAogICAgewogICAgICB0cmFuc2FjdGlvbklkOiBpamllamZ3cndpZTIzcjIsCiAgICAgIHN0YXR1czogRkFJTEVECiAgICB9LAogICAgewogICAgICB0cmFuc2FjdGlvbklkOiBpamllcnQyamZpZTIzcjIsCiAgICAgIHN0YXR1czogT0sKICAgIH0sCiAgICB7CiAgICAgIHRyYW5zYWN0aW9uSWQ6IGlqMzI0MzJpZWpmaWUyM3IyLAogICAgICBzdGF0dXM6IE9LCiAgICB9CiAgXQp9Cg==".getBytes();
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(body);
    }
}
