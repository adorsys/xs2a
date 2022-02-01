/*
 * Copyright 2018-2022 adorsys GmbH & Co KG
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
 * contact us at psd2@adorsys.com.
 */

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation;

import de.adorsys.psd2.consent.api.pis.PisCommonPaymentResponse;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_UNKNOWN_403;

@Slf4j
@Component
public class PisAuthorisationValidator {

    @NotNull
    public ValidationResult validate(@NotNull String authorisationId, @NotNull PisCommonPaymentResponse commonPaymentResponse) {
        Optional<Authorisation> authorisationOptional = commonPaymentResponse.findAuthorisationInPayment(authorisationId);

        if (authorisationOptional.isEmpty()) {
            log.info("Payment ID: [{}], Authorisation ID: [{}]. Updating PIS initiation authorisation PSU Data has failed: couldn't find authorisation with given authorisationId for payment",
                     commonPaymentResponse.getExternalId(), authorisationId);
            return ValidationResult.invalid(ErrorType.PIS_403, RESOURCE_UNKNOWN_403);
        }
        return ValidationResult.valid();
    }
}
