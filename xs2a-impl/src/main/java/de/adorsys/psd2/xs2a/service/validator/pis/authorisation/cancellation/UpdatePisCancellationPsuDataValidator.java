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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.cancellation;

import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.PisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.AbstractUpdatePisPsuDataValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationStatusValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.UpdatePisPsuDataPO;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation.UpdatePaymentPsuDataPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType.PIS_CANCELLATION;

/**
 * Validator to be used for validating create PIS cancellation authorisation request according to some business rules
 */
@Slf4j
@Component
public class UpdatePisCancellationPsuDataValidator extends AbstractUpdatePisPsuDataValidator<UpdatePaymentPsuDataPO> {

    public UpdatePisCancellationPsuDataValidator(PisEndpointAccessCheckerService pisEndpointAccessCheckerService,
                                                 PisAuthorisationValidator pisAuthorisationValidator,
                                                 PisAuthorisationStatusValidator pisAuthorisationStatusValidator,
                                                 PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator,
                                                 AuthorisationStageCheckValidator authorisationStageCheckValidator) {
        super(pisEndpointAccessCheckerService, pisAuthorisationValidator,
              pisAuthorisationStatusValidator, pisPsuDataUpdateAuthorisationCheckerValidator,
              authorisationStageCheckValidator);
    }

    @Override
    protected ValidationResult validateTransactionStatus(UpdatePisPsuDataPO paymentObject) {
        return ValidationResult.valid();
    }

    @Override
    protected AuthorisationServiceType getAuthorisationServiceType() {
        return PIS_CANCELLATION;
    }
}
