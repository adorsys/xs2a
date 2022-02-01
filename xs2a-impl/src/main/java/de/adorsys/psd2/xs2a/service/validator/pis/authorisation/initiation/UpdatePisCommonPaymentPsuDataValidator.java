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

package de.adorsys.psd2.xs2a.service.validator.pis.authorisation.initiation;

import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.PisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.PisPsuDataUpdateAuthorisationCheckerValidator;
import de.adorsys.psd2.xs2a.service.validator.authorisation.AuthorisationStageCheckValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.AbstractUpdatePisPsuDataValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationStatusValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.PisAuthorisationValidator;
import de.adorsys.psd2.xs2a.service.validator.pis.authorisation.UpdatePisPsuDataPO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.RESOURCE_EXPIRED_403;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.SCA_INVALID;
import static de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationServiceType.PIS;

/**
 * Validator to be used for validating update PSU Data in payment authorisation request according to some business rules
 */
@Slf4j
@Component
public class UpdatePisCommonPaymentPsuDataValidator extends AbstractUpdatePisPsuDataValidator<UpdatePaymentPsuDataPO> {
    private final AspspProfileServiceWrapper aspspProfileService;

    public UpdatePisCommonPaymentPsuDataValidator(PisEndpointAccessCheckerService pisEndpointAccessCheckerService,
                                                  PisAuthorisationValidator pisAuthorisationValidator,
                                                  PisAuthorisationStatusValidator pisAuthorisationStatusValidator,
                                                  PisPsuDataUpdateAuthorisationCheckerValidator pisPsuDataUpdateAuthorisationCheckerValidator,
                                                  AuthorisationStageCheckValidator authorisationStageCheckValidator, AspspProfileServiceWrapper aspspProfileService) {
        super(pisEndpointAccessCheckerService, pisAuthorisationValidator,
              pisAuthorisationStatusValidator, pisPsuDataUpdateAuthorisationCheckerValidator,
              authorisationStageCheckValidator);
        this.aspspProfileService = aspspProfileService;
    }

    @Override
    protected AuthorisationServiceType getAuthorisationServiceType() {
        return PIS;
    }

    @Override
    protected ValidationResult validateTransactionStatus(UpdatePisPsuDataPO paymentObject) {
        if (paymentObject.getPisCommonPaymentResponse().getTransactionStatus() != TransactionStatus.RJCT) {
            return ValidationResult.valid();
        }

        if (aspspProfileService.isAuthorisationConfirmationRequestMandated() && StringUtils.isNotBlank(paymentObject.getUpdateRequest().getConfirmationCode())) {
            return ValidationResult.invalid(ErrorType.PIS_400, SCA_INVALID);
        }


        log.info("Authorisation ID: [{}]. Updating PIS initiation authorisation PSU Data has failed: payment has been rejected",
                 paymentObject.getUpdateRequest().getAuthorisationId());

        return ValidationResult.invalid(ErrorType.PIS_403, RESOURCE_EXPIRED_403);
    }
}
