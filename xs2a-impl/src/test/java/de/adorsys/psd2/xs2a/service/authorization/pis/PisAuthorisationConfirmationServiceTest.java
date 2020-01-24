/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.authorization.pis;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.authorisation.UpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.consent.api.service.PisAuthorisationServiceEncrypted;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataResponse;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.Xs2aUpdatePaymentAfterSpiService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.spi.domain.SpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiConfirmationCode;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiConfirmationCodeCheckingResponse;
import de.adorsys.psd2.xs2a.spi.domain.payment.response.SpiPaymentConfirmationCodeValidationResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static de.adorsys.psd2.consent.api.CmsError.TECHNICAL_ERROR;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PisAuthorisationConfirmationServiceTest {
    private static final String PAYMENT_ID = "c713a32c-15ff-4f90-afa0-34a500359844";
    private static final String AUTHORISATION_ID = "ad746cb3-a01b-4196-a6b9-40b0e4cd2350";
    private static final String CONFIRMATION_CODE = "123456";
    private static final boolean IS_CANCELLATION = false;
    private final static JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private PisAuthorisationConfirmationService pisAuthorisationConfirmationService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;
    @Mock
    private Xs2aPisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private Xs2aToSpiPaymentMapper xs2aToSpiPaymentMapper;
    @Mock
    private PisAuthorisationServiceEncrypted pisAuthorisationServiceEncrypted;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private PisCheckAuthorisationConfirmationService pisCheckAuthorisationConfirmationService;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private SpiPayment payment;
    @Mock
    private SpiAspspConsentDataProvider aspspConsentDataProvider;
    @Mock
    private Xs2aUpdatePaymentAfterSpiService xs2aUpdatePaymentAfterSpiService;

    @Test
    void processAuthorisationConfirmation_success_checkOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.FINALISED, PAYMENT_ID, AUTHORISATION_ID, psuIdData);
        GetPisAuthorisationResponse authorisationResponse = buildGetPisAuthorisationResponse();

        SpiConfirmationCode spiConfirmationCode = new SpiConfirmationCode(request.getConfirmationCode());
        SpiContextData contextData = getSpiContextData();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(pisAuthorisationServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                                                                                                        .payload(authorisationResponse)
                                                                                                        .build());
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(authorisationResponse, request.getPaymentService(), request.getPaymentProduct()))
            .thenReturn(payment);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCode(contextData, spiConfirmationCode, payment, aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiConfirmationCodeCheckingResponse>builder()
                            .payload(new SpiConfirmationCodeCheckingResponse(ScaStatus.FINALISED))
                            .build());

        when(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(expectedResult)).thenReturn(buildUpdatePisCommonPaymentPsuDataRequest(psuIdData));

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request, IS_CANCELLATION);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void processAuthorisationConfirmation_success_checkOnXs2a() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(ScaStatus.FINALISED, PAYMENT_ID, AUTHORISATION_ID, psuIdData);
        GetPisAuthorisationResponse authorisationResponse = buildGetPisAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(pisAuthorisationServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                                                                                                        .payload(authorisationResponse)
                                                                                                        .build());

        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest = buildUpdatePisCommonPaymentPsuDataRequest(psuIdData);

        when(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(expectedResult)).thenReturn(updatePisCommonPaymentPsuDataRequest);

        SpiContextData contextData = getSpiContextData();
        SpiPaymentConfirmationCodeValidationResponse response = new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.FAILED, TransactionStatus.RJCT);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder().payload(response).build();
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(authorisationResponse, request.getPaymentService(), request.getPaymentProduct())).thenReturn(payment);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(aspspConsentDataProvider);
        when(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(expectedResult)).thenReturn(updatePisCommonPaymentPsuDataRequest);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(contextData);
        when(pisCheckAuthorisationConfirmationService.notifyConfirmationCodeValidation(contextData, true, payment, false, aspspConsentDataProvider)).thenReturn(spiResponse);

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request, IS_CANCELLATION);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);

        verify(pisAuthorisationServiceEncrypted, times(1)).updatePisAuthorisation(AUTHORISATION_ID, updatePisCommonPaymentPsuDataRequest);
        verify(xs2aUpdatePaymentAfterSpiService, times(1)).updatePaymentStatus(PAYMENT_ID, response.getTransactionStatus());
    }

    @Test
    void processAuthorisationConfirmation_failed_NoAuthorisation() {
        // given
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_404)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_404_NO_AUTHORISATION))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());


        when(pisAuthorisationServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                                                                                                        .error(TECHNICAL_ERROR)
                                                                                                        .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request, IS_CANCELLATION);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void processAuthorisationConfirmation_failed_WrongScaStatus() {
        // given
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR_SCA_STATUS, ScaStatus.FINALISED.name(), ScaStatus.UNCONFIRMED.name(), ScaStatus.PSUAUTHENTICATED))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());


        GetPisAuthorisationResponse authorisationResponse = buildGetPisAuthorisationResponse();
        authorisationResponse.setScaStatus(ScaStatus.PSUAUTHENTICATED);


        when(pisAuthorisationServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                                                                                                        .payload(authorisationResponse)
                                                                                                        .build());

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request, IS_CANCELLATION);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void processAuthorisationConfirmation_failed_wrongCode() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        request.setConfirmationCode("wrong_code");

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());

        GetPisAuthorisationResponse authorisationResponse = buildGetPisAuthorisationResponse();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(true);
        when(pisAuthorisationServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                                                                                                        .payload(authorisationResponse)
                                                                                                        .build());
        UpdatePisCommonPaymentPsuDataRequest updatePisCommonPaymentPsuDataRequest = buildUpdatePisCommonPaymentPsuDataRequest(psuIdData);
        updatePisCommonPaymentPsuDataRequest.setScaStatus(ScaStatus.FAILED);

        when(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(expectedResult)).thenReturn(updatePisCommonPaymentPsuDataRequest);

        SpiContextData contextData = getSpiContextData();
        SpiPaymentConfirmationCodeValidationResponse response = new SpiPaymentConfirmationCodeValidationResponse(ScaStatus.FAILED, TransactionStatus.RJCT);
        SpiResponse<SpiPaymentConfirmationCodeValidationResponse> spiResponse = SpiResponse.<SpiPaymentConfirmationCodeValidationResponse>builder().payload(response).build();
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(authorisationResponse, request.getPaymentService(), request.getPaymentProduct())).thenReturn(payment);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID)).thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData)).thenReturn(contextData);
        when(pisCheckAuthorisationConfirmationService.notifyConfirmationCodeValidation(contextData, false, payment, false, aspspConsentDataProvider)).thenReturn(spiResponse);

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request, IS_CANCELLATION);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
        verify(pisAuthorisationServiceEncrypted, times(1)).updatePisAuthorisation(AUTHORISATION_ID, updatePisCommonPaymentPsuDataRequest);
        verify(xs2aUpdatePaymentAfterSpiService, times(1)).updatePaymentStatus(PAYMENT_ID, response.getTransactionStatus());
    }

    @Test
    void processAuthorisationConfirmation_failed_errorOnSpi() {
        // given
        PsuIdData psuIdData = buildPsuIdData();
        Xs2aUpdatePisCommonPaymentPsuDataRequest request = buildUpdatePisCommonPaymentPsuDataRequest();
        SpiResponse<SpiConfirmationCodeCheckingResponse> spiResponse = SpiResponse.<SpiConfirmationCodeCheckingResponse>builder()
                                                                           .error(new TppMessage(PSU_CREDENTIALS_INVALID))
                                                                           .build();

        ErrorHolder errorHolder = ErrorHolder.builder(ErrorType.PIS_400)
                                      .tppMessages(TppMessageInformation.of(MessageErrorCode.SCA_INVALID))
                                      .build();
        Xs2aUpdatePisCommonPaymentPsuDataResponse expectedResult = new Xs2aUpdatePisCommonPaymentPsuDataResponse(errorHolder, request.getPaymentId(), request.getAuthorisationId(), request.getPsuData());
        GetPisAuthorisationResponse authorisationResponse = buildGetPisAuthorisationResponse();

        SpiConfirmationCode spiConfirmationCode = new SpiConfirmationCode(request.getConfirmationCode());
        SpiContextData contextData = getSpiContextData();

        when(aspspProfileServiceWrapper.isAuthorisationConfirmationCheckByXs2a()).thenReturn(false);
        when(pisAuthorisationServiceEncrypted.getPisAuthorisationById(AUTHORISATION_ID)).thenReturn(CmsResponse.<GetPisAuthorisationResponse>builder()
                                                                                                        .payload(authorisationResponse)
                                                                                                        .build());
        when(spiContextDataProvider.provideWithPsuIdData(psuIdData))
            .thenReturn(contextData);
        when(xs2aToSpiPaymentMapper.mapToSpiPayment(authorisationResponse, request.getPaymentService(), request.getPaymentProduct()))
            .thenReturn(payment);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(PAYMENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(pisCheckAuthorisationConfirmationService.checkConfirmationCode(contextData, spiConfirmationCode, payment, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIS)).thenReturn(errorHolder);
        when(pisCommonPaymentMapper.mapToCmsUpdateCommonPaymentPsuDataReq(expectedResult)).thenReturn(buildUpdatePisCommonPaymentPsuDataRequest(psuIdData));

        // when
        Xs2aUpdatePisCommonPaymentPsuDataResponse actualResult = pisAuthorisationConfirmationService.processAuthorisationConfirmation(request, IS_CANCELLATION);

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private SpiContextData getSpiContextData() {
        return new SpiContextData(null, null, null, null, null);
    }

    private GetPisAuthorisationResponse buildGetPisAuthorisationResponse() {
        GetPisAuthorisationResponse response = new GetPisAuthorisationResponse();
        response.setPaymentId(PAYMENT_ID);
        response.setScaStatus(ScaStatus.UNCONFIRMED);
        response.setScaAuthenticationData(CONFIRMATION_CODE);
        response.setPaymentType(PaymentType.SINGLE);
        return response;
    }

    private UpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest(PsuIdData psuIdData) {
        UpdatePisCommonPaymentPsuDataRequest request = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/update-pis-common-payment-psu-data-request.json",
                                                                                    UpdatePisCommonPaymentPsuDataRequest.class);
        request.setAuthorizationId(AUTHORISATION_ID);
        request.setPaymentId(PAYMENT_ID);
        request.setScaStatus(ScaStatus.FINALISED);
        request.setPsuData(psuIdData);
        return request;
    }

    private Xs2aUpdatePisCommonPaymentPsuDataRequest buildUpdatePisCommonPaymentPsuDataRequest() {
        Xs2aUpdatePisCommonPaymentPsuDataRequest request =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-update-pis-common-payment-psu-data-request.json",
                                         Xs2aUpdatePisCommonPaymentPsuDataRequest.class);

        request.setConfirmationCode(CONFIRMATION_CODE);
        request.setPaymentId(PAYMENT_ID);
        request.setAuthorisationId(AUTHORISATION_ID);
        request.setScaStatus(ScaStatus.UNCONFIRMED);
        request.setPsuData(buildPsuIdData());

        return request;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    }

}
