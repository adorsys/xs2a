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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.ConsentStatusResponse;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aConfirmationOfFundsResponse;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountReferenceMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.piis.CreatePiisConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiatePiisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.piis.SpiPiisConsent;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.service.PiisConsentSpi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PiisConsentServiceTest {
    private static String CORRECT_PSU_ID = "marion.mueller";
    private static String CONSENT_ID = "consent ID";
    private static PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private static SpiPsuData SPI_PSU_DATA = SpiPsuData.builder().psuId(CORRECT_PSU_ID).build();
    private static TppInfo TPP_INFO = buildTppInfo();
    private static SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, TPP_INFO, UUID.randomUUID(), UUID.randomUUID(), "", "");
    private PiisConsent piisConsent = buildPiisConsent();
    private SpiPiisConsent spiPiisConsent = buildSpiPiisConsent();

    private static final MessageError RESOURCE_ERROR = new MessageError(ErrorType.PIIS_400, TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_400));
    private static final MessageError CONSENT_UNKNOWN_ERROR = new MessageError(ErrorType.PIIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403));

    @InjectMocks
    private PiisConsentService piisConsentService;

    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private TppService tppService;
    @Mock
    private Xs2aPiisConsentService xs2aPiisConsentService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private Xs2aToSpiPiisConsentMapper xs2aToSpiPiisConsentMapper;
    @Mock
    private SpiAspspConsentDataProviderFactory aspspConsentDataProviderFactory;
    @Mock
    private PiisConsentSpi piisConsentSpi;
    @Mock
    private InitialSpiAspspConsentDataProvider aspspConsentDataProvider;
    @Mock
    private SpiToXs2aAccountReferenceMapper spiToXs2aAccountReferenceMapper;
    @Mock
    private AccountReferenceInConsentUpdater accountReferenceUpdater;
    @Mock
    private SpiErrorMapper spiErrorMapper;
    @Mock
    private CreatePiisConsentValidator createPiisConsentValidator;

    @Test
    void createPiisConsentWithResponse_success() {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);
        Xs2aCreatePiisConsentResponse xs2aCreatePiisConsentResponse = new Xs2aCreatePiisConsentResponse(CONSENT_ID, piisConsent);
        when(xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, TPP_INFO))
            .thenReturn(Optional.of(xs2aCreatePiisConsentResponse));
        SpiAccountReference spiAccountReference = new SpiAccountReference(null, "DE15500105172295759744", null, null, null, null, Currency.getInstance("EUR"), null);
        SpiInitiatePiisConsentResponse spiInitiatePiisConsentResponse = new SpiInitiatePiisConsentResponse(spiAccountReference, false, "");
        when(piisConsentSpi.initiatePiisConsent(SPI_CONTEXT_DATA, spiPiisConsent, aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiInitiatePiisConsentResponse>builder().payload(spiInitiatePiisConsentResponse).build());
        when(spiContextDataProvider.provide(PSU_ID_DATA, TPP_INFO))
            .thenReturn(SPI_CONTEXT_DATA);
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(aspspConsentDataProvider);
        AccountReference accountReference = new AccountReference();
        accountReference.setIban(spiAccountReference.getIban());
        accountReference.setCurrency(spiAccountReference.getCurrency());
        when(spiToXs2aAccountReferenceMapper.mapToXs2aAccountReference(spiAccountReference))
            .thenReturn(accountReference);
        when(createPiisConsentValidator.validate(new CreatePiisConsentRequestObject(request, PSU_ID_DATA))).thenReturn(ValidationResult.valid());

        //When
        ResponseObject<Xs2aConfirmationOfFundsResponse> xs2aConfirmationOfFundsResponseResponseObject = piisConsentService.createPiisConsentWithResponse(request, PSU_ID_DATA, false);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordTppRequest(EventType.CREATE_PIIS_CONSENT_REQUEST_RECEIVED, request);
        AccountAccess accountAccess = new AccountAccess(Collections.singletonList(accountReference), Collections.emptyList(), Collections.emptyList(), null);
        verify(accountReferenceUpdater, atLeastOnce()).rewriteAccountAccess(CONSENT_ID, accountAccess, ConsentType.PIIS_TPP);
        assertFalse(xs2aConfirmationOfFundsResponseResponseObject.hasError());
        Xs2aConfirmationOfFundsResponse xs2aConfirmationOfFundsResponse = xs2aConfirmationOfFundsResponseResponseObject.getBody();
        assertEquals(CONSENT_ID, xs2aConfirmationOfFundsResponse.getConsentId());
        assertEquals(piisConsent.getConsentStatus().getValue(), xs2aConfirmationOfFundsResponse.getConsentStatus());
    }

    @Test
    void createPiisConsentWithResponse_createConsentFailed() {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);
        when(xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, TPP_INFO))
            .thenReturn(Optional.empty());
        when(createPiisConsentValidator.validate(new CreatePiisConsentRequestObject(request, PSU_ID_DATA))).thenReturn(ValidationResult.valid());
        //When
        ResponseObject<Xs2aConfirmationOfFundsResponse> xs2aConfirmationOfFundsResponseResponseObject = piisConsentService.createPiisConsentWithResponse(request, PSU_ID_DATA, false);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordTppRequest(EventType.CREATE_PIIS_CONSENT_REQUEST_RECEIVED, request);
        assertTrue(xs2aConfirmationOfFundsResponseResponseObject.hasError());
        assertEquals(RESOURCE_ERROR, xs2aConfirmationOfFundsResponseResponseObject.getError());
    }

    @Test
    void createPiisConsentWithResponse_initiatePiisConsentInSpiFailed() {
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);
        Xs2aCreatePiisConsentResponse xs2aCreatePiisConsentResponse = new Xs2aCreatePiisConsentResponse(CONSENT_ID, piisConsent);
        when(xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, TPP_INFO))
            .thenReturn(Optional.of(xs2aCreatePiisConsentResponse));
        SpiResponse<SpiInitiatePiisConsentResponse> spiResponse = SpiResponse.<SpiInitiatePiisConsentResponse>builder().error(new TppMessage(PSU_CREDENTIALS_INVALID)).build();
        when(piisConsentSpi.initiatePiisConsent(SPI_CONTEXT_DATA, spiPiisConsent, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiContextDataProvider.provide(PSU_ID_DATA, TPP_INFO))
            .thenReturn(SPI_CONTEXT_DATA);
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getInitialAspspConsentDataProvider())
            .thenReturn(aspspConsentDataProvider);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(ErrorType.PIIS_400).tppMessages(RESOURCE_ERROR.getTppMessage()).build());
        when(createPiisConsentValidator.validate(new CreatePiisConsentRequestObject(request, PSU_ID_DATA))).thenReturn(ValidationResult.valid());
        //When
        ResponseObject<Xs2aConfirmationOfFundsResponse> xs2aConfirmationOfFundsResponseResponseObject = piisConsentService.createPiisConsentWithResponse(request, PSU_ID_DATA, false);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordTppRequest(EventType.CREATE_PIIS_CONSENT_REQUEST_RECEIVED, request);
        assertTrue(xs2aConfirmationOfFundsResponseResponseObject.hasError());
        assertEquals(RESOURCE_ERROR, xs2aConfirmationOfFundsResponseResponseObject.getError());
    }

    @Test
    void getPiisConsentById_success() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        SpiConsentStatusResponse spiConsentStatusResponse = new SpiConsentStatusResponse(ConsentStatus.VALID, "");
        when(piisConsentSpi.getConsentStatus(SPI_CONTEXT_DATA, spiPiisConsent, aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiConsentStatusResponse>builder().payload(spiConsentStatusResponse).build());
        //When
        ResponseObject<PiisConsent> piisConsentResponseObject = piisConsentService.getPiisConsentById(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordAisTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).updateConsentStatus(CONSENT_ID, spiConsentStatusResponse.getConsentStatus());
        assertFalse(piisConsentResponseObject.hasError());
        PiisConsent piisConsent = piisConsentResponseObject.getBody();
        assertEquals(spiConsentStatusResponse.getConsentStatus(), piisConsent.getConsentStatus());
    }

    @Test
    void getPiisConsentById_getConsentFailed() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());
        //When
        ResponseObject<PiisConsent> piisConsentResponseObject = piisConsentService.getPiisConsentById(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordAisTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_REQUEST_RECEIVED);
        assertTrue(piisConsentResponseObject.hasError());
        assertEquals(CONSENT_UNKNOWN_ERROR, piisConsentResponseObject.getError());
    }

    @Test
    void getPiisConsentById_getConsentStatusFromSpiFailed() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        SpiResponse<SpiConsentStatusResponse> spiResponse = SpiResponse.<SpiConsentStatusResponse>builder().error(new TppMessage(PSU_CREDENTIALS_INVALID)).build();
        when(piisConsentSpi.getConsentStatus(SPI_CONTEXT_DATA, spiPiisConsent, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(ErrorType.PIIS_403).tppMessages(CONSENT_UNKNOWN_ERROR.getTppMessage()).build());
        //When
        ResponseObject<PiisConsent> piisConsentResponseObject = piisConsentService.getPiisConsentById(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordAisTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_REQUEST_RECEIVED);
        assertTrue(piisConsentResponseObject.hasError());
        assertEquals(CONSENT_UNKNOWN_ERROR, piisConsentResponseObject.getError());
    }

    @Test
    void getPiisConsentStatusById_success() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        SpiConsentStatusResponse spiConsentStatusResponse = new SpiConsentStatusResponse(ConsentStatus.VALID, "");
        when(piisConsentSpi.getConsentStatus(SPI_CONTEXT_DATA, spiPiisConsent, aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiConsentStatusResponse>builder().payload(spiConsentStatusResponse).build());
        //When
        ResponseObject<ConsentStatusResponse> piisConsentResponseObject = piisConsentService.getPiisConsentStatusById(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordAisTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_STATUS_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).updateConsentStatus(CONSENT_ID, spiConsentStatusResponse.getConsentStatus());
        assertFalse(piisConsentResponseObject.hasError());
    }

    @Test
    void getPiisConsentStatusById_getConsentFailed() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());
        //When
        ResponseObject<ConsentStatusResponse> piisConsentResponseObject = piisConsentService.getPiisConsentStatusById(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordAisTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_STATUS_REQUEST_RECEIVED);
        assertTrue(piisConsentResponseObject.hasError());
        assertEquals(CONSENT_UNKNOWN_ERROR, piisConsentResponseObject.getError());
    }

    @Test
    void getPiisConsentStatusById_getConsentStatusFromSpiFailed() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provide())
            .thenReturn(SPI_CONTEXT_DATA);
        SpiResponse<SpiConsentStatusResponse> spiResponse = SpiResponse.<SpiConsentStatusResponse>builder().error(new TppMessage(PSU_CREDENTIALS_INVALID)).build();
        when(piisConsentSpi.getConsentStatus(SPI_CONTEXT_DATA, spiPiisConsent, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(ErrorType.PIIS_403).tppMessages(CONSENT_UNKNOWN_ERROR.getTppMessage()).build());
        //When
        ResponseObject<ConsentStatusResponse> piisConsentResponseObject = piisConsentService.getPiisConsentStatusById(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordAisTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_STATUS_REQUEST_RECEIVED);
        assertTrue(piisConsentResponseObject.hasError());
        assertEquals(CONSENT_UNKNOWN_ERROR, piisConsentResponseObject.getError());
    }

    private PiisConsent buildPiisConsent() {
        PiisConsent piisConsent = new PiisConsent();
        piisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        return piisConsent;
    }

    private SpiPiisConsent buildSpiPiisConsent() {
        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        spiPiisConsent.setConsentStatus(ConsentStatus.RECEIVED);
        return spiPiisConsent;
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("Test TppId");
        return tppInfo;
    }
}
