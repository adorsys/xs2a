/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.AisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.CreateConsentRequestValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.domain.MessageErrorCode.*;
import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType.*;

@Service
@RequiredArgsConstructor
//TODO Refactor Service: split responsibilities https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/569
public class ConsentService {
    private final Xs2aAisConsentMapper aisConsentMapper;
    private final SpiToXs2aAccountAccessMapper spiToXs2aAccountAccessMapper;
    private final Xs2aAisConsentService aisConsentService;
    private final AisConsentDataService aisConsentDataService;
    private final AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;
    private final TppService tppService;
    private final AisEndpointAccessCheckerService endpointAccessCheckerService;
    private final SpiContextDataProvider spiContextDataProvider;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final AisConsentSpi aisConsentSpi;
    private final CreateConsentRequestValidator createConsentRequestValidator;
    private final Xs2aEventService xs2aEventService;
    private final AccountReferenceInConsentUpdater accountReferenceUpdater;
    private final SpiErrorMapper spiErrorMapper;
    private final ScaApproachResolver scaApproachResolver;
    private final AspspProfileServiceWrapper aspspProfileService;

    private static final String MESSAGE_ERROR_NO_PSU = "Please provide the PSU identification data";

    /**
     * Performs create consent operation either by filling the appropriate AccountAccess fields with corresponding
     * account details or by getting account details from ASPSP by psuId and filling the appropriate fields in
     * AccountAccess determined by availableAccounts or allPsd2 variables
     *
     * @param request body of create consent request carrying such parameters as AccountAccess, validity terms etc.
     * @param psuData PsuIdData container of authorisation data about PSU
     * @param explicitPreferred is TPP explicit authorisation preferred
     * @param tppRedirectUri URI for redirect SCA approach
     * @return CreateConsentResponse representing the complete response to create consent request
     */
    public ResponseObject<CreateConsentResponse> createAccountConsentsWithResponse(CreateConsentReq request, PsuIdData psuData, boolean explicitPreferred, TppRedirectUri tppRedirectUri) {
        xs2aEventService.recordTppRequest(EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED, request);
        if (aspspProfileService.isPsuInInitialRequestMandated()
                && psuData.isEmpty()) {
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(AIS_400, of(FORMAT_ERROR, MESSAGE_ERROR_NO_PSU))
                       .build();
        }

        ValidationResult validationResult = createConsentRequestValidator.validateRequest(request);

        if (validationResult.isNotValid()) {
            return ResponseObject.<CreateConsentResponse>builder().fail(validationResult.getMessageError()).build();
        }

        if (request.isGlobalOrAllAccountsAccessConsent()) {
            request.setAccess(getAccessForGlobalOrAllAvailableAccountsConsent(request));
        }

        TppInfo tppInfo = tppService.getTppInfo();
        tppInfo.setTppRedirectUri(tppRedirectUri);

        String consentId = aisConsentService.createConsent(request, psuData, tppInfo);

        if (StringUtils.isBlank(consentId)) {
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(AIS_400, of(RESOURCE_UNKNOWN_400))
                       .build();
        }

        AccountConsent accountConsent = getInitialAccountConsent(consentId);

        SpiContextData contextData = spiContextDataProvider.provide(psuData, tppInfo);

        SpiResponse<SpiInitiateAisConsentResponse> initiateAisConsentSpiResponse = aisConsentSpi.initiateAisConsent(contextData, aisConsentMapper.mapToSpiAccountConsent(accountConsent), aisConsentDataService.getAspspConsentDataByConsentId(consentId));
        aisConsentDataService.updateAspspConsentData(initiateAisConsentSpiResponse.getAspspConsentData());

        if (initiateAisConsentSpiResponse.hasError()) {
            aisConsentService.updateConsentStatus(consentId, ConsentStatus.REJECTED);
            return ResponseObject.<CreateConsentResponse>builder()
                       .fail(new MessageError(spiErrorMapper.mapToErrorHolder(initiateAisConsentSpiResponse, ServiceType.AIS)))
                       .build();
        }

        SpiInitiateAisConsentResponse spiResponsePayload = initiateAisConsentSpiResponse.getPayload();
        boolean multilevelScaRequired = spiResponsePayload.isMultilevelScaRequired();

        updateMultilevelSca(consentId, multilevelScaRequired);

        Optional<Xs2aAccountAccess> xs2aAccountAccess = spiToXs2aAccountAccessMapper.mapToAccountAccess(spiResponsePayload.getAccountAccess());
        xs2aAccountAccess.ifPresent(accountAccess ->
                                        accountReferenceUpdater.rewriteAccountAccess(consentId, accountAccess));

        ResponseObject<CreateConsentResponse> createConsentResponseObject = ResponseObject.<CreateConsentResponse>builder().body(new CreateConsentResponse(ConsentStatus.RECEIVED.getValue(), consentId, null, null, null, null, multilevelScaRequired)).build();

        // TODO add actual value during imlementation of multilevel sca https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/515
        if (isEmbeddedOrRedirectScaApproach()
                && authorisationMethodDecider.isImplicitMethod(explicitPreferred, multilevelScaRequired)) {
            proceedImplicitCaseForCreateConsent(createConsentResponseObject.getBody(), psuData, consentId);
        }

        return createConsentResponseObject;
    }

