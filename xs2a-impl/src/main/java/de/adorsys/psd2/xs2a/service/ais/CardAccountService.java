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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCardAccountListHolder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetCardAccountDetailsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetCardAccountListValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetCardAccountDetailsRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetCardAccountListConsentObject;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiCardAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.CardAccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_400;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.AIS_500;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_VALIDATION_FAILED;

@Slf4j
@Service
@AllArgsConstructor
public class CardAccountService {
    private final CardAccountSpi cardAccountSpi;

    private final SpiToXs2aAccountDetailsMapper accountDetailsMapper;
    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final AccountReferenceInConsentUpdater accountReferenceUpdater;
    private final SpiErrorMapper spiErrorMapper;

    private final GetCardAccountListValidator getCardAccountListValidator;
    private final GetCardAccountDetailsValidator getCardAccountDetailsValidator;

    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;
    private final LoggingContextService loggingContextService;

    /**
     * Gets CardAccount list based on card accounts in provided AIS-consent, depending on
     * AccountAccess in AIS-consent Balances are passed al/AccountControllerTestong with CardAccountDetails.
     *
     * @param consentId  String representing an Consent identification
     * @param requestUri the URI of incoming request
     * @return response with {@link Xs2aCardAccountListHolder} containing the List of CardAccountDetails with Balances and granted by consent
     */
    public ResponseObject<Xs2aCardAccountListHolder> getCardAccountList(String consentId, String requestUri) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.READ_CARD_ACCOUNT_LIST_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Consent-ID [{}]. Get card account list failed. Account consent not found by ID", consentId);
            return ResponseObject.<Xs2aCardAccountListHolder>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();

        ValidationResult validationResult = getCardAccountListValidator.validate(new GetCardAccountListConsentObject(aisConsent, requestUri));
        if (validationResult.isNotValid()) {
            log.info("Consent-ID [{}], RequestUri [{}]. Get card account list - validation failed: {}",
                     consentId, requestUri, validationResult.getMessageError());
            return ResponseObject.<Xs2aCardAccountListHolder>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<List<SpiCardAccountDetails>> spiResponse = getAccountListSpiResponse(aisConsent, consentId);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.info("Consent-ID: [{}]. Get card account list failed: SPI level error msg: [{}]",
                     consentId, errorHolder);
            return ResponseObject.<Xs2aCardAccountListHolder>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        List<Xs2aCardAccountDetails> accountDetails = accountDetailsMapper.mapToXs2aCardAccountDetailsList(spiResponse.getPayload());

        CmsResponse<AisConsent> aisConsentUpdated =
            accountReferenceUpdater.updateCardAccountReferences(consentId, aisConsent, accountDetails);

        if (aisConsentUpdated.hasError()) {
            log.info("Consent-ID: [{}]. Get card account list failed: couldn't update account consent access.",
                     consentId);

            if (CmsError.CHECKSUM_ERROR == aisConsentUpdated.getError()) {
                return ResponseObject.<Xs2aCardAccountListHolder>builder()
                           .fail(AIS_500, TppMessageInformation.of(CONSENT_VALIDATION_FAILED))
                           .build();
            }

            return ResponseObject.<Xs2aCardAccountListHolder>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        return getXs2aAccountListHolderResponseObject(consentId, requestUri, aisConsentUpdated.getPayload(), accountDetails);
    }

    /**
     * Gets CardAccount details based on card account ID and provided AIS-consent, depending on
     * AccountAccess in AIS-consent.
     *
     * @param consentId  String representing an Consent identification
     * @param accountId  identifier of the account
     * @param requestUri the URI of incoming request
     * @return response with {@link Xs2aCardAccountDetailsHolder} containing Xs2aCardAccountDetails object instance
     */
    public ResponseObject<Xs2aCardAccountDetailsHolder> getCardAccountDetails(String consentId, String accountId, String requestUri) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.READ_CARD_ACCOUNT_DETAILS_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Account-ID [{}], Consent-ID [{}]. Get card account details failed. Account consent not found by ID",
                     accountId, consentId);
            return ResponseObject.<Xs2aCardAccountDetailsHolder>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();

