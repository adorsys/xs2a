package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.validator.TppInfoProvider;
import lombok.Value;
import org.jetbrains.annotations.NotNull;

@Value
public class GetConsentAuthorisationScaStatusPO implements TppInfoProvider {
    @NotNull
    private final AccountConsent accountConsent;

    @NotNull
    private final String authorisationId;

    @Override
    public TppInfo getTppInfo() {
        return accountConsent.getTppInfo();
    }
}
