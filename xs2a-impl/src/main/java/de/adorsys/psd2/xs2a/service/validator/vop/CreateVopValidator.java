/*
 * Copyright 2018-2025 adorsys GmbH & Co KG
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
package de.adorsys.psd2.xs2a.service.validator.vop;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.SingleVop;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.BusinessValidator;
import de.adorsys.psd2.xs2a.service.validator.vop.dto.CreateVopRequestObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CreateVopValidator implements BusinessValidator<CreateVopRequestObject> {
    @Override
    public ValidationResult validate(CreateVopRequestObject requestObject) {
        String endpoint = requestObject != null && requestObject.vopInitiationParameters() != null
            ? requestObject.vopInitiationParameters().getPartyVerificationEndpoint()
            : null;

        if (endpoint != null && SingleVop.getByValue(endpoint).isPresent()) {
            return ValidationResult.valid();
        }
        return ValidationResult.invalid(
            ErrorType.VOP_400,
            TppMessageInformation.buildWithCustomError(MessageErrorCode.FORMAT_ERROR, "Unsupported partyVerificationEndpoint")
        );
    }
}
