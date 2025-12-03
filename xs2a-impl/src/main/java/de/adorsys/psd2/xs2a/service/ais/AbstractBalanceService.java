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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR_UNKNOWN_ACCOUNT;

/**
 * Abstract base class for balance retrieval services.
 * Provides common functionality for retrieving account balances.
 */
@Slf4j
@AllArgsConstructor
public abstract class AbstractBalanceService {
    /**
     * Holder for account-related services used in balance operations.
     */
    protected final AccountServicesHolder accountServicesHolder;
    private final Xs2aEventService xs2aEventService;
    private final LoggingContextService loggingContextService;

    /**
     * Gets Balances Report based on consentId and accountId
     *
     * @param consentId  String representing an Consent identification
     * @param accountId  String representing a PSU`s Account at ASPSP
     * @param requestUri the URI of incoming request
     * @return Balances Report based on consentId and accountId
     */
    public ResponseObject<Xs2aBalancesReport> getBalancesReport(String consentId, String accountId, String requestUri) {
        xs2aEventService.recordConsentTppRequest(consentId, getEventType());

        Optional<AisConsent> aisConsentOptional = accountServicesHolder.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Account-ID [{}], Consent-ID [{}]. Get balances report failed. Account consent not found by ID",
                     accountId, consentId);
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();

        ValidationResult validationResult = getValidationResultForCommonAccountBalanceRequest(accountId, requestUri, aisConsent);

        if (validationResult.isNotValid()) {
            log.info("Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get balances report - validation failed: {}",
                     accountId, consentId, requestUri, validationResult.getMessageError());
            return ResponseObject.<Xs2aBalancesReport>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        final var spiAccountReference = getRequestedAccountReference(aisConsent, accountId);
        if (spiAccountReference == null) {
            log.info("Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get balance report with incorrect account id",
                accountId, consentId, requestUri);

            return ResponseObject.<Xs2aBalancesReport>builder()
                .fail(new MessageError(AIS_400, TppMessageInformation.of(FORMAT_ERROR_UNKNOWN_ACCOUNT)))
                .build();
        }

        SpiResponse<List<SpiAccountBalance>> spiResponse = getSpiResponse(aisConsent, consentId, spiAccountReference);

        if (spiResponse.hasError()) {
            return checkSpiResponse(consentId, accountId, spiResponse);
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        return getXs2aBalancesReportResponseObject(aisConsent, accountId, consentId, requestUri, spiResponse.getPayload());
    }

    /**
     * Gets the requested account reference from the consent.
     *
     * @param aisConsent the AIS consent
     * @param accountId the account ID
     * @return the SPI account reference
     */
    protected SpiAccountReference getRequestedAccountReference(AisConsent aisConsent, String accountId) {
        final AccountAccess access = aisConsent.getAspspAccountAccesses();
        return accountServicesHolder.findAccountReference(access.getBalances(), accountId);
    }

    /**
     * Returns the event type for this balance service.
     *
     * @return the event type
     */
    protected abstract EventType getEventType();

    /**
     * Validates the common account balance request.
     *
     * @param accountId the account ID
     * @param requestUri the request URI
     * @param accountConsent the account consent
     * @return validation result
     */
    protected abstract ValidationResult getValidationResultForCommonAccountBalanceRequest(String accountId, String requestUri, AisConsent accountConsent);

    /**
     * Retrieves the SPI response containing account balances.
     *
     * @param aisConsent the AIS consent
     * @param consentId the consent ID
     * @param accountReference the account reference
     * @return SPI response with list of account balances
     */
    protected abstract SpiResponse<List<SpiAccountBalance>> getSpiResponse(AisConsent aisConsent, String consentId, SpiAccountReference accountReference);

    /**
     * Checks the SPI response and builds appropriate error response if needed.
     *
     * @param consentId the consent ID
     * @param accountId the account ID
     * @param spiResponse the SPI response to check
     * @return response object with balances report or error
     */
    protected abstract ResponseObject<Xs2aBalancesReport> checkSpiResponse(String consentId, String accountId, SpiResponse<List<SpiAccountBalance>> spiResponse);

    /**
     * Creates the XS2A balances report response object.
     *
     * @param accountConsent the account consent
     * @param accountId the account ID
     * @param consentId the consent ID
     * @param requestUri the request URI
     * @param payload the list of SPI account balances
     * @return response object with balances report
     */
    protected abstract ResponseObject<Xs2aBalancesReport> getXs2aBalancesReportResponseObject(AisConsent accountConsent,
                                                                                              String accountId,
                                                                                              String consentId,
                                                                                              String requestUri,
                                                                                              List<SpiAccountBalance> payload);
}
