package de.adorsys.psd2.xs2a.service.validator.ais.consent;

import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;

@Slf4j
@Component
@RequiredArgsConstructor
public class AisAuthorisationValidator {
    private final RequestProviderService requestProviderService;

    @NotNull
    public ValidationResult validate(@NotNull String authorisationId, @NotNull AccountConsent consent) {
        Optional<AccountConsentAuthorization> authorisationOptional = consent.findAuthorisationInConsent(authorisationId);
        if (!authorisationOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent ID: [{}], Authorisation ID: [{}]. Authorisation validation has failed: couldn't find authorisation with given authorisationId for consent",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), consent.getId(), authorisationId);
            return ValidationResult.invalid(ErrorType.AIS_403, RESOURCE_UNKNOWN_403);
        }

        return ValidationResult.valid();
    }
}