    private void updateMultilevelSca(String consentId, boolean multilevelScaRequired) {
        // default value is false, so we do the call only for non-default (true) case
        if (multilevelScaRequired) {
            aisConsentService.updateMultilevelScaRequired(consentId, multilevelScaRequired);
        }
    }

    /**
     * Returns status of requested consent
     *
     * @param consentId String representation of AccountConsent identification
     * @return ConsentStatus
     */
    public ResponseObject<ConsentStatusResponse> getAccountConsentsStatusById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_AIS_CONSENT_STATUS_REQUEST_RECEIVED);

        AccountConsent validatedAccountConsent = getValidatedAccountConsent(consentId);
        Optional<ConsentStatus> consentStatus =
            Optional.ofNullable(validatedAccountConsent)
                .map(AccountConsent::getConsentStatus);

        ResponseObject.ResponseBuilder<ConsentStatusResponse> responseBuilder = ResponseObject.builder();
        if (consentStatus.isPresent()) {
            responseBuilder = responseBuilder.body(new ConsentStatusResponse(consentStatus.get()));
        } else {
            responseBuilder = responseBuilder.fail(AIS_400, of(CONSENT_UNKNOWN_400));
        }
        return responseBuilder.build();
    }

    /**
     * Terminates account consent on PSU request
     *
     * @param consentId String representation of AccountConsent identification
     * @return VOID
     */
    public ResponseObject<Void> deleteAccountConsentsById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.DELETE_AIS_CONSENT_REQUEST_RECEIVED);
        AccountConsent accountConsent = getValidatedAccountConsent(consentId);

        if (accountConsent != null) {
            // TODO this is not correct. https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/569
            // PSU Data here should be provided from actual request headers. Data in consent is provided in consent
            //TODO provide correct PSU Data to the SPI https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/701
            SpiContextData contextData = getSpiContextData(accountConsent.getPsuIdDataList());

            SpiResponse<VoidResponse> revokeAisConsentResponse = aisConsentSpi.revokeAisConsent(contextData, aisConsentMapper.mapToSpiAccountConsent(accountConsent), aisConsentDataService.getAspspConsentDataByConsentId(consentId));
            aisConsentDataService.updateAspspConsentData(revokeAisConsentResponse.getAspspConsentData());

            if (revokeAisConsentResponse.hasError()) {
                return ResponseObject.<Void>builder()
                           .fail(new MessageError(spiErrorMapper.mapToErrorHolder(revokeAisConsentResponse, ServiceType.AIS)))
                           .build();
            }

            ConsentStatus newConsentStatus = accountConsent.getConsentStatus() == ConsentStatus.RECEIVED
                                                 ? ConsentStatus.REJECTED
                                                 : ConsentStatus.TERMINATED_BY_TPP;

            aisConsentService.updateConsentStatus(consentId, newConsentStatus);
            return ResponseObject.<Void>builder().build();
        }

        return ResponseObject.<Void>builder()
                   .fail(AIS_400, of(CONSENT_UNKNOWN_400)).build();
    }

    /**
     * Returns account consent by its id
     *
     * @param consentId String representation of AccountConsent identification
     * @return AccountConsent requested by consentId
     */
    public ResponseObject<AccountConsent> getAccountConsentById(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_AIS_CONSENT_REQUEST_RECEIVED);

        AccountConsent consent = getInitialAccountConsent(consentId);
        return consent == null
                   ? ResponseObject.<AccountConsent>builder().fail(AIS_403, of(CONSENT_UNKNOWN_403)).build()
                   : ResponseObject.<AccountConsent>builder().body(consent).build();
    }

    @SuppressWarnings("WeakerAccess") // fixes the issue https://github.com/adorsys/xs2a/issues/16
    public ResponseObject<AccountConsent> getValidatedConsent(String consentId, boolean withBalance) {
        AccountConsent accountConsent = getValidatedAccountConsent(consentId);

        if (accountConsent == null) {
            return ResponseObject.<AccountConsent>builder()
                       .fail(AIS_400, of(CONSENT_UNKNOWN_400)).build();
        }

        if (LocalDate.now().compareTo(accountConsent.getValidUntil()) >= 0) {
            return ResponseObject.<AccountConsent>builder()
                       .fail(AIS_401, of(CONSENT_EXPIRED)).build();
        }

        ConsentStatus consentStatus = accountConsent.getConsentStatus();
        if (consentStatus != ConsentStatus.VALID) {
            MessageErrorCode messageErrorCode = consentStatus == ConsentStatus.RECEIVED
                                                    ? CONSENT_INVALID
                                                    : CONSENT_EXPIRED;
            return ResponseObject.<AccountConsent>builder()
                       .fail(AIS_401, of(messageErrorCode)).build();
        }
        if (!accountConsent.isValidFrequency()) {
            return ResponseObject.<AccountConsent>builder()
                       .fail(AIS_429, of(ACCESS_EXCEEDED)).build();
        }
        return ResponseObject.<AccountConsent>builder().body(accountConsent).build();
    }

    @SuppressWarnings("WeakerAccess")  // fixes the issue https://github.com/adorsys/xs2a/issues/16
    public ResponseObject<AccountConsent> getValidatedConsent(String consentId) {
        return getValidatedConsent(consentId, false);
    }

    public ResponseObject<CreateConsentAuthorizationResponse> createConsentAuthorizationWithResponse(PsuIdData psuData, String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        // TODO temporary solution: CMS should be refactored to return response objects instead of Strings, Enums, Booleans etc., so we should receive this error from CMS https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/581
        AccountConsent accountConsent = getValidatedAccountConsent(consentId);
        if (accountConsent != null && accountConsent.isExpired()) {
            return ResponseObject.<CreateConsentAuthorizationResponse>builder()
                       .fail(AIS_401, of(CONSENT_EXPIRED))
                       .build();
        }

        return aisScaAuthorisationServiceResolver.getService().createConsentAuthorization(psuData, consentId)
                   .map(resp -> ResponseObject.<CreateConsentAuthorizationResponse>builder().body(resp).build())
                   .orElseGet(ResponseObject.<CreateConsentAuthorizationResponse>builder()
                                  .fail(AIS_400, of(CONSENT_UNKNOWN_400))
                                  ::build);
    }

    public ResponseObject<UpdateConsentPsuDataResponse> updateConsentPsuData(UpdateConsentPsuDataReq updatePsuData) {
        xs2aEventService.recordAisTppRequest(updatePsuData.getConsentId(), EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED, updatePsuData);

        if (!endpointAccessCheckerService.isEndpointAccessible(updatePsuData.getAuthorizationId(), updatePsuData.getConsentId())) {
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                .fail(AIS_403, of(SERVICE_BLOCKED))
                .build();
        }

        // TODO temporary solution: CMS should be refactored to return response objects instead of Strings, Enums, Booleans etc., so we should receive this error from CMS https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/581
        AccountConsent accountConsent = getValidatedAccountConsent(updatePsuData.getConsentId());
        if (accountConsent != null && accountConsent.isExpired()) {
            return ResponseObject.<UpdateConsentPsuDataResponse>builder()
                       .fail(AIS_401, of(CONSENT_EXPIRED))
                       .build();
        }

        return Optional.ofNullable(aisScaAuthorisationServiceResolver.getService().getAccountConsentAuthorizationById(updatePsuData.getAuthorizationId(), updatePsuData.getConsentId()))
                   .map(conAuth -> getUpdateConsentPsuDataResponse(updatePsuData, conAuth))
                   .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                  .fail(AIS_404, of(RESOURCE_UNKNOWN_404))
                                  ::build);
    }

    private ResponseObject<UpdateConsentPsuDataResponse> getUpdateConsentPsuDataResponse(UpdateConsentPsuDataReq updatePsuData, AccountConsentAuthorization consentAuthorization) {
        UpdateConsentPsuDataResponse response = aisScaAuthorisationServiceResolver.getService().updateConsentPsuData(updatePsuData, consentAuthorization);

        return Optional.ofNullable(response)
                   .map(s -> Optional.ofNullable(s.getMessageError())
                                 .map(e -> ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                               .fail(e)
                                               .build())
                                 .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder().body(response)::build))
                   .orElseGet(ResponseObject.<UpdateConsentPsuDataResponse>builder()
                                  .fail(AIS_400, of(FORMAT_ERROR))
                                  ::build);
    }

    public ResponseObject<Xs2aAuthorisationSubResources> getConsentInitiationAuthorisations(String consentId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        return aisScaAuthorisationServiceResolver.getService().getAuthorisationSubResources(consentId)
                   .map(resp -> ResponseObject.<Xs2aAuthorisationSubResources>builder().body(resp).build())
                   .orElseGet(ResponseObject.<Xs2aAuthorisationSubResources>builder()
                                  .fail(AIS_404, of(RESOURCE_UNKNOWN_404))
                                  ::build);
    }

    /**
     * Gets SCA status of consent authorisation
     *
     * @param consentId       String representation of consent identifier
     * @param authorisationId String representation of authorisation identifier
     * @return Response containing SCA status of the authorisation or corresponding error
     */
    public ResponseObject<ScaStatus> getConsentAuthorisationScaStatus(String consentId, String authorisationId) {
        xs2aEventService.recordAisTppRequest(consentId, EventType.GET_CONSENT_SCA_STATUS_REQUEST_RECEIVED);

        Optional<ScaStatus> scaStatus = aisScaAuthorisationServiceResolver.getService().getAuthorisationScaStatus(consentId, authorisationId);

        if (!scaStatus.isPresent()) {
            return ResponseObject.<ScaStatus>builder()
                       .fail(AIS_403, of(RESOURCE_UNKNOWN_403))
                       .build();
        }

        return ResponseObject.<ScaStatus>builder()
                   .body(scaStatus.get())
                   .build();
    }

    @SuppressWarnings("WeakerAccess")  // fixes the issue https://github.com/adorsys/xs2a/issues/16
    public boolean isValidAccountByAccess(String resourceId, List<AccountReference> allowedAccountData) {
        return CollectionUtils.isNotEmpty(allowedAccountData)
                   && allowedAccountData.stream()
                          .anyMatch(a -> a.getResourceId().equals(resourceId));
    }

    private Xs2aAccountAccess getAccessForGlobalOrAllAvailableAccountsConsent(CreateConsentReq request) {
        return new Xs2aAccountAccess(
            new ArrayList<>(),
            new ArrayList<>(),
            new ArrayList<>(),
            request.getAccess().getAvailableAccounts(),
            request.getAccess().getAllPsd2()
        );
    }

    private AccountConsent getValidatedAccountConsent(String consentId) {
        return Optional.ofNullable(aisConsentService.getAccountConsentById(consentId))
                   .filter(consent -> tppService.getTppId().equals(consent.getTppInfo()
                                                                       .getAuthorisationNumber()))
                   .orElse(null);
    }

    // TODO return Optional instead of orElse(null) https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/585
    private AccountConsent getInitialAccountConsent(String consentId) {
        return Optional.ofNullable(aisConsentService.getInitialAccountConsentById(consentId))
                   .filter(consent -> tppService.getTppId().equals(consent.getTppInfo()
                                                                       .getAuthorisationNumber()))
                   .orElse(null);
    }

    private void proceedImplicitCaseForCreateConsent(CreateConsentResponse response, PsuIdData psuData, String consentId) {
        aisScaAuthorisationServiceResolver.getService().createConsentAuthorization(psuData, consentId)
            .ifPresent(a -> response.setAuthorizationId(a.getAuthorizationId()));
    }

    private boolean isEmbeddedOrRedirectScaApproach() {
        return EnumSet.of(ScaApproach.EMBEDDED, ScaApproach.REDIRECT)
                   .contains(scaApproachResolver.resolveScaApproach());
    }

    private SpiContextData getSpiContextData(List<PsuIdData> psuIdDataList) {
        //TODO provide correct PSU Data to the SPI https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/701
        return spiContextDataProvider.provideWithPsuIdData(CollectionUtils.isNotEmpty(psuIdDataList)
                                                               ? psuIdDataList.get(0)
                                                               : null);
    }
}
