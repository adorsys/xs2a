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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetTrustedBeneficiariesListConsentObject;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_405;

/**
 * Validator to be used for validating get trusted beneficiaries list request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetTrustedBeneficiariesListValidator extends AbstractAccountTppValidator<GetTrustedBeneficiariesListConsentObject> {
    private final AccountConsentValidator accountConsentValidator;
    private final AspspProfileServiceWrapper aspspProfileService;

    /**
     * Validates get trusted beneficiaries list request by checking whether:
     * <ul>
     * <li>consent has access to trusted beneficiaries list</li>
     * </ul>
     *
     * @param consentObject consent information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(GetTrustedBeneficiariesListConsentObject consentObject) {
        AisConsent aisConsent = consentObject.getAisConsent();

        ValidationResult accountConsentValidationResult = accountConsentValidator.validate(aisConsent, consentObject.getRequestUri());

        if (accountConsentValidationResult.isNotValid()) {
            return accountConsentValidationResult;
        }

        if (!aspspProfileService.isTrustedBeneficiariesSupported()) {
            return ValidationResult.invalid(AIS_405, MessageErrorCode.SERVICE_INVALID_405);
        }

        if (aisConsent.isGlobalConsent()) {
            return ValidationResult.valid();
        }

        if (aisConsent.isConsentForAllAvailableAccounts()) {
            return ValidationResult.invalid(AIS_401, MessageErrorCode.CONSENT_INVALID);
        }

        if (doesNotDedicatedConsentHaveRights(aisConsent)) {
            return ValidationResult.invalid(AIS_401, MessageErrorCode.CONSENT_INVALID);
        }

        return ValidationResult.valid();
    }

    private boolean doesNotDedicatedConsentHaveRights(AisConsent aisConsent) {
        AdditionalInformationAccess additionalInformationAccess = aisConsent.getAccess().getAdditionalInformationAccess();

        boolean isConsentDedicated = aisConsent.isConsentForDedicatedAccounts();
        boolean isNotTrustedBeneficiariesSupported = additionalInformationAccess == null ||
                                                         additionalInformationAccess.getTrustedBeneficiaries() == null;

        return isConsentDedicated && isNotTrustedBeneficiariesSupported;
    }
}
