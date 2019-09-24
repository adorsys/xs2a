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
import de.adorsys.psd2.xs2a.domain.account.Xs2aAccountDetailsHolder;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountDetailsMapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.account.GetAccountDetailsValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.CommonAccountRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.AccountSpi;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_400;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.AIS_400;

@Slf4j
@Service
@AllArgsConstructor
public class AccountDetailsService {
    private final AccountSpi accountSpi;

    private final SpiToXs2aAccountDetailsMapper accountDetailsMapper;

    private final Xs2aAisConsentService aisConsentService;
    private final Xs2aAisConsentMapper consentMapper;
    private final TppService tppService;
    private final Xs2aEventService xs2aEventService;
    private final SpiErrorMapper spiErrorMapper;

    private final GetAccountDetailsValidator getAccountDetailsValidator;
    private final RequestProviderService requestProviderService;
    private final SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    private final AccountHelperService accountHelperService;

    /**
     * Gets AccountDetails based on accountId, details get checked with provided AIS-consent, depending on
     * withBalance variable and
     * AccountAccess in AIS-consent Balances are passed along with AccountDetails.
     *
     * @param consentId   String representing an AccountConsent identification
     * @param accountId   String representing a PSU`s Account at ASPSP
     * @param withBalance boolean representing if the responded AccountDetails should contain
     * @param requestUri  the URI of incoming request
     * @return response with {@link Xs2aAccountDetailsHolder} based on accountId with Balances if requested and granted by consent
     */
    public ResponseObject<Xs2aAccountDetailsHolder> getAccountDetails(String consentId, String accountId,
                                                                      boolean withBalance, String requestUri) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.READ_ACCOUNT_DETAILS_REQUEST_RECEIVED);

        Optional<AccountConsent> accountConsentOptional = aisConsentService.getAccountConsentById(consentId);

        UUID internalRequestId = requestProviderService.getInternalRequestId();
        UUID xRequestId = requestProviderService.getRequestId();

        if (!accountConsentOptional.isPresent()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}]. Get account details failed. Account consent not found by id",
                     internalRequestId, xRequestId, accountId, consentId);
            return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = accountConsentOptional.get();
        ValidationResult validationResult = getValidationResultForCommonAccountRequest(accountId, withBalance, requestUri, accountConsent);

        if (validationResult.isNotValid()) {
            log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID [{}], WithBalance [{}], RequestUri [{}]. Get account details - validation failed: {}",
                     internalRequestId, xRequestId, accountId, consentId, withBalance, requestUri, validationResult.getMessageError());
            return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                       .fail(validationResult.getMessageError())
                       .build();
        }

        SpiResponse<SpiAccountDetails> spiResponse = getSpiResponse(accountConsent, consentId, accountId, withBalance);

        if (spiResponse.hasError()) {
            return checkSpiResponse(consentId, accountId, spiResponse);
        }

        return getXs2aAccountDetailsHolderResponseObject(consentId, withBalance, requestUri, accountConsent, spiResponse.getPayload());
    }

    private ValidationResult getValidationResultForCommonAccountRequest(String accountId, boolean withBalance, String requestUri, AccountConsent accountConsent) {
        CommonAccountRequestObject validatorObject = new CommonAccountRequestObject(accountConsent, accountId, withBalance, requestUri);
        return getAccountDetailsValidator.validate(validatorObject);
    }

    private SpiResponse<SpiAccountDetails> getSpiResponse(AccountConsent accountConsent, String consentId,
                                                          String accountId, boolean withBalance) {
        Xs2aAccountAccess access = accountConsent.getAccess();
        SpiAccountReference requestedAccountReference = accountHelperService.findAccountReference(access.getAccounts(), accountId);

        return accountSpi.requestAccountDetailForAccount(accountHelperService.getSpiContextData(),
                                                         withBalance, requestedAccountReference,
                                                         consentMapper.mapToSpiAccountConsent(accountConsent),
                                                         aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(consentId));
    }

    private ResponseObject<Xs2aAccountDetailsHolder> checkSpiResponse(String consentId, String accountId, SpiResponse<SpiAccountDetails> spiResponse) {
        UUID internalRequestId = requestProviderService.getInternalRequestId();
        UUID xRequestId = requestProviderService.getRequestId();

        ErrorHolder errorHolder = spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS);
        log.info("InR-ID: [{}], X-Request-ID: [{}], Account-ID [{}], Consent-ID: [{}]. Get account details failed: couldn't get account details. Error msg: [{}]",
                 internalRequestId, xRequestId, accountId, consentId, errorHolder);
        return ResponseObject.<Xs2aAccountDetailsHolder>builder()
                   .fail(errorHolder)
                   .build();
    }

    @NotNull
    private ResponseObject<Xs2aAccountDetailsHolder> getXs2aAccountDetailsHolderResponseObject(String consentId,
                                                                                               boolean withBalance,
                                                                                               String requestUri,
                                                                                               AccountConsent accountConsent,
                                                                                               SpiAccountDetails spiAccountDetails) {
        Xs2aAccountDetails accountDetails = accountDetailsMapper.mapToXs2aAccountDetails(spiAccountDetails);
        Xs2aAccountDetailsHolder xs2aAccountDetailsHolder = new Xs2aAccountDetailsHolder(accountDetails, accountConsent);

        ResponseObject<Xs2aAccountDetailsHolder> response = ResponseObject.<Xs2aAccountDetailsHolder>builder()
                                                                .body(xs2aAccountDetailsHolder)
                                                                .build();

        aisConsentService.consentActionLog(tppService.getTppId(), consentId,
                                           accountHelperService.createActionStatus(withBalance, TypeAccess.ACCOUNT, response),
                                           requestUri, accountHelperService.needsToUpdateUsage(accountConsent));

        return response;
    }
}
