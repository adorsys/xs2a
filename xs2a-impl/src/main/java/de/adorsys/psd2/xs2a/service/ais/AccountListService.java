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

package de.adorsys.psd2.xs2a.service.ais;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetails;
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountListHolder;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
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
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;

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
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;

    /**
     * Gets AccountDetails list based on accounts in provided AIS-consent, depending on withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @param requestUri  the URI of incoming request
     * @return response with {@link Xs2aAccountListHolder} containing the List of AccountDetails with Balances if requested and granted by consent
     */
    public ResponseObject<Xs2aAccountListHolder> getAccountList(String consentId, boolean withBalance, String requestUri) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_ACCOUNT_LIST_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        UUID internalRequestId = requestProviderService.getInternalRequestId();
        UUID xRequestId = requestProviderService.getRequestId();

        if (!accountConsentOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}]. Get account list failed. Account consent not found by id",
                     internalRequestId, xRequestId, consentId);
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();

        ValidationResult validationResult = getValidationResultForGetAccountListConsent(withBalance, requestUri, accountConsent);

        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID [{}], WithBalance [{}], RequestUri [{}]. Get account list - validation failed: {}",
                     internalRequestId, xRequestId, consentId, withBalance, requestUri, validationResult.getMessageError());
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<List<SpiAccountDetails>> spiResponse = getSpiResponse(accountConsent, consentId, withBalance);

        if (spiResponse.hasError()) {
            ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get account list failed: couldn't get accounts. Error msg: [{}]",
                     internalRequestId, xRequestId, consentId, errorHolder);
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(new MessageError(errorHolder))
                       .build();
        }

        List<Xs2aAccountDetails> accountDetails = accountDetailsMapper.mapToXs2aAccountDetailsList(spiResponse.getPayload());

        Optional<AccountConsent> accountConsentUpdated =
            accountReferenceUpdater.updateAccountReferences(consentId, accountConsent.getAccess(), accountDetails);

        if (!accountConsentUpdated.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Consent-ID: [{}]. Get account list failed: couldn't update account consent access. Actual consent not found by id",
                     internalRequestId, xRequestId, consentId);
            return ResponseObject.<Xs2aAccountListHolder>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        return getXs2aAccountListHolderResponseObject(consentId, withBalance, requestUri, accountConsentUpdated.get(), accountDetails);
    }

    private ValidationResult getValidationResultForGetAccountListConsent(boolean withBalance, String requestUri, AccountConsent accountConsent) {
        GetAccountListConsentObject validatorObject = new GetAccountListConsentObject(accountConsent, withBalance, requestUri);
        return getAccountListValidator.validate(validatorObject);
    }

    private SpiResponse<List<SpiAccountDetails>> getSpiResponse(AccountConsent accountConsent, String consentId,
                                                                boolean withBalance) {
        return accountSpi.requestAccountList(accountHelperService.getSpiContextData(),
                                             withBalance,
                                             consentMapper.mapToSpiAccountConsent(accountConsent),
                                             aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    @NotNull
    private ResponseObject<Xs2aAccountListHolder> getXs2aAccountListHolderResponseObject(String consentId,
                                                                                         boolean withBalance,
                                                                                         String requestUri,
                                                                                         AccountConsent accountConsent,
                                                                                         List<Xs2aAccountDetails> accountDetails) {
        Xs2aAccountListHolder xs2aAccountListHolder = new Xs2aAccountListHolder(accountDetails, accountConsent);

        ResponseObject<Xs2aAccountListHolder> response = ResponseObject.<Xs2aAccountListHolder>builder()
                                                             .body(xs2aAccountListHolder)
                                                             .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(withBalance, TypeAccess.ACCOUNT, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(accountConsent));

        return response;
    }
}
