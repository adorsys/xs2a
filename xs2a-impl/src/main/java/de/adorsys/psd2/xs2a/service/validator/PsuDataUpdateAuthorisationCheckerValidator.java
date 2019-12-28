package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Slf4j
@RequiredArgsConstructor
public abstract class PsuDataUpdateAuthorisationCheckerValidator {
    private final RequestProviderService requestProviderService;
    private final PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker;

    @NotNull
    public ValidationResult validate(@NotNull PsuIdData psuIdDataRequest, @Nullable PsuIdData psuIdDataAuthorisation) {
        if (psuDataUpdateAuthorisationChecker.areBothPsusAbsent(psuIdDataRequest, psuIdDataAuthorisation)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], PsuID-Request: [{}], PsuID-Authorisation: [{}]. Updating PSU Data has failed: PSU from request and PSU from authorisation are absent",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), psuIdDataRequest, psuIdDataAuthorisation);
            return ValidationResult.invalid(getMessageErrorAreBothPsusAbsent());
        }

        if (!psuDataUpdateAuthorisationChecker.canPsuUpdateAuthorisation(psuIdDataRequest, psuIdDataAuthorisation)) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], PsuID-Request: [{}], PsuID-Authorisation: [{}]. Updating PSU Data has failed: PSU from authorisation and PSU from request are different",
                     requestProviderService.getInternalRequestId(), requestProviderService.getRequestId(), psuIdDataRequest, psuIdDataAuthorisation);
            return ValidationResult.invalid(getMessageErrorCanPsuUpdateAuthorisation());
        }

        return ValidationResult.valid();
    }

    public abstract MessageError getMessageErrorAreBothPsusAbsent();

    public abstract MessageError getMessageErrorCanPsuUpdateAuthorisation();
}
