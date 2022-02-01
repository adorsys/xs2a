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
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetAccountListValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetAccountListConsentObject;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
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
public class AccountListService {
    private final AccountSpi accountSpi;

    private final SpiToXs2aAccountDetailsMapper accountDetailsMapper;

    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final AccountReferenceInConsentUpdater accountReferenceUpdater;
    private final SpiErrorMapper spiErrorMapper;

    private final GetAccountListValidator getAccountListValidator;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;
    private final LoggingContextService loggingContextService;

    /**
     * Gets AccountDetails list based on accounts in provided AIS-consent, depending on withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an Consent identification
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @param requestUri  the URI of incoming request
     * @return response with {@link Xs2aAccountListHolder} containing the List of AccountDetails with Balances if requested and granted by consent
     */
    public ResponseObject<Xs2aAccountListHolder> getAccountList(String consentId, boolean withBalance, String requestUri) {
        xs2aEventService.recordConsentTppRequest(consentId, EventType.READ_ACCOUNT_LIST_REQUEST_RECEIVED);

        Optional<AisConsent> aisConsentOptional = aisConsentService.getAccountConsentById(consentId);

        if (aisConsentOptional.isEmpty()) {
            log.info("Consent-ID [{}]. Get account list failed. Account consent not found by id", consentId);
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AisConsent aisConsent = aisConsentOptional.get();

        ValidationResult validationResult = getValidationResultForGetAccountListConsent(withBalance, requestUri, aisConsent);

        if (validationResult.isNotValid()) {
            log.info("Consent-ID [{}], WithBalance [{}], RequestUri [{}]. Get account list - validation failed: {}",
                     consentId, withBalance, requestUri, validationResult.getMessageError());
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<List<SpiAccountDetails>> spiResponse = getSpiResponse(aisConsent, consentId, withBalance);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.info("Consent-ID: [{}]. Get account list failed: couldn't get accounts. Error msg: [{}]",
                     consentId, errorHolder);
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        List<Xs2aAccountDetails> accountDetails = accountDetailsMapper.mapToXs2aAccountDetailsList(spiResponse.getPayload());

        CmsResponse<AisConsent> aisConsentUpdated =
            accountReferenceUpdater.updateAccountReferences(consentId, aisConsent, accountDetails);

        if (aisConsentUpdated.hasError()) {
            log.info("Consent-ID: [{}]. Get account list failed: couldn't update account consent access.", consentId);

            if (CmsError.CHECKSUM_ERROR == aisConsentUpdated.getError()) {
                return ResponseObject.<Xs2aAccountListHolder>builder()
                           .fail(AIS_500, TppMessageInformation.of(CONSENT_VALIDATION_FAILED))
                           .build();
            }

            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(AIS_400, TppMessageInformation.of(CONSENT_UNKNOWN_400))
                       .build();
        }

        loggingContextService.storeConsentStatus(aisConsent.getConsentStatus());

        return getXs2aAccountListHolderResponseObject(consentId, withBalance, requestUri, aisConsentUpdated.getPayload(), accountDetails);
    }

    private ValidationResult getValidationResultForGetAccountListConsent(boolean withBalance, String requestUri, AisConsent aisConsent) {
        GetAccountListConsentObject validatorObject = new GetAccountListConsentObject(aisConsent, withBalance, requestUri);
        return getAccountListValidator.validate(validatorObject);
    }

    private SpiResponse<List<SpiAccountDetails>> getSpiResponse(AisConsent aisConsent, String consentId,
                                                                boolean withBalance) {
        return accountSpi.requestAccountList(accountHelperService.getSpiContextData(),
                                             withBalance,
                                             consentMapper.mapToSpiAccountConsent(aisConsent),
                                             aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    @NotNull
    private ResponseObject<Xs2aAccountListHolder> getXs2aAccountListHolderResponseObject(String consentId,
                                                                                         boolean withBalance,
                                                                                         String requestUri,
                                                                                         AisConsent aisConsent,
                                                                                         List<Xs2aAccountDetails> accountDetails) {
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(accountDetails, aisConsent);

        ResponseObject<Xs2aAccountListHolder> response = ResponseObject.<Xs2aAccountListHolder>builder()
                                                             .body(xs2aAccountListHolder)
                                                             .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(withBalance, TypeAccess.ACCOUNT, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(aisConsent), null, null);

        return response;
    }
}
