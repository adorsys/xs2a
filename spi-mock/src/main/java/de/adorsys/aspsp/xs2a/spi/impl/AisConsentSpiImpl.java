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

package de.adorsys.aspsp.xs2a.spi.impl;

import de.adorsys.aspsp.xs2a.spi.component.SpiMockJsonConverter;
import de.adorsys.aspsp.xs2a.spi.config.rest.AspspRemoteUrls;
import de.adorsys.aspsp.xs2a.spi.domain.SpiAspspAuthorisationData;
import de.adorsys.aspsp.xs2a.spi.impl.service.KeycloakInvokerService;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.sca.ChallengeData;
import de.adorsys.psd2.xs2a.exception.RestException;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountDetails;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.*;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse.VoidResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType.BANK_OFFERED;
import static de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType.GLOBAL;
import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.FAILURE;
import static de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiAuthorisationStatus.SUCCESS;

@Component
@Slf4j
@RequiredArgsConstructor
public class AisConsentSpiImpl implements AisConsentSpi {
    private static final String DECOUPLED_PSU_MESSAGE = "Please use your BankApp for transaction Authorisation";

    private final AspspRemoteUrls remoteSpiUrls;
    @Qualifier("aspspRestTemplate")
    private final RestTemplate aspspRestTemplate;
    private final KeycloakInvokerService keycloakInvokerService;
    private final SpiMockJsonConverter jsonConverter;

    // Test data is used there for testing purposes to have the possibility to see if AisConsentSpiImpl is being invoked from xs2a.
    // TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
    private static final String TEST_ASPSP_DATA = "Test aspsp data";
    private static final String TEST_MESSAGE = "Test message";

