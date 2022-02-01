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

package de.adorsys.psd2.xs2a.service.payment.create;

import de.adorsys.psd2.consent.api.pis.CreatePisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.processor.model.PisAuthorisationProcessorRequest;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aToCmsPisCommonPaymentRequestMapper;
import de.adorsys.psd2.xs2a.service.payment.create.spi.PaymentInitiationService;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.domain.TppMessageInformation.of;
import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIS_400;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PAYMENT_FAILED;

@RequiredArgsConstructor
public abstract class AbstractCreatePaymentService<P extends CommonPayment, S extends PaymentInitiationService<P>> implements CreatePaymentService {
    protected final Xs2aPisCommonPaymentService pisCommonPaymentService;
    private final PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver;
    private final AuthorisationMethodDecider authorisationMethodDecider;
    private final Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper;
    private final Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper;
    private final S paymentInitiationService;
    private final RequestProviderService requestProviderService;
    private final LoggingContextService loggingContextService;
    private final AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    private final ScaApproachResolver scaApproachResolver;

    /**
     * Initiates payment
     *
     * @param payment                     payment information
     * @param paymentInitiationParameters payment initiation parameters
     * @param tppInfo                     information about particular TPP
     * @return Response containing information about created common payment or corresponding error
     */
    @Override
    public ResponseObject<PaymentInitiationResponse> createPayment(byte[] payment, PaymentInitiationParameters paymentInitiationParameters, TppInfo tppInfo) {
        PsuIdData psuIdData = paymentInitiationParameters.getPsuData();

        P paymentRequest = getPaymentRequest(payment, paymentInitiationParameters);
        OffsetDateTime creationTimestamp = OffsetDateTime.now();
        paymentRequest.setCreationTimestamp(creationTimestamp);
        paymentRequest.setInstanceId(paymentInitiationParameters.getInstanceId());
        PaymentInitiationResponse response = paymentInitiationService.initiatePayment(paymentRequest, paymentInitiationParameters.getPaymentProduct(), psuIdData);

        if (response.hasError()) {
            return buildErrorResponse(response.getErrorHolder());
        }

        String internalRequestId = requestProviderService.getInternalRequestIdString();
        String contentType = requestProviderService.getContentTypeHeader();
        PisPaymentInfo pisPaymentInfo = xs2aToCmsPisCommonPaymentRequestMapper.mapToPisPaymentInfo(new PisPaymentInfoCreationObject(paymentInitiationParameters, tppInfo, response, paymentRequest.getPaymentData(), internalRequestId, creationTimestamp, contentType));
        response.setInternalRequestId(internalRequestId);
        pisPaymentInfo.setInternalPaymentStatus(InternalPaymentStatus.INITIATED);
        CreatePisCommonPaymentResponse cmsResponse = pisCommonPaymentService.createCommonPayment(pisPaymentInfo);
        response.setTppNotificationContentPreferred(cmsResponse.getTppNotificationContentPreferred());

        Xs2aPisCommonPayment pisCommonPayment = xs2aPisCommonPaymentMapper.mapToXs2aPisCommonPayment(cmsResponse, psuIdData);

        String externalPaymentId = pisCommonPayment.getPaymentId();

        if (StringUtils.isBlank(externalPaymentId)) {
            return ResponseObject.<PaymentInitiationResponse>builder()
                       .fail(PIS_400, of(PAYMENT_FAILED))
                       .build();
        }

        InitialSpiAspspConsentDataProvider aspspConsentDataProvider = response.getAspspConsentDataProvider();
        aspspConsentDataProvider.saveWith(externalPaymentId);

        response.setPaymentId(externalPaymentId);

        boolean implicitMethod = authorisationMethodDecider.isImplicitMethod(paymentInitiationParameters.isTppExplicitAuthorisationPreferred(), response.isMultilevelScaRequired());
        if (implicitMethod) {
            // TODO https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/-/issues/1629
            ScaStatus scaStatus = ScaStatus.STARTED;
            String authorisationId = UUID.randomUUID().toString();
            ScaApproach scaApproach = scaApproachResolver.resolveScaApproach();
            StartAuthorisationsParameters startAuthorisationsParameters = StartAuthorisationsParameters.builder()
                                                                              .psuData(psuIdData)
                                                                              .businessObjectId(externalPaymentId)
                                                                              .scaStatus(scaStatus)
                                                                              .authorisationId(authorisationId)
                                                                              .build();

            Authorisation authorisation = new Authorisation(authorisationId, psuIdData, externalPaymentId, AuthorisationType.PIS_CREATION, scaStatus);
            PisAuthorisationProcessorRequest processorRequest = new PisAuthorisationProcessorRequest(scaApproach, scaStatus, startAuthorisationsParameters, authorisation);
            CreatePaymentAuthorisationProcessorResponse processorResponse =
                (CreatePaymentAuthorisationProcessorResponse) authorisationChainResponsibilityService.apply(processorRequest);

            loggingContextService.storeScaStatus(processorResponse.getScaStatus());

            Xs2aCreateAuthorisationRequest createAuthorisationRequest = Xs2aCreateAuthorisationRequest.builder()
                                                                            .psuData(psuIdData)
                                                                            .paymentId(externalPaymentId)
                                                                            .authorisationId(authorisationId)
                                                                            .scaStatus(processorResponse.getScaStatus())
                                                                            .scaApproach(processorResponse.getScaApproach())
                                                                            .build();

            PisScaAuthorisationService pisScaAuthorisationService = pisScaAuthorisationServiceResolver.getService();

            Optional<Xs2aCreatePisAuthorisationResponse> consentAuthorisation =
                pisScaAuthorisationService.createCommonPaymentAuthorisation(createAuthorisationRequest, paymentRequest.getPaymentType());

            if (consentAuthorisation.isEmpty()) {
                return ResponseObject.<PaymentInitiationResponse>builder()
                           .fail(PIS_400, of(PAYMENT_FAILED))
                           .build();
            }

            Xs2aCreatePisAuthorisationResponse authorisationResponse = consentAuthorisation.get();
            response.setAuthorizationId(authorisationResponse.getAuthorisationId());
            response.setScaStatus(authorisationResponse.getScaStatus());
            setPsuMessageAndTppMessages(response, processorResponse.getPsuMessage(), processorResponse.getTppMessages());
        }

        return ResponseObject.<PaymentInitiationResponse>builder()
                   .body(response)
                   .build();
    }

    private void setPsuMessageAndTppMessages(PaymentInitiationResponse response,
                                             String psuMessage, Set<TppMessageInformation> tppMessageInformationSet) {
        if (psuMessage != null) {
            response.setPsuMessage(psuMessage);
        }
        if (tppMessageInformationSet != null) {
            response.getTppMessageInformation().addAll(tppMessageInformationSet);
        }
    }

    protected abstract P getPaymentRequest(byte[] payment, PaymentInitiationParameters paymentInitiationParameters);
}
