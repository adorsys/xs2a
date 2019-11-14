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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.*;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentRequest;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.consent.config.CmsRestException;
import de.adorsys.psd2.consent.config.PisCommonPaymentRemoteUrls;
import de.adorsys.psd2.xs2a.core.pis.PaymentAuthorisationType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class PisCommonPaymentServiceRemote implements PisCommonPaymentServiceEncrypted {
    @Qualifier("consentRestTemplate")
    private final RestTemplate consentRestTemplate;
    private final PisCommonPaymentRemoteUrls remotePisCommonPaymentUrls;

    @Override
    public CmsResponse<CreatePisCommonPaymentResponse> createCommonPayment(PisPaymentInfo request) {
        try {
            CreatePisCommonPaymentResponse body = consentRestTemplate.postForEntity(remotePisCommonPaymentUrls.createPisCommonPayment(), request, CreatePisCommonPaymentResponse.class).getBody();
            return CmsResponse.<CreatePisCommonPaymentResponse>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote common payment creation failed");
        }

        return CmsResponse.<CreatePisCommonPaymentResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<TransactionStatus> getPisCommonPaymentStatusById(String paymentId) {
        return CmsResponse.<TransactionStatus>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<PisCommonPaymentResponse> getCommonPaymentById(String paymentId) {
        try {
            PisCommonPaymentResponse body = consentRestTemplate.getForEntity(remotePisCommonPaymentUrls.getPisCommonPaymentById(), PisCommonPaymentResponse.class, paymentId).getBody();
            return CmsResponse.<PisCommonPaymentResponse>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote get common payment by ID failed");
        }

        return CmsResponse.<PisCommonPaymentResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateCommonPaymentStatusById(String paymentId, TransactionStatus status) {
        try {
            HttpStatus statusCode = consentRestTemplate.exchange(remotePisCommonPaymentUrls.updatePisCommonPaymentStatus(), HttpMethod.PUT,
                                                                 null, Void.class, paymentId, status.getTransactionStatus()).getStatusCode();

            return CmsResponse.<Boolean>builder()
                       .payload(statusCode == HttpStatus.OK)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote update common payment status by ID failed");
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<String> getDecryptedId(String encryptedId) {
        try {
            String body = consentRestTemplate.getForEntity(remotePisCommonPaymentUrls.getPaymentIdByEncryptedString(), String.class, encryptedId).getBody();
            return CmsResponse.<String>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote decrypt encrypted common payment ID failed");
        }

        return CmsResponse.<String>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<CreatePisAuthorisationResponse> createAuthorization(String paymentId, CreatePisAuthorisationRequest request) {
        try {
            CreatePisAuthorisationResponse body = consentRestTemplate.postForEntity(remotePisCommonPaymentUrls.createPisAuthorisation(),
                                                                                    request, CreatePisAuthorisationResponse.class, paymentId).getBody();
            return CmsResponse.<CreatePisAuthorisationResponse>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("No authorisation was created for the paymentId {}", paymentId);
        }

        return CmsResponse.<CreatePisAuthorisationResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<CreatePisAuthorisationResponse> createAuthorizationCancellation(String paymentId, CreatePisAuthorisationRequest request) {
        try {
            CreatePisAuthorisationResponse body = consentRestTemplate.postForEntity(remotePisCommonPaymentUrls.createPisAuthorisationCancellation(), request, CreatePisAuthorisationResponse.class, paymentId).getBody();
            return CmsResponse.<CreatePisAuthorisationResponse>builder()
                       .payload(body)
                       .build();

        } catch (CmsRestException cmsRestException) {
            log.warn("No cancellation authorisation was created for the paymentId {}", paymentId);
        }

        return CmsResponse.<CreatePisAuthorisationResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<UpdatePisCommonPaymentPsuDataResponse> updatePisAuthorisation(String authorisationId, UpdatePisCommonPaymentPsuDataRequest request) {
        try {
            UpdatePisCommonPaymentPsuDataResponse body = consentRestTemplate.exchange(remotePisCommonPaymentUrls.updatePisAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request),
                                                                                      UpdatePisCommonPaymentPsuDataResponse.class, request.getAuthorizationId()).getBody();
            return CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote update authorisation failed for authorisation id {}", authorisationId);
        }

        return CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updatePisAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        try {
            consentRestTemplate.put(remotePisCommonPaymentUrls.updatePisAuthorisationStatus(), null, authorisationId, scaStatus.getValue());
            return CmsResponse.<Boolean>builder()
                       .payload(true)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Couldn't update authorisation status by authorisationId {}", authorisationId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<UpdatePisCommonPaymentPsuDataResponse> updatePisCancellationAuthorisation(String authorisationId, UpdatePisCommonPaymentPsuDataRequest request) {
        try {
            UpdatePisCommonPaymentPsuDataResponse body = consentRestTemplate.exchange(remotePisCommonPaymentUrls.updatePisCancellationAuthorisation(), HttpMethod.PUT, new HttpEntity<>(request),
                                                                                      UpdatePisCommonPaymentPsuDataResponse.class, request.getAuthorizationId()).getBody();
            return CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote update authorisation cancellation failed for authorisation id {}", authorisationId);
        }

        return CmsResponse.<UpdatePisCommonPaymentPsuDataResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<CmsResponse.VoidResponse> updateCommonPayment(PisCommonPaymentRequest request, String paymentId) {
        consentRestTemplate.exchange(remotePisCommonPaymentUrls.updatePisCommonPayment(), HttpMethod.PUT, new HttpEntity<>(request), Void.class, paymentId);
        return CmsResponse.<CmsResponse.VoidResponse>builder()
                   .payload(CmsResponse.voidResponse())
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateMultilevelSca(String paymentId, boolean multilevelScaRequired) {
        try {
            Boolean body = consentRestTemplate.exchange(remotePisCommonPaymentUrls.updateMultilevelScaRequired(), HttpMethod.PUT, null, Boolean.class, paymentId, multilevelScaRequired).getBody();
            return CmsResponse.<Boolean>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Payment ID: [{}]. No payment could be found by given payment ID.", paymentId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<GetPisAuthorisationResponse> getPisAuthorisationById(String authorisationId) {
        try {
            GetPisAuthorisationResponse body = consentRestTemplate.exchange(remotePisCommonPaymentUrls.getPisAuthorisationById(), HttpMethod.GET, null, GetPisAuthorisationResponse.class, authorisationId).getBody();
            return CmsResponse.<GetPisAuthorisationResponse>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Authorisation ID: [{}]. No initiation authorisation could be found by given authorisation ID", authorisationId);
        }

        return CmsResponse.<GetPisAuthorisationResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<GetPisAuthorisationResponse> getPisCancellationAuthorisationById(String cancellationId) {
        try {
            GetPisAuthorisationResponse body = consentRestTemplate.exchange(remotePisCommonPaymentUrls.getPisCancellationAuthorisationById(), HttpMethod.GET, null, GetPisAuthorisationResponse.class, cancellationId).getBody();
            return CmsResponse.<GetPisAuthorisationResponse>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.info("Authorisation ID: [{}]. No cancellation authorisation could be found by given cancellation ID", cancellationId);
        }

        return CmsResponse.<GetPisAuthorisationResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<List<String>> getAuthorisationsByPaymentId(String paymentId, PaymentAuthorisationType authorisationType) {
        String url = getAuthorisationSubResourcesUrl(authorisationType);

        try {
            List<String> body = consentRestTemplate.exchange(url, HttpMethod.GET, null,
                                                             new ParameterizedTypeReference<List<String>>() {
                                                             }, paymentId).getBody();
            return CmsResponse.<List<String>>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("No authorisation found by paymentId {}", paymentId);
        }

        return CmsResponse.<List<String>>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<ScaStatus> getAuthorisationScaStatus(String paymentId, String authorisationId, PaymentAuthorisationType authorisationType) {
        String url = getAuthorisationScaStatusUrl(authorisationType);
        try {
            ScaStatus body = consentRestTemplate.getForEntity(url, ScaStatus.class, paymentId, authorisationId).getBody();
            return CmsResponse.<ScaStatus>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't get authorisation SCA Status by paymentId {} and authorisationId {}", paymentId, authorisationId);
        }

        return CmsResponse.<ScaStatus>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        Boolean body = consentRestTemplate.getForEntity(remotePisCommonPaymentUrls.isAuthenticationMethodDecoupled(), Boolean.class, authorisationId, authenticationMethodId)
                           .getBody();
        return CmsResponse.<Boolean>builder()
                   .payload(body)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        try {
            ResponseEntity<Void> responseEntity = consentRestTemplate.exchange(remotePisCommonPaymentUrls.saveAuthenticationMethods(), HttpMethod.POST, new HttpEntity<>(methods), Void.class, authorisationId);

            if (responseEntity.getStatusCode() == HttpStatus.NO_CONTENT) {
                return CmsResponse.<Boolean>builder()
                           .payload(true)
                           .build();
            }
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't save authentication methods {} by authorisationId {}", methods, authorisationId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    private String getAuthorisationSubResourcesUrl(PaymentAuthorisationType authorisationType) {
        switch (authorisationType) {
            case CREATED:
                return remotePisCommonPaymentUrls.getAuthorisationSubResources();
            case CANCELLED:
                return remotePisCommonPaymentUrls.getCancellationAuthorisationSubResources();
            default:
                log.error("Unknown payment authorisation type {}", authorisationType);
                throw new IllegalArgumentException("Unknown payment authorisation type " + authorisationType);
        }
    }

    @Override
    public CmsResponse<List<PsuIdData>> getPsuDataListByPaymentId(String paymentId) {
        try {
            PsuIdData[] body = consentRestTemplate.getForEntity(remotePisCommonPaymentUrls.getPsuDataByPaymentId(), PsuIdData[].class, paymentId).getBody();
            if (body != null) {
                return CmsResponse.<List<PsuIdData>>builder()
                           .payload(Arrays.asList(body))
                           .build();
            }
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote get PSU data list by paymentId {} failed", paymentId);
        }

        return CmsResponse.<List<PsuIdData>>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    @Override
    public CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        try {
            Boolean body = consentRestTemplate.exchange(remotePisCommonPaymentUrls.updateScaApproach(), HttpMethod.PUT,
                                                        null, Boolean.class, authorisationId, scaApproach)
                               .getBody();
            return CmsResponse.<Boolean>builder()
                       .payload(body)
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Remote update payment authorisation SCA approach for authorisationID {} failed", authorisationId);
        }

        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Override
    public CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId, PaymentAuthorisationType authorisationType) {
        String url = getAuthorisationScaApproachUrl(authorisationType);

        try {
            ResponseEntity<AuthorisationScaApproachResponse> request = consentRestTemplate.getForEntity(
                url, AuthorisationScaApproachResponse.class, authorisationId);
            return CmsResponse.<AuthorisationScaApproachResponse>builder()
                       .payload(request.getBody())
                       .build();
        } catch (CmsRestException cmsRestException) {
            log.warn("Couldn't get authorisation SCA Approach by authorisationId {}", authorisationId);
        }

        return CmsResponse.<AuthorisationScaApproachResponse>builder()
                   .error(TECHNICAL_ERROR)
                   .build();
    }

    private String getAuthorisationScaApproachUrl(PaymentAuthorisationType authorisationType) {
        switch (authorisationType) {
            case CREATED:
                return remotePisCommonPaymentUrls.getAuthorisationScaApproach();
            case CANCELLED:
                return remotePisCommonPaymentUrls.getCancellationAuthorisationScaApproach();
            default:
                log.error("Unknown payment authorisation type {}", authorisationType);
                throw new IllegalArgumentException("Unknown payment authorisation type " + authorisationType);
        }
    }

    private String getAuthorisationScaStatusUrl(PaymentAuthorisationType authorisationType) {
        switch (authorisationType) {
            case CREATED:
                return remotePisCommonPaymentUrls.getAuthorisationScaStatus();
            case CANCELLED:
                return remotePisCommonPaymentUrls.getCancellationAuthorisationScaStatus();
            default:
                log.error("Unknown payment authorisation type {}", authorisationType);
                throw new IllegalArgumentException("Unknown payment authorisation type " + authorisationType);
        }
    }
}
