/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.profile.AdditionalInformationAccess;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
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
