package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_NO_PSU;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;

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
