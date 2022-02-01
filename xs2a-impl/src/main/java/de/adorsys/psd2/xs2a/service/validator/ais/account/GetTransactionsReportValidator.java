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
import de.adorsys.psd2.xs2a.core.ais.BookingStatus;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.PermittedAccountReferenceValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.TransactionReportAcceptHeaderValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.TransactionsReportByPeriodObject;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_401;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;

/**
 * Validator to be used for validating get transactions report request according to some business rules
 */
@Component
@RequiredArgsConstructor
public class GetTransactionsReportValidator extends AbstractAccountTppValidator<TransactionsReportByPeriodObject> {

    private final PermittedAccountReferenceValidator permittedAccountReferenceValidator;
    private final AccountConsentValidator accountConsentValidator;
    private final AspspProfileServiceWrapper aspspProfileService;
    private final TransactionReportAcceptHeaderValidator transactionReportAcceptHeaderValidator;
    private final AccountReferenceAccessValidator accountReferenceAccessValidator;
    private final OauthConsentValidator oauthConsentValidator;

    /**
     * Validates get transactions report request
     *
     * @param requestObject transaction report information object
     * @return valid result if the consent is valid, invalid result with appropriate error otherwise
     */
    @NotNull
    @Override
    protected ValidationResult executeBusinessValidation(TransactionsReportByPeriodObject requestObject) { // NOPMD
        AisConsent aisConsent = requestObject.getAisConsent();

        if (aisConsent.isConsentWithNotIbanAccount() && !aisConsent.isConsentForAllAvailableAccounts() && !aisConsent.isGlobalConsent()) {
            return ValidationResult.invalid(AIS_401, CONSENT_INVALID);
        }

        ValidationResult acceptHeaderValidationResult = transactionReportAcceptHeaderValidator.validate(requestObject.getAcceptHeader());
        if (acceptHeaderValidationResult.isNotValid()) {
            return acceptHeaderValidationResult;
        }

        ValidationResult validationResult = validateTransactionReportParameters(requestObject.getEntryReferenceFrom(), requestObject.getDeltaList(), requestObject.getDateFrom());
        if (validationResult.isNotValid()) {
            return validationResult;
        }

        ValidationResult accountReferenceValidationResult = accountReferenceAccessValidator.validate(aisConsent,
                                                                                                     requestObject.getTransactions(), requestObject.getAccountId(), aisConsent.getAisConsentRequestType());
        if (accountReferenceValidationResult.isNotValid()) {
            return accountReferenceValidationResult;
        }

        ValidationResult permittedAccountReferenceValidationResult =
            permittedAccountReferenceValidator.validate(aisConsent, requestObject.getAccountId(), requestObject.isWithBalance());

        if (permittedAccountReferenceValidationResult.isNotValid()) {
            return permittedAccountReferenceValidationResult;
        }

        BookingStatus bookingStatus = requestObject.getBookingStatus();
        if (isNotSupportedBookingStatus(bookingStatus)) {
            return ValidationResult.invalid(AIS_400, TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_BOOKING_STATUS, bookingStatus.getValue()));
        }

        ValidationResult oauthConsentValidationResult = oauthConsentValidator.validate(aisConsent);
        if (oauthConsentValidationResult.isNotValid()) {
            return oauthConsentValidationResult;
        }

        return accountConsentValidator.validate(aisConsent, requestObject.getRequestUri());
    }

    private ValidationResult validateTransactionReportParameters(String entryReferenceFrom, Boolean deltaList, LocalDate dateFrom) {
        List<TppMessageInformation> tppMessageInformationList = new ArrayList<>();
        boolean isEntryReferenceFromSupported = aspspProfileService.isEntryReferenceFromSupported();
        boolean isEntryReferenceFromPresentInRequest = StringUtils.isNotBlank(entryReferenceFrom);
        boolean isNotSearchByPeriod = dateFrom == null;

        if (isDeltaAccessParameterNotSupported(isEntryReferenceFromPresentInRequest, isEntryReferenceFromSupported, isNotSearchByPeriod)) {
            tppMessageInformationList.add(TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_ENTRY_REFERENCE_FROM));
        }
        boolean isDeltaListSupported = aspspProfileService.isDeltaListSupported();
        boolean isDeltaListPresentInRequest = BooleanUtils.isTrue(deltaList);

        if (isDeltaAccessParameterNotSupported(isDeltaListPresentInRequest, isDeltaListSupported, isNotSearchByPeriod)) {
            tppMessageInformationList.add(TppMessageInformation.of(PARAMETER_NOT_SUPPORTED_DELTA_LIST));
        }

        boolean isAllParametersPresentAndSupported = isDeltaListSupported
                                                         && isEntryReferenceFromSupported
                                                         && isEntryReferenceFromPresentInRequest
                                                         && isDeltaListPresentInRequest;
        if (isAllParametersPresentAndSupported) {
            tppMessageInformationList.add(TppMessageInformation.of(FORMAT_ERROR_MULTIPLE_DELTA_REPORT));
        }

        return tppMessageInformationList.isEmpty()
                   ? ValidationResult.valid()
                   : ValidationResult.invalid(AIS_400, tppMessageInformationList.toArray(new TppMessageInformation[0]));
    }

    private boolean isDeltaAccessParameterNotSupported(boolean isParameterPresentInRequest, boolean isParameterSupportedInProfile, boolean isNotSearchByPeriod) {
        return isParameterPresentInRequest && !isParameterSupportedInProfile && isNotSearchByPeriod;
    }

    private boolean isNotSupportedBookingStatus(BookingStatus bookingStatus) {
        return !aspspProfileService.getAvailableBookingStatuses()
                    .contains(bookingStatus);
    }
}
