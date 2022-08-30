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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.event.core.model.EventType;
import de.adorsys.psd2.logger.context.LoggingContextService;
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
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.Xs2aResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.domain.authorisation.AuthorisationResponse;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.AbstractPiisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.piis.PiisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountReferenceMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aLinksMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPiisConsentMapper;
import de.adorsys.psd2.xs2a.service.spi.InitialSpiAspspConsentDataProvider;
import de.adorsys.psd2.xs2a.service.spi.SpiAspspConsentDataProviderFactory;
import de.adorsys.psd2.xs2a.service.validator.piis.CreatePiisConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.piis.dto.CreatePiisConsentRequestObject;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaStatusResponse;
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

import java.util.*;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_UNKNOWN_403_INCORRECT_CERTIFICATE;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.PSU_CREDENTIALS_INVALID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PiisConsentServiceTest {
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final String CONSENT_ID = "consent ID";
    private static final String AUTHORISATION_ID = "1b61a80e-e1f1-4752-bf71-a7af0955a47b";
    private static final String PASSWORD = "password";
    private static final String PSU_MESSAGE = "psu message";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = SpiPsuData.builder().psuId(CORRECT_PSU_ID).build();
    private static final TppInfo TPP_INFO = buildTppInfo();
    private static final SpiContextData SPI_CONTEXT_DATA = new SpiContextData(SPI_PSU_DATA, TPP_INFO, UUID.randomUUID(), UUID.randomUUID(), "", "", null, null, null);
    private final PiisConsent piisConsent = buildPiisConsent(ConsentStatus.RECEIVED);
    private final SpiPiisConsent spiPiisConsent = buildSpiPiisConsent(ConsentStatus.RECEIVED);
    private static final ScaStatus SCA_STATUS = ScaStatus.RECEIVED;
    private static final ScaApproach SCA_APPROACH = ScaApproach.EMBEDDED;
    private static final MessageError RESOURCE_ERROR = new MessageError(ErrorType.PIIS_400, TppMessageInformation.of(MessageErrorCode.RESOURCE_UNKNOWN_400));
    private static final MessageError CONSENT_UNKNOWN_ERROR = new MessageError(ErrorType.PIIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403));
    private static final MessageError INCORRECT_CERTIFICATE_ERROR = new MessageError(ErrorType.PIIS_403, TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_403_INCORRECT_CERTIFICATE));
    private static final Set<TppMessageInformation> TEST_TPP_MESSAGES = Collections.singleton(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR));

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
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private PiisScaAuthorisationServiceResolver piisScaAuthorisationServiceResolver;
    @Mock
    private AbstractPiisAuthorizationService piisAuthorizationService;
    @Mock
    private ConfirmationOfFundsConsentValidationService confirmationOfFundsConsentValidationService;
    @Mock
    private PiisConsentAuthorisationService piisConsentAuthorisationService;
    @Mock
    private CreateConsentAuthorizationResponse createConsentAuthorizationResponse;
    @Mock
    private Xs2aAuthorisationService xs2aAuthorisationService;
    @Mock
    private SpiToXs2aLinksMapper spiToXs2aLinksMapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AuthorisationChainResponsibilityService authorisationChainResponsibilityService;
    @Mock
    private LoggingContextService loggingContextService;

    @Test
    void createPiisConsentWithResponse_success() {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);
        Xs2aCreatePiisConsentResponse xs2aCreatePiisConsentResponse = new Xs2aCreatePiisConsentResponse(CONSENT_ID, piisConsent);
        when(xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, TPP_INFO))
            .thenReturn(Xs2aResponse.<Xs2aCreatePiisConsentResponse>builder()
                            .payload(xs2aCreatePiisConsentResponse)
                            .build());
        SpiAccountReference spiAccountReference = SpiAccountReference.builder()
                                                      .iban("DE15500105172295759744")
                                                      .currency(Currency.getInstance("EUR"))
                                                      .build();
        SpiInitiatePiisConsentResponse spiInitiatePiisConsentResponse = new SpiInitiatePiisConsentResponse(spiAccountReference, true, PSU_MESSAGE);
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
        when(authorisationMethodDecider.isImplicitMethod(anyBoolean(), anyBoolean()))
            .thenReturn(true);
        when(piisScaAuthorisationServiceResolver.getService())
            .thenReturn(piisAuthorizationService);

        CreateConsentAuthorisationProcessorResponse response = new CreateConsentAuthorisationProcessorResponse(SCA_STATUS, SCA_APPROACH, PSU_MESSAGE, TEST_TPP_MESSAGES, CONSENT_ID, PSU_ID_DATA);
        when(authorisationChainResponsibilityService.apply(any())).thenReturn(response);
        CreateConsentAuthorizationResponse createConsentAuthorizationResponse = new CreateConsentAuthorizationResponse();
        String AUTHORISATION_ID = "4af7bca6-54f8-49ae-83b2-3436f8d99c6f";
        createConsentAuthorizationResponse.setAuthorisationId(AUTHORISATION_ID);

        when(piisAuthorizationService.createConsentAuthorization(any()))
            .thenReturn(Optional.of(createConsentAuthorizationResponse));

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
        assertEquals(AUTHORISATION_ID, xs2aConfirmationOfFundsResponse.getAuthorizationId());
        assertEquals(PSU_MESSAGE, xs2aConfirmationOfFundsResponse.getPsuMessage());
        assertTrue(xs2aConfirmationOfFundsResponse.isMultilevelScaRequired());
        verify(xs2aPiisConsentService, atLeastOnce()).updateMultilevelScaRequired(CONSENT_ID, true);
    }

    @Test
    void createPiisConsentWithResponse_createConsentFailed() {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);
        when(xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, TPP_INFO))
            .thenReturn(Xs2aResponse.<Xs2aCreatePiisConsentResponse>builder()
                            .build());
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
            .thenReturn(Xs2aResponse.<Xs2aCreatePiisConsentResponse>builder()
                            .payload(xs2aCreatePiisConsentResponse)
                            .build());
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
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_REQUEST_RECEIVED);
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
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_REQUEST_RECEIVED);
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
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_REQUEST_RECEIVED);
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
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_STATUS_REQUEST_RECEIVED);
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
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_STATUS_REQUEST_RECEIVED);
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
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.GET_PIIS_CONSENT_STATUS_REQUEST_RECEIVED);
        assertTrue(piisConsentResponseObject.hasError());
        assertEquals(CONSENT_UNKNOWN_ERROR, piisConsentResponseObject.getError());
    }

    @Test
    void deleteAccountConsentsById_getConsentFailed() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.empty());
        //When
        ResponseObject<Void> responseObject = piisConsentService.deleteAccountConsentsById(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.DELETE_PIIS_CONSENT_REQUEST_RECEIVED);
        assertTrue(responseObject.hasError());
        assertEquals(CONSENT_UNKNOWN_ERROR, responseObject.getError());
    }

    @Test
    void deleteAccountConsentsById_validationFailed() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentOnDelete(piisConsent))
            .thenReturn(ValidationResult.invalid(ErrorType.PIIS_403, TppMessageInformation.of(CONSENT_UNKNOWN_403_INCORRECT_CERTIFICATE)));
        //When
        ResponseObject<Void> responseObject = piisConsentService.deleteAccountConsentsById(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.DELETE_PIIS_CONSENT_REQUEST_RECEIVED);
        assertTrue(responseObject.hasError());
        assertEquals(INCORRECT_CERTIFICATE_ERROR, responseObject.getError());
    }

    @Test
    void deleteAccountConsentsById_revokePiisConsentSpiFailed() {
        //Given
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentOnDelete(piisConsent))
            .thenReturn(ValidationResult.valid());
        when(requestProviderService.getPsuIdData())
            .thenReturn(PSU_ID_DATA);
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        SpiResponse<SpiResponse.VoidResponse> spiResponse = SpiResponse.<SpiResponse.VoidResponse>builder().error(new TppMessage(PSU_CREDENTIALS_INVALID)).build();
        when(piisConsentSpi.revokePiisConsent(SPI_CONTEXT_DATA, spiPiisConsent, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(ErrorType.PIIS_403).tppMessages(CONSENT_UNKNOWN_ERROR.getTppMessage()).build());
        //When
        ResponseObject<Void> responseObject = piisConsentService.deleteAccountConsentsById(CONSENT_ID);
        //Then
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.DELETE_PIIS_CONSENT_REQUEST_RECEIVED);
        assertTrue(responseObject.hasError());
        assertEquals(CONSENT_UNKNOWN_ERROR, responseObject.getError());
    }

    @Test
    void deleteAccountConsentsById_RejectedStatus_Success() {
        //Given
        PiisConsent piisConsent = buildPiisConsent(ConsentStatus.RECEIVED);
        SpiPiisConsent spiPiisConsent = buildSpiPiisConsent(ConsentStatus.RECEIVED);
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentOnDelete(piisConsent))
            .thenReturn(ValidationResult.valid());
        when(requestProviderService.getPsuIdData())
            .thenReturn(PSU_ID_DATA);
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        SpiResponse<SpiResponse.VoidResponse> spiResponse = SpiResponse.<SpiResponse.VoidResponse>builder().payload(SpiResponse.voidResponse()).build();
        when(piisConsentSpi.revokePiisConsent(SPI_CONTEXT_DATA, spiPiisConsent, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        //When
        ResponseObject<Void> responseObject = piisConsentService.deleteAccountConsentsById(CONSENT_ID);
        //Then
        assertFalse(responseObject.hasError());
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.DELETE_PIIS_CONSENT_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).updateConsentStatus(CONSENT_ID, ConsentStatus.REJECTED);
    }

    @Test
    void deleteAccountConsentsById_TerminatedByTppStatus_Success() {
        //Given
        PiisConsent piisConsent = buildPiisConsent(ConsentStatus.VALID);
        SpiPiisConsent spiPiisConsent = buildSpiPiisConsent(ConsentStatus.VALID);
        when(xs2aPiisConsentService.getPiisConsentById(CONSENT_ID))
            .thenReturn(Optional.of(piisConsent));
        when(confirmationOfFundsConsentValidationService.validateConsentOnDelete(piisConsent))
            .thenReturn(ValidationResult.valid());
        when(requestProviderService.getPsuIdData())
            .thenReturn(PSU_ID_DATA);
        when(xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent))
            .thenReturn(spiPiisConsent);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA))
            .thenReturn(SPI_CONTEXT_DATA);
        SpiResponse<SpiResponse.VoidResponse> spiResponse = SpiResponse.<SpiResponse.VoidResponse>builder().payload(SpiResponse.voidResponse()).build();
        when(piisConsentSpi.revokePiisConsent(SPI_CONTEXT_DATA, spiPiisConsent, aspspConsentDataProvider))
            .thenReturn(spiResponse);
        //When
        ResponseObject<Void> responseObject = piisConsentService.deleteAccountConsentsById(CONSENT_ID);
        //Then
        assertFalse(responseObject.hasError());
        verify(xs2aEventService, atLeastOnce()).recordConsentTppRequest(CONSENT_ID, EventType.DELETE_PIIS_CONSENT_REQUEST_RECEIVED);
        verify(xs2aPiisConsentService, atLeastOnce()).updateConsentStatus(CONSENT_ID, ConsentStatus.TERMINATED_BY_TPP);
    }

    @Test
    void createPiisAuthorisation() {
        //Given
        ResponseObject<AuthorisationResponse> authorisationResponseResponseObject = ResponseObject.<AuthorisationResponse>builder().body(createConsentAuthorizationResponse).build();
        when(piisConsentAuthorisationService.createPiisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD))
            .thenReturn(authorisationResponseResponseObject);
        //When
        ResponseObject<AuthorisationResponse> actualResponse = piisConsentService.createPiisAuthorisation(PSU_ID_DATA, CONSENT_ID, PASSWORD);
        //Then
        assertEquals(authorisationResponseResponseObject.getBody(), actualResponse.getBody());
    }

    @Test
    void getConsentInitiationAuthorisations_Success() {
        //Given
        List<String> authorisations = Collections.singletonList("1b61a80e-e1f1-4752-bf71-a7af0955a47b");
        Xs2aAuthorisationSubResources response = new Xs2aAuthorisationSubResources(authorisations);
        when(piisConsentAuthorisationService.getConsentInitiationAuthorisations(CONSENT_ID))
            .thenReturn(ResponseObject.<Xs2aAuthorisationSubResources>builder().body(response).build());
        //When
        ResponseObject<Xs2aAuthorisationSubResources> xs2aAuthorisationSubResourcesResponseObject = piisConsentService.getConsentInitiationAuthorisations(CONSENT_ID);
        //Then
        assertEquals(response, xs2aAuthorisationSubResourcesResponseObject.getBody());
        verify(piisConsentAuthorisationService, atLeastOnce()).getConsentInitiationAuthorisations(CONSENT_ID);
    }

    @Test
    void getConsentAuthorisationScaStatus_Success() {
        //Given
        ConfirmationOfFundsConsentScaStatus confirmationOfFundsConsentScaStatus = new ConfirmationOfFundsConsentScaStatus(PSU_ID_DATA, piisConsent, ScaStatus.RECEIVED);
        when(piisConsentAuthorisationService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(ResponseObject.<ConfirmationOfFundsConsentScaStatus>builder().body(confirmationOfFundsConsentScaStatus).build());

        when(requestProviderService.getPsuIdData()).thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);

        when(piisConsentSpi.getScaStatus(ScaStatus.RECEIVED, SPI_CONTEXT_DATA, AUTHORISATION_ID, xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent), aspspConsentDataProvider))
            .thenReturn(SpiResponse.<SpiScaStatusResponse>builder()
                            .payload(new SpiScaStatusResponse(ScaStatus.FINALISED, true, "psu message", null, null))
                            .build());

        //When
        ResponseObject<Xs2aScaStatusResponse> xs2aScaStatusResponseResponseObject = piisConsentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        //Then
        assertEquals(ScaStatus.FINALISED, xs2aScaStatusResponseResponseObject.getBody().getScaStatus());
        verify(piisConsentAuthorisationService, atLeastOnce()).getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
    }

    @Test
    void getConsentAuthorisationScaStatus_spiError() {
        //Given
        ResponseObject<Xs2aScaStatusResponse> expected = ResponseObject.<Xs2aScaStatusResponse>builder()
                                                             .fail(ErrorHolder.builder(ErrorType.PIIS_400).build())
                                                             .build();

        ConfirmationOfFundsConsentScaStatus confirmationOfFundsConsentScaStatus = new ConfirmationOfFundsConsentScaStatus(PSU_ID_DATA, piisConsent, ScaStatus.RECEIVED);
        when(piisConsentAuthorisationService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(ResponseObject.<ConfirmationOfFundsConsentScaStatus>builder().body(confirmationOfFundsConsentScaStatus).build());

        when(requestProviderService.getPsuIdData()).thenReturn(PSU_ID_DATA);
        when(spiContextDataProvider.provideWithPsuIdData(PSU_ID_DATA)).thenReturn(SPI_CONTEXT_DATA);
        when(aspspConsentDataProviderFactory.getSpiAspspDataProviderFor(CONSENT_ID))
            .thenReturn(aspspConsentDataProvider);

        TppMessage tppMessage = new TppMessage(MessageErrorCode.SCA_INVALID);
        SpiResponse<SpiScaStatusResponse> spiResponse = SpiResponse.<SpiScaStatusResponse>builder()
                                                            .error(tppMessage)
                                                            .build();
        when(piisConsentSpi.getScaStatus(ScaStatus.RECEIVED, SPI_CONTEXT_DATA, AUTHORISATION_ID, xs2aToSpiPiisConsentMapper.mapToSpiPiisConsent(piisConsent), aspspConsentDataProvider))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.PIIS))
            .thenReturn(ErrorHolder.builder(ErrorType.PIIS_400).build());

        //When
        ResponseObject<Xs2aScaStatusResponse> actual = piisConsentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        //Then
        assertTrue(actual.hasError());
        assertEquals(expected.getError(), actual.getError());

        verify(piisConsentAuthorisationService, atLeastOnce()).getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
    }

    @Test
    void getConsentAuthorisationScaStatus_Failed() {
        //Given
        when(piisConsentAuthorisationService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(ResponseObject.<ConfirmationOfFundsConsentScaStatus>builder().fail(CONSENT_UNKNOWN_ERROR).build());
        //When
        ResponseObject<Xs2aScaStatusResponse> xs2aScaStatusResponseResponseObject = piisConsentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
        //Then
        assertTrue(xs2aScaStatusResponseResponseObject.hasError());
        assertEquals(CONSENT_UNKNOWN_ERROR, xs2aScaStatusResponseResponseObject.getError());
        verify(piisConsentAuthorisationService, atLeastOnce()).getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);
    }

    @Test
    void updateConsentPsuData() {
        ConsentAuthorisationsParameters updateConsentPsuDataReq = new ConsentAuthorisationsParameters();
        piisConsentService.updateConsentPsuData(updateConsentPsuDataReq);

        verify(piisConsentAuthorisationService, times(1)).updateConsentPsuData(updateConsentPsuDataReq);
    }

    private PiisConsent buildPiisConsent(ConsentStatus consentStatus) {
        PiisConsent piisConsent = new PiisConsent(ConsentType.PIIS_TPP);
        piisConsent.setConsentStatus(consentStatus);
        return piisConsent;
    }

    private SpiPiisConsent buildSpiPiisConsent(ConsentStatus consentStatus) {
        SpiPiisConsent spiPiisConsent = new SpiPiisConsent();
        spiPiisConsent.setConsentStatus(consentStatus);
        return spiPiisConsent;
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("Test TppId");
        return tppInfo;
    }
}