    @Override
    public SpiResponse<SpiInitiateAisConsentResponse> initiateAisConsent(@NotNull SpiContextData spiContextData, SpiAccountConsent accountConsent, AspspConsentData initialAspspConsentData) {
        log.info("AisConsentSpi initiateAisConsent() mock implementation");
        try {
            SpiAccountAccess access = accountConsent.getAccess();
            if (EnumSet.of(GLOBAL, BANK_OFFERED).contains(accountConsent.getAisConsentRequestType())) {
                List<SpiAccountDetails> accountDetailsByPsuId = getAccountDetailsByPsuId(accountConsent);

                List<SpiAccountReference> spiAccountReferences = accountDetailsByPsuId.stream()
                                                                     .map(SpiAccountReference::new)
                                                                     .collect(Collectors.toList());
                access.setAccounts(spiAccountReferences);
                access.setTransactions(spiAccountReferences);
                if (accountConsent.isWithBalance()) {
                    access.setBalances(spiAccountReferences);
                }
            } else {
                access = getAccountDetailsFromReferences(accountConsent);
            }

            return SpiResponse.<SpiInitiateAisConsentResponse>builder()
                       .payload(new SpiInitiateAisConsentResponse(access, false))
                       .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                       .success();

        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiInitiateAisConsentResponse>builder()
                           .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiInitiateAisConsentResponse>builder()
                       .aspspConsentData(initialAspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    public SpiResponse<VoidResponse> revokeAisConsent(@NotNull SpiContextData spiContextData, SpiAccountConsent accountConsent, AspspConsentData aspspConsentData) {
        log.info("AisConsentSpi revokeAisConsent() mock implementation");
        return SpiResponse.<VoidResponse>builder()
                   .payload(SpiResponse.voidResponse())
                   .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                   .message(Collections.singletonList(TEST_MESSAGE))                                      // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                   .success();
    }

    @Override
    public SpiResponse<SpiAuthorisationStatus> authorisePsu(@NotNull SpiContextData spiContextData, @NotNull SpiPsuData psuLoginData, String password, SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            Optional<SpiAspspAuthorisationData> accessToken = keycloakInvokerService.obtainAuthorisationData(
                psuLoginData.getPsuId(), password);
            SpiAuthorisationStatus spiAuthorisationStatus = accessToken.map(t -> SUCCESS)
                                                                .orElse(FAILURE);
            byte[] payload = accessToken.flatMap(jsonConverter::toJson)
                                 .map(String::getBytes)
                                 .orElse(null);
            if (spiAuthorisationStatus == FAILURE) {
                return SpiResponse.<SpiAuthorisationStatus>builder()
                           .aspspConsentData(aspspConsentData.respondWith(payload))
                           .fail(SpiResponseStatus.UNAUTHORIZED_FAILURE);
            }
            return new SpiResponse<>(spiAuthorisationStatus, aspspConsentData.respondWith(payload));
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiAuthorisationStatus>builder()
                           .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiAuthorisationStatus>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }

    }

    @Override
    public SpiResponse<List<SpiAuthenticationObject>> requestAvailableScaMethods(@NotNull SpiContextData spiContextData,
                                                                                 SpiAccountConsent accountConsent,
                                                                                 @NotNull AspspConsentData aspspConsentData) {
        try {
            ResponseEntity<List<SpiAuthenticationObject>> response = aspspRestTemplate.exchange(
                remoteSpiUrls.getScaMethods(), HttpMethod.GET, null, new ParameterizedTypeReference<List<SpiAuthenticationObject>>() {
                }, spiContextData.getPsuData().getPsuId());
            List<SpiAuthenticationObject> spiScaMethods = Optional.ofNullable(response.getBody())
                                                              .orElseGet(Collections::emptyList);
            return new SpiResponse<>(spiScaMethods, aspspConsentData);
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<List<SpiAuthenticationObject>>builder()
                           .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<List<SpiAuthenticationObject>>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }

    }

    @Override
    public @NotNull SpiResponse<SpiAuthorizationCodeResult> requestAuthorisationCode(@NotNull SpiContextData spiContextData,
                                                                                     @NotNull String authenticationMethodId,
                                                                                     @NotNull SpiAccountConsent accountConsent,
                                                                                     @NotNull AspspConsentData aspspConsentData) {
        try {
            aspspRestTemplate.exchange(remoteSpiUrls.getGenerateTanConfirmationForAis(), HttpMethod.POST, null, Void.class, spiContextData.getPsuData().getPsuId());
            return SpiResponse.<SpiAuthorizationCodeResult>builder()
                       // TODO We need to return real payload data from ASPSP https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/489
                       .payload(getDefaultSpiAuthorizationCodeResult())
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                       .message(Collections.singletonList(TEST_MESSAGE))                                      // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                       .success();
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<SpiAuthorizationCodeResult>builder()
                           .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<SpiAuthorizationCodeResult>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<VoidResponse> verifyScaAuthorisation(@NotNull SpiContextData spiContextData, @NotNull SpiScaConfirmation spiScaConfirmation, @NotNull SpiAccountConsent accountConsent, @NotNull AspspConsentData aspspConsentData) {
        try {
            aspspRestTemplate.exchange(remoteSpiUrls.applyStrongUserAuthorisationForAis(), HttpMethod.PUT, new HttpEntity<>(spiScaConfirmation), Void.class);
            return SpiResponse.<VoidResponse>builder()
                       .payload(SpiResponse.voidResponse())
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                       .message(Collections.singletonList(TEST_MESSAGE))                                      // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                       .success();
        } catch (RestException e) {
            if (e.getHttpStatus() == HttpStatus.INTERNAL_SERVER_ERROR) {
                return SpiResponse.<VoidResponse>builder()
                           .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                           .fail(SpiResponseStatus.TECHNICAL_FAILURE);
            }

            return SpiResponse.<VoidResponse>builder()
                       .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))            // added for test purposes TODO remove if some requirements will be received https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/394
                       .fail(SpiResponseStatus.LOGICAL_FAILURE);
        }
    }

    @Override
    @NotNull
    public SpiResponse<SpiAuthorisationDecoupledScaResponse> startScaDecoupled(@NotNull SpiContextData contextData, @NotNull String authorisationId, @Nullable String authenticationMethodId, @NotNull SpiAccountConsent businessObject, @NotNull AspspConsentData aspspConsentData) {
        SpiAuthorisationDecoupledScaResponse response = new SpiAuthorisationDecoupledScaResponse(DECOUPLED_PSU_MESSAGE);

        return SpiResponse.<SpiAuthorisationDecoupledScaResponse>builder()
                   .payload(response)
                   .aspspConsentData(aspspConsentData.respondWith(TEST_ASPSP_DATA.getBytes()))
                   .success();
    }

    private SpiAuthorizationCodeResult getDefaultSpiAuthorizationCodeResult() {
        SpiAuthenticationObject method = new SpiAuthenticationObject();
        method.setAuthenticationMethodId("sms");
        method.setAuthenticationType("SMS_OTP");

        ChallengeData challengeData = new ChallengeData(null, "some data", "some link", 100, null, "info");

        SpiAuthorizationCodeResult resultTmp = new SpiAuthorizationCodeResult();
        resultTmp.setChallengeData(challengeData);
        resultTmp.setSelectedScaMethod(method);

        return resultTmp;
    }

    private List<SpiAccountDetails> getAccountDetailsByPsuId(SpiAccountConsent accountConsent) {
        // TODO correctly handle multiple PSUs for multilevel SCA https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/516
        String psuId = CollectionUtils.isNotEmpty(accountConsent.getPsuData())
                           ? accountConsent.getPsuData().get(0).getPsuId()
                           : null;

        return Optional.ofNullable(aspspRestTemplate.exchange(
            remoteSpiUrls.getAccountDetailsByPsuId(),
            HttpMethod.GET,
            null,
            new ParameterizedTypeReference<List<SpiAccountDetails>>() {
            },
            psuId
        ).getBody())
                   .orElseGet(Collections::emptyList);
    }

    private SpiAccountAccess getAccountDetailsFromReferences(SpiAccountConsent accountConsent) {
        SpiAccountAccess accountAccess = accountConsent.getAccess();

        updateAspspAccountId(accountAccess.getAccounts());
        updateAspspAccountId(accountAccess.getTransactions());
        if (accountConsent.isWithBalance()) {
            updateAspspAccountId(accountAccess.getBalances());
        }
        return accountAccess;
    }

    private void updateAspspAccountId(List<SpiAccountReference> references) {
        for (SpiAccountReference reference : references) {
            Optional<SpiAccountDetails> spiAccountDetails = getAccountDetailsByAccountReference(reference);
            if (spiAccountDetails.isPresent()) {
                SpiAccountDetails details = spiAccountDetails.get();
                reference.setResourceId(details.getResourceId());
                reference.setAspspAccountId(details.getAspspAccountId());
            }
        }
    }

    private Optional<SpiAccountDetails> getAccountDetailsByAccountReference(SpiAccountReference reference) {
        if (reference == null) {
            return Optional.empty();
        }
        List<SpiAccountDetails> accountDetails = Optional.ofNullable(
            aspspRestTemplate.exchange(
                remoteSpiUrls.getAccountDetailsByIban(),
                HttpMethod.GET,
                new HttpEntity<>(null), new ParameterizedTypeReference<List<SpiAccountDetails>>() {
                },
                reference.getIban()
            ).getBody()
        ).orElseGet(Collections::emptyList);

        return accountDetails.stream()
                   .filter(acc -> acc.getCurrency() == reference.getCurrency())
                   .findFirst();
    }
}
