/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
 *
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version. This program is distributed in the hope that
 * it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 *
 * This project is also available under a separate commercial license. You can
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_NO_PSU;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;

@Component
public class PiisPsuDataUpdateAuthorisationCheckerValidator extends PsuDataUpdateAuthorisationCheckerValidator {
    public PiisPsuDataUpdateAuthorisationCheckerValidator(RequestProviderService requestProviderService, PsuDataUpdateAuthorisationChecker psuDataUpdateAuthorisationChecker) {
        super(requestProviderService, psuDataUpdateAuthorisationChecker);
    }

    @Override
    public MessageError getMessageErrorAreBothPsusAbsent() {
        return new MessageError(ErrorType.PIIS_400, TppMessageInformation.of(FORMAT_ERROR_NO_PSU));
    }

    @Override
    public MessageError getMessageErrorCanPsuUpdateAuthorisation() {
        return new MessageError(ErrorType.PIIS_401, TppMessageInformation.of(PSU_CREDENTIALS_INVALID));
    }
}
