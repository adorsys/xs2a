/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.AbstractAisTppValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.PermittedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.TransactionsReportByPeriodObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.FORMAT_ERROR;
import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.PARAMETER_NOT_SUPPORTED;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;

/**
 * Validator to be used for validating get transactions report request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetTransactionsReportValidator extends AbstractAisTppValidator<TransactionsReportByPeriodObject> {
    static final String ENTRY_REFERENCE_FROM_NOT_SUPPORTED_ERROR_TEXT = "Parameter 'entryReferenceFrom' is not supported by ASPSP";
    static final String DELTA_LIST_NOT_SUPPORTED_ERROR_TEXT = "Parameter 'deltaList' is not supported by ASPSP";
    static final String ONE_DELTA_REPORT_CAN_BE_PRESENT_ERROR_TEXT = "Only one delta report query parameter can be present in request";
    private final PermittedAccountReferenceValidator permittedAccountReferenceValidator;
    private final AccountConsentValidator accountConsentValidator;
    private final AspspProfileServiceWrapper aspspProfileService;

    /**
     * Validates get transactions report request
     *
     * @param requestObject transaction report information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(TransactionsReportByPeriodObject requestObject) {
        ValidationResult validationResult = validateTransactionReportParameters(requestObject.getEntryReferenceFrom(), requestObject.getDeltaList());
        if (validationResult.isNotValid()) {
            return validationResult;
        }

        AccountConsent accountConsent = requestObject.getAccountConsent();

        ValidationResult permittedAccountReferenceValidationResult =
            permittedAccountReferenceValidator.validate(accountConsent, requestObject.getTransactions(), requestObject.getAccountId(), requestObject.isWithBalance());

        if (permittedAccountReferenceValidationResult.isNotValid()) {
            return permittedAccountReferenceValidationResult;
        }

        return accountConsentValidator.validate(accountConsent, requestObject.getRequestUri());
    }

    private ValidationResult validateTransactionReportParameters(String entryReferenceFrom, Boolean deltaList) {
        List<TppMessageInformation> tppMessageInformationList = new ArrayList<>();
        boolean isEntryReferenceFromSupported = aspspProfileService.isEntryReferenceFromSupported();
        boolean isEntryReferenceFromPresentInRequest = StringUtils.isNotBlank(entryReferenceFrom);

        if (isEntryReferenceFromPresentInRequest && !isEntryReferenceFromSupported) {
            tppMessageInformationList.add(TppMessageInformation.of(PARAMETER_NOT_SUPPORTED, ENTRY_REFERENCE_FROM_NOT_SUPPORTED_ERROR_TEXT));
        }
        boolean isDeltaListSupported = aspspProfileService.isDeltaListSupported();
        boolean isDeltaListPresentInRequest = BooleanUtils.isTrue(deltaList);

        if (isDeltaListPresentInRequest && !isDeltaListSupported) {
            tppMessageInformationList.add(TppMessageInformation.of(PARAMETER_NOT_SUPPORTED, DELTA_LIST_NOT_SUPPORTED_ERROR_TEXT));
        }

        boolean isAllParametersPresentAndSupported = isDeltaListSupported
                                                         && isEntryReferenceFromSupported
                                                         && isEntryReferenceFromPresentInRequest
                                                         && isDeltaListPresentInRequest;
        if (isAllParametersPresentAndSupported) {
            tppMessageInformationList.add(TppMessageInformation.of(FORMAT_ERROR, ONE_DELTA_REPORT_CAN_BE_PRESENT_ERROR_TEXT));
        }

        return tppMessageInformationList.isEmpty()
                   ? ValidationResult.valid()
                   : ValidationResult.invalid(AIS_400, tppMessageInformationList.toArray(new TppMessageInformation[0]));
    }
}
