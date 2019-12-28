package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_NO_PSU;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;

@Component
public class PisPsuDataUpdateAuthorisationCheckerValidator extends PsuDataUpdateAuthorisationCheckerValidator {
    public PisPsuDataUpdateAuthorisationCheckerValidator(RequestProviderService requestProviderService, PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker) {
        super(requestProviderService, psuDataUpdateAuthorisationChecker);
    }

    @Override
    public MessageError getMessageErrorAreBothPsusAbsent() {
        return new MessageError(ErrorType.PIS_400, of(FORMAT_ERROR_NO_PSU));
    }

    @Override
    public MessageError getMessageErrorCanPsuUpdateAuthorisation() {
        return new MessageError(ErrorType.PIS_401, of(PSU_CREDENTIALS_INVALID));
    }
}