        ValidationResult validationResult = getCardAccountDetailsValidator.validate(new GetCardAccountDetailsRequestObject(aisConsent, accountId, requestUri));
        if (validationResult.isNotValid()) {
            log.info("Account-ID [{}], Consent-ID [{}], RequestUri [{}]. Get card account details - validation failed: {}",
                     accountId, consentId, requestUri, validationResult.getMessageError());

            return ResponseObject.<Xs2aCardAccountDetailsHolder>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<SpiCardAccountDetails> spiResponse = getAccountDetailsSpiResponse(aisConsent, consentId, accountId);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.info("Account-ID [{}], Consent-ID: [{}]. Get card account details failed: couldn't get account details. Error msg: [{}]",
                     accountId, consentId, errorHolder);
            return ResponseObject.<Xs2aCardAccountDetailsHolder>builder()
                       .fail(errorHolder)
                       .build();
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        return getXs2aAccountDetailsHolderResponseObject(consentId, requestUri, aisConsent, spiResponse.getPayload());
    }

    private SpiResponse<SpiCardAccountDetails> getAccountDetailsSpiResponse(AisConsent aisConsent,
                                                                            String consentId, String accountId) {
        AccountAccess access = aisConsent.getAccess();
        SpiAccountReference requestedAccountReference = aisConsent.isGlobalConsent() ?
                                                            SpiAccountReference.builder().resourceId(accountId).build() :
                                                            accountHelperService.findAccountReference(access.getAccounts(), accountId);

        return cardAccountSpi.requestCardAccountDetailsForAccount(accountHelperService.getSpiContextData(),
                                                                  requestedAccountReference,
                                                                  consentMapper.mapToSpiAccountConsent(aisConsent),
                                                                  aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    @NotNull
    private ResponseObject<Xs2aCardAccountDetailsHolder> getXs2aAccountDetailsHolderResponseObject(String consentId,
                                                                                                   String requestUri,
                                                                                                   AisConsent aisConsent,
                                                                                                   SpiCardAccountDetails spiAccountDetails) {
        Xs2aCardAccountDetails cardAccountDetails = accountDetailsMapper.mapToXs2aCardAccountDetails(spiAccountDetails);

        Xs2aCardAccountDetailsHolder xs2aCardAccountDetailsHolder = new Xs2aCardAccountDetailsHolder(cardAccountDetails, aisConsent);

        ResponseObject<Xs2aCardAccountDetailsHolder> response = ResponseObject.<Xs2aCardAccountDetailsHolder>builder()
                                                                    .body(xs2aCardAccountDetailsHolder)
                                                                    .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(false, TypeAccess.ACCOUNT, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(aisConsent),
                                           spiAccountDetails.getResourceId(), null);

        return response;
    }


    private SpiResponse<List<SpiCardAccountDetails>> getAccountListSpiResponse(AisConsent aisConsent, String consentId) {
        return cardAccountSpi.requestCardAccountList(accountHelperService.getSpiContextData(),
                                                     consentMapper.mapToSpiAccountConsent(aisConsent),
                                                     aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    @NotNull
    private ResponseObject<Xs2aCardAccountListHolder> getXs2aAccountListHolderResponseObject(String consentId,
                                                                                             String requestUri,
                                                                                             AisConsent aisConsent,
                                                                                             List<Xs2aCardAccountDetails> accountDetails) {
        Xs2aCardAccountListHolder xs2aCardAccountListHolder = new Xs2aCardAccountListHolder(accountDetails, aisConsent);

        ResponseObject<Xs2aCardAccountListHolder> response = ResponseObject.<Xs2aCardAccountListHolder>builder()
                                                                 .body(xs2aCardAccountListHolder)
                                                                 .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(false, TypeAccess.ACCOUNT, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(aisConsent), null, null);

        return response;
    }
}
