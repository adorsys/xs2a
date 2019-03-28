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

import de.adorsys.psd2.consent.api.ActionStatus;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.event.EventType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.domain.MessageErrorCode;
import de.adorsys.psd2.xs2a.domain.ResponseObject;
import de.adorsys.psd2.xs2a.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.domain.consent.*;
import de.adorsys.psd2.xs2a.exception.MessageCategory;
import de.adorsys.psd2.xs2a.exception.MessageError;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.authorization.ais.RedirectAisAuthorizationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisAuthorisationService;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.AccountReferenceInConsentUpdater;
import de.adorsys.psd2.xs2a.service.consent.AisConsentDataService;
import de.adorsys.psd2.xs2a.service.consent.PisPsuDataService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aAisConsentService;
import de.adorsys.psd2.xs2a.service.context.SpiContextDataProvider;
import de.adorsys.psd2.xs2a.service.event.Xs2aEventService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ErrorType;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceType;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aAccountAccessMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import de.adorsys.psd2.xs2a.service.validator.AisEndpointAccessCheckerService;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.CommonConsentObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.*;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiAisConsentStatusResponse;
import de.adorsys.psd2.xs2a.spi.domain.consent.SpiInitiateAisConsentResponse;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponseStatus;
import de.adorsys.psd2.xs2a.spi.service.AisConsentSpi;
import de.adorsys.psd2.xs2a.web.mapper.TppRedirectUriMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.util.*;

import static de.adorsys.psd2.xs2a.domain.TppMessageInformation.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsentServiceTest {
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String WRONG_CONSENT_ID = "wrong_consent_id";
    private static final String CONSENT_ID_FINALISED = "finalised_consent_id";
    private static final String TPP_ID = "Test TppId";
    private static final String CORRECT_PSU_ID = "123456789";
    private static final String CONSENT_ID = "c966f143-f6a2-41db-9036-8abaeeef3af7";
    private static final String CORRECT_IBAN = "DE123456789";
    private static final String CORRECT_IBAN_1 = "DE987654321";
    private static final String WRONG_IBAN = "WRONG IBAN";
    private static final Currency CURRENCY = Currency.getInstance("EUR");
    private static final Currency CURRENCY_2 = Currency.getInstance("USD");
    private static final LocalDate DATE = LocalDate.now().plusDays(1);
    private static final boolean EXPLICIT_PREFERRED = true;
    private static final AspspConsentData ASPSP_CONSENT_DATA = new AspspConsentData(new byte[0], "Some Consent ID");
    private static final String CONSENT_ID_DATE_VALID_YESTERDAY = "c966f143-f6a2-41db-9036-8abaeeef3af8";
    private static final String CONSENT_ID_DATE_VALID_TODAY = "d4716922-9bbb-45b9-92e3-6ca868ac29d7";
    private static final LocalDate YESTERDAY = LocalDate.now().minus(Period.ofDays(1));
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null);
    private static final SpiPsuData SPI_PSU_DATA = new SpiPsuData(CORRECT_PSU_ID, null, null, null);
    private static final String AUTHORISATION_ID = "a8fc1f02-3639-4528-bd19-3eacf1c67038";
    private static final String WRONG_AUTHORISATION_ID = "wrong authorisation id";
    private static final SpiAccountConsent SPI_ACCOUNT_CONSENT = new SpiAccountConsent();
    private static final OffsetDateTime STATUS_CHANGE_TIMESTAMP = OffsetDateTime.MAX;
    private static final MessageError VALIDATION_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(MessageErrorCode.CONSENT_INVALID));

    @InjectMocks
    private ConsentService consentService;

    @Mock
    private Xs2aAisConsentService aisConsentService;
    @Mock
    private AisConsentDataService aspspConsentDataService;
    @Mock
    private Xs2aAisConsentMapper aisConsentMapper;
    @Mock
    SpiToXs2aAccountAccessMapper spiToXs2aAccountAccessMapper;
    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private AuthorisationMethodDecider authorisationMethodDecider;
    @Mock
    private TppService tppService;
    @Mock
    private AisConsentSpi aisConsentSpi;
    @Mock
    private CreateConsentRequestValidator createConsentRequestValidator;
    @Mock
    private Xs2aToSpiPsuDataMapper psuDataMapper;
    @Mock
    private Xs2aEventService xs2aEventService;
    @Mock
    private AisScaAuthorisationServiceResolver aisScaAuthorisationServiceResolver;
    @Mock
    private PisAuthorisationService pisAuthorisationService;
    @Mock
    private PisScaAuthorisationService pisScaAuthorisationService;
    @Mock
    private PisPsuDataService pisPsuDataService;
    @Mock
    private TppRedirectUriMapper tppRedirectUriMapper;
    @Mock
    private SpiContextDataProvider spiContextDataProvider;
    @Mock
    private AccountReferenceInConsentUpdater accountReferenceUpdater;
    @Mock
    private RedirectAisAuthorizationService redirectAisAuthorizationService;
    @Mock
    private AisEndpointAccessCheckerService endpointAccessCheckerService;
    @Mock
    private SpiErrorMapper spiErrorMapper;

    @Mock
    private GetAccountConsentsStatusByIdValidator getAccountConsentsStatusByIdValidator;
    @Mock
    private GetAccountConsentByIdValidator getAccountConsentByIdValidator;
    @Mock
    private DeleteAccountConsentsByIdValidator deleteAccountConsentsByIdValidator;
    @Mock
    private CreateConsentAuthorisationValidator createConsentAuthorisationValidator;
    @Mock
    private UpdateConsentPsuDataValidator updateConsentPsuDataValidator;
    @Mock
    private GetConsentAuthorisationsValidator getConsentAuthorisationsValidator;
    @Mock
    private GetConsentAuthorisationScaStatusValidator getConsentAuthorisationScaStatusValidator;

    @Before
    public void setUp() {
        //ConsentMapping
        when(spiToXs2aAccountAccessMapper.mapToAccountAccess(any()))
            .thenReturn(Optional.of(getXs2aAccountAccess(Collections.singletonList(getXs2aReference(CORRECT_IBAN, CURRENCY)), null, null, false, false)));

        //AisReportMock
        doNothing().when(aisConsentService).consentActionLog(anyString(), anyString(), any(ActionStatus.class));
        //ByPSU-ID
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);

        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(getReferenceList(), getReferenceList(), getReferenceList(), false, true)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        //ByAccess
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN_1, CURRENCY_2), getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Arrays.asList(getReference(CORRECT_IBAN, CURRENCY), getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);
        when(aisConsentService.createConsent(getCreateConsentRequest(getAccess(
            Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)), PSU_ID_DATA, buildTppInfo()))
            .thenReturn(CONSENT_ID);

        //GetConsentById
        when(aisConsentService.getInitialAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent(CONSENT_ID, DATE, 0)));
        when(aisConsentService.getAccountConsentById(CONSENT_ID)).thenReturn(Optional.of(getAccountConsent(CONSENT_ID, DATE, 0)));
        when(aisConsentService.getAccountConsentById(CONSENT_ID_FINALISED)).thenReturn(Optional.of(getAccountConsentFinalised(CONSENT_ID, getXs2aAccountAccess(Collections.singletonList(getXs2aReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false)));
        when(aisConsentService.getAccountConsentById(CONSENT_ID_DATE_VALID_YESTERDAY)).thenReturn(Optional.of(getAccountConsent(CONSENT_ID_DATE_VALID_YESTERDAY, YESTERDAY, 0)));
        when(aisConsentService.getAccountConsentById(CONSENT_ID_DATE_VALID_TODAY)).thenReturn(Optional.of(getAccountConsent(CONSENT_ID_DATE_VALID_TODAY, LocalDate.now(), 1)));
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID)).thenReturn(null);

        //GetStatusById
        when(aisConsentService.getAccountConsentStatusById(CONSENT_ID))
            .thenReturn(Optional.of(ConsentStatus.RECEIVED));
        when(aisConsentService.getAccountConsentStatusById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());

        when(aspspProfileService.getConsentLifetime())
            .thenReturn(0);

        when(aspspProfileService.getAllPsd2Support())
            .thenReturn(true);

        when(aspspProfileService.isBankOfferedConsentSupported())
            .thenReturn(true);
        when(tppService.getTppId()).thenReturn(TPP_ID);
        when(tppService.getTppInfo()).thenReturn(buildTppInfo());

        when(aspspConsentDataService.getAspspConsentDataByConsentId(anyString()))
            .thenReturn(ASPSP_CONSENT_DATA);

        doNothing().when(aspspConsentDataService).updateAspspConsentData(any(AspspConsentData.class));

        when(psuDataMapper.mapToSpiPsuData(PSU_ID_DATA))
            .thenReturn(SPI_PSU_DATA);

        when(scaApproachResolver.resolveScaApproach())
            .thenReturn(ScaApproach.EMBEDDED);

        when(aisConsentMapper.mapToSpiAccountConsent(any()))
            .thenReturn(SPI_ACCOUNT_CONSENT);

        when(getAccountConsentsStatusByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getAccountConsentByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(deleteAccountConsentsByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(createConsentAuthorisationValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(updateConsentPsuDataValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getConsentAuthorisationsValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
        when(getConsentAuthorisationScaStatusValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.valid());
    }

    @Test
    public void createAccountConsentsWithResponse_Success_AllAccounts() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        //When:
        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false))
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ShouldRecordEvent() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(true, null));
        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false))
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        // When
        consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());

        // Then
        verify(xs2aEventService, times(1)).recordTppRequest(argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.CREATE_AIS_CONSENT_REQUEST_RECEIVED);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_AllPSD2() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)
        );

        //When:
        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false))
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Failure_AllPSD2() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, true)
        );

        //When:
        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(false, createMessageError(ErrorType.AIS_400, MessageErrorCode.PARAMETER_NOT_SUPPORTED)));

        when(aspspProfileService.getAllPsd2Support())
            .thenReturn(false);

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());

        MessageError messageError = responseObj.getError();

        //Then:
        assertThat(messageError).isNotNull();

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.PARAMETER_NOT_SUPPORTED);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ByAccess_Account() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(getReferenceList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        //When:
        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false))
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ByAccess_Balances() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.emptyList(), false, false)
        );

        //When:
        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false))
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Success_ByAccess_Balances_Transactions() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), Collections.singletonList(getReference(CORRECT_IBAN_1, CURRENCY_2)), Collections.singletonList(getReference(CORRECT_IBAN, CURRENCY)), false, false)
        );

        //When:
        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false))
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        CreateConsentResponse response = responseObj.getBody();
        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentWithResponse_Success_BankOfferedConsent() {
        //Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(true, null));

        when(aisConsentSpi.initiateAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiInitiateAisConsentResponse>builder()
                            .payload(new SpiInitiateAisConsentResponse(getSpiAccountAccess(Collections.singletonList(getSpiReference(CORRECT_IBAN, CURRENCY)), null, null, false, false), false))
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        CreateConsentResponse response = responseObj.getBody();

        //Then:
        assertThat(response.getConsentId()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createAccountConsentsWithResponse_Failure() {
        //Given:
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(WRONG_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        //When:
        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(true, null));

        ResponseObject responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        //Then:
        assertThat(responseObj.getError().getErrorType()).isEqualTo(ErrorType.AIS_400);
    }

    @Test
    public void createAccountConsentWithResponse_Failure_BankOfferedConsent() {
        //Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        //When
        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(false, createMessageError(ErrorType.AIS_400, MessageErrorCode.PARAMETER_NOT_SUPPORTED)));

        when(aspspProfileService.isBankOfferedConsentSupported())
            .thenReturn(false);

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        MessageError messageError = responseObj.getError();

        //Then
        assertThat(messageError).isNotNull();
        assertThat(messageError.getErrorType()).isEqualTo(ErrorType.AIS_400);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.PARAMETER_NOT_SUPPORTED);
    }

    @Test
    public void createAccountConsentWithResponse_Failure_NotSupportedAvailableAccounts() {
        //Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), true, false)
        );

        //When
        when(createConsentRequestValidator.validate(req))
            .thenReturn(createValidationResult(false, createMessageError(ErrorType.AIS_405, MessageErrorCode.SERVICE_INVALID_405)));

        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(
            req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());
        MessageError messageError = responseObj.getError();

        //Then
        assertThat(messageError).isNotNull();
        assertThat(messageError.getErrorType()).isEqualTo(ErrorType.AIS_405);

        TppMessageInformation tppMessage = messageError.getTppMessage();

        assertThat(tppMessage).isNotNull();
        assertThat(tppMessage.getMessageErrorCode()).isEqualTo(MessageErrorCode.SERVICE_INVALID_405);
    }

    @Test
    public void createAccountConsentsWithResponse_Failure_No_PSU() {
        //Given:
        when(aspspProfileService.isPsuInInitialRequestMandated()).thenReturn(true);
        //When:
        PsuIdData psuIdData = new PsuIdData(null, null, null, null);
        ResponseObject<CreateConsentResponse> responseObj = consentService.createAccountConsentsWithResponse(null, psuIdData, EXPLICIT_PREFERRED, buildTppRedirectUri());
        //Then:
        MessageError error = responseObj.getError();
        assertThat(error).isNotNull();
        assertThat(error.getErrorType()).isEqualTo(ErrorType.AIS_400);
        assertThat(error.getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
    }

    @Test
    public void createAccountConsentsWithResponse_withInvalidConsent_shouldReturnValidationError() {
        // Given
        CreateConsentReq req = getCreateConsentRequest(
            getAccess(Collections.singletonList(getReference(WRONG_IBAN, CURRENCY)), Collections.emptyList(), Collections.emptyList(), false, false)
        );

        when(createConsentRequestValidator.validate(any(CreateConsentReq.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<CreateConsentResponse> actualResponse = consentService.createAccountConsentsWithResponse(req, PSU_ID_DATA, EXPLICIT_PREFERRED, buildTppRedirectUri());

        // Then
        verify(createConsentRequestValidator).validate(req);
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getAccountConsentsStatusById_Success() {
        //Given:
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                   .success();

        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(spiResponse);

        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID);
        //Then:
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.VALID));
    }

    @Test
    public void getAccountConsentsStatusById_status_finalised_Success() {
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(CONSENT_ID_FINALISED);
        //Then:
        assertThat(response.getBody()).isEqualTo(new ConsentStatusResponse(ConsentStatus.REJECTED));
    }

    @Test
    public void getAccountConsentsStatusById_spi_response_has_error() {
        //Given:
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                   .fail(SpiResponseStatus.LOGICAL_FAILURE);

        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(spiResponse);
        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS))
            .thenReturn(ErrorHolder.builder(MessageErrorCode.FORMAT_ERROR).errorType(ErrorType.AIS_400).build());

        //When:
        ResponseObject actualResponse = consentService.getAccountConsentsStatusById(CONSENT_ID);
        //Then:
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getBody()).isNull();
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.FORMAT_ERROR);
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(ErrorType.AIS_400);
    }

    @Test
    public void getAccountConsentsStatusById_Success_ShouldRecordEvent() {
        //Given:
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                   .success();

        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(spiResponse);
        //When:
        consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_AIS_CONSENT_STATUS_REQUEST_RECEIVED);
    }

    @Test
    public void getAccountConsentsStatusById_Failure() {
        //Given:
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());
        //When:
        ResponseObject response = consentService.getAccountConsentsStatusById(WRONG_CONSENT_ID);
        //Then:
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.AIS_400);
    }

    @Test
    public void getAccountConsentsStatusById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getAccountConsentsStatusByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<ConsentStatusResponse> actualResponse = consentService.getAccountConsentsStatusById(CONSENT_ID);

        // Then
        AccountConsent accountConsent = getAccountConsent(CONSENT_ID, DATE, 0);

        verify(getAccountConsentsStatusByIdValidator).validate(new CommonConsentObject(accountConsent));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getAccountConsentsById_Success() {
        //Given:
        AccountConsent consentExpected = getAccountConsent(CONSENT_ID, DATE, 0);
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                   .success();
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(spiResponse);
        when(aisConsentMapper.mapToAccountConsentWithNewStatus(consentExpected, spiResponse.getPayload().getConsentStatus()))
            .thenReturn(consentExpected);

        //When:
        ResponseObject response = consentService.getAccountConsentById(CONSENT_ID);
        AccountConsent consent = (AccountConsent) response.getBody();
        //Than:
        assertThat(consent.getAccess().getAccounts().get(0).getIban()).isEqualTo(CORRECT_IBAN);
    }

    @Test
    public void getAccountConsentsById_Success_ShouldRecordEvent() {
        //Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        AccountConsent consentExpected = getAccountConsent(CONSENT_ID, DATE, 0);
        SpiResponse<SpiAisConsentStatusResponse> spiResponse = SpiResponse.<SpiAisConsentStatusResponse>builder()
                                                                   .payload(new SpiAisConsentStatusResponse(ConsentStatus.VALID))
                                                                   .aspspConsentData(ASPSP_CONSENT_DATA)
                                                                   .success();
        when(aisConsentSpi.getConsentStatus(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(spiResponse);
        when(aisConsentMapper.mapToAccountConsentWithNewStatus(consentExpected, spiResponse.getPayload().getConsentStatus()))
            .thenReturn(consentExpected);

        // When
        consentService.getAccountConsentById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_AIS_CONSENT_REQUEST_RECEIVED);
    }

    @Test
    public void getAccountConsentsById_Failure() {
        //Given:
        when(aisConsentService.getInitialAccountConsentById(WRONG_CONSENT_ID)).thenReturn(Optional.empty());

        //When:
        ResponseObject response = consentService.getAccountConsentById(WRONG_CONSENT_ID);
        //Than:
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.AIS_403);
    }

    @Test
    public void getAccountConsentsById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getAccountConsentByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<AccountConsent> actualResponse = consentService.getAccountConsentById(CONSENT_ID);

        // Then
        AccountConsent accountConsent = getAccountConsent(CONSENT_ID, DATE, 0);

        verify(getAccountConsentByIdValidator).validate(new CommonConsentObject(accountConsent));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void deleteAccountConsentsById_Success() {
        //When:
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        ResponseObject response = consentService.deleteAccountConsentsById(CONSENT_ID);
        //Than:
        assertThat(response.hasError()).isEqualTo(false);
    }

    @Test
    public void deleteAccountConsentsById_Success_ShouldRecordEvent() {
        when(aisConsentSpi.revokeAisConsent(any(SpiContextData.class), any(SpiAccountConsent.class), any(AspspConsentData.class)))
            .thenReturn(SpiResponse.<SpiResponse.VoidResponse>builder()
                            .payload(SpiResponse.voidResponse())
                            .aspspConsentData(ASPSP_CONSENT_DATA)
                            .success());

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.DELETE_AIS_CONSENT_REQUEST_RECEIVED);
    }

    @Test
    public void deleteAccountConsentsById_Failure() {
        //Given
        when(aisConsentService.getAccountConsentById(WRONG_CONSENT_ID))
            .thenReturn(Optional.empty());
        //When:
        ResponseObject response = consentService.deleteAccountConsentsById(WRONG_CONSENT_ID);
        //Than:
        assertThat(response.getError().getErrorType()).isEqualTo(ErrorType.AIS_400);
    }

    @Test
    public void deleteAccountConsentsById_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(deleteAccountConsentsByIdValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Void> actualResponse = consentService.deleteAccountConsentsById(CONSENT_ID);

        // Then
        AccountConsent accountConsent = getAccountConsent(CONSENT_ID, DATE, 0);

        verify(deleteAccountConsentsByIdValidator).validate(new CommonConsentObject(accountConsent));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void createConsentAuthorizationWithResponse_Success_ShouldRecordEvent() {
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(new CreateConsentAuthorizationResponse()));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.createConsentAuthorizationWithResponse(PSU_ID_DATA, CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.START_AIS_CONSENT_AUTHORISATION_REQUEST_RECEIVED);
    }

    @Test
    public void createConsentAuthorisationWithResponse_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(createConsentAuthorisationValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<CreateConsentAuthorizationResponse> actualResponse = consentService.createConsentAuthorizationWithResponse(PSU_ID_DATA, CONSENT_ID);

        // Then
        AccountConsent accountConsent = getAccountConsent(CONSENT_ID, DATE, 0);

        verify(createConsentAuthorisationValidator).validate(new CommonConsentObject(accountConsent));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void updateConsentPsuData_Success_ShouldRecordEvent() {
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAccountConsentAuthorizationById(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(Optional.of(new AccountConsentAuthorization()));
        when(redirectAisAuthorizationService.createConsentAuthorization(any(), anyString()))
            .thenReturn(Optional.of(new CreateConsentAuthorizationResponse()));
        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq();

        // When
        consentService.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture(), any());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED);
    }

    @Test
    public void updateConsentPsuData_Failure_EndpointIsNotAccessible() {
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq();

        doNothing()
            .when(xs2aEventService).recordAisTppRequest(CONSENT_ID, EventType.UPDATE_AIS_CONSENT_PSU_DATA_REQUEST_RECEIVED, updateConsentPsuDataReq);

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(false);

        ResponseObject<UpdateConsentPsuDataResponse> actualResponse = consentService.updateConsentPsuData(updateConsentPsuDataReq);

        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError().getErrorType()).isEqualTo(ErrorType.AIS_403);
        assertThat(actualResponse.getError().getTppMessage().getMessageErrorCode()).isEqualTo(MessageErrorCode.SERVICE_BLOCKED);
        assertThat(actualResponse.getError().getTppMessage().getCategory()).isEqualTo(MessageCategory.ERROR);
    }

    @Test
    public void updateConsentPsuData_withInvalidConsent_shouldReturnValidationError() {
        // Given
        UpdateConsentPsuDataReq updateConsentPsuDataReq = buildUpdateConsentPsuDataReq();

        when(endpointAccessCheckerService.isEndpointAccessible(AUTHORISATION_ID, CONSENT_ID))
            .thenReturn(true);

        when(updateConsentPsuDataValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<UpdateConsentPsuDataResponse> actualResponse = consentService.updateConsentPsuData(updateConsentPsuDataReq);

        // Then
        AccountConsent accountConsent = getAccountConsent(CONSENT_ID, DATE, 0);

        verify(updateConsentPsuDataValidator).validate(new CommonConsentObject(accountConsent));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getConsentInitiationAuthorisation() {
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationSubResources(anyString()))
            .thenReturn(Optional.of(new Xs2aAuthorisationSubResources(Collections.singletonList(CONSENT_ID))));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        ResponseObject<Xs2aAuthorisationSubResources> paymentInitiationAuthorisation = consentService.getConsentInitiationAuthorisations(CONSENT_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_CONSENT_AUTHORISATION_REQUEST_RECEIVED);

        assertThat(paymentInitiationAuthorisation.getBody()).isNotNull();
        List<String> authorisationIds = paymentInitiationAuthorisation.getBody().getAuthorisationIds();
        assertFalse(authorisationIds.isEmpty());
        assertThat(authorisationIds.get(0)).isEqualTo(CONSENT_ID);
    }

    @Test
    public void getConsentInitiationAuthorisations_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getConsentAuthorisationsValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<Xs2aAuthorisationSubResources> actualResponse = consentService.getConsentInitiationAuthorisations(CONSENT_ID);

        // Then
        AccountConsent accountConsent = getAccountConsent(CONSENT_ID, DATE, 0);

        verify(getConsentAuthorisationsValidator).validate(new CommonConsentObject(accountConsent));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    @Test
    public void getConsentAuthorisationScaStatus_success() {
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        // When
        ResponseObject<ScaStatus> actual = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        assertFalse(actual.hasError());
        assertEquals(ScaStatus.RECEIVED, actual.getBody());
    }

    @Test
    public void getConsentAuthorisationScaStatus_success_shouldRecordEvent() {
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(any(), any()))
            .thenReturn(Optional.of(ScaStatus.RECEIVED));

        // Given:
        ArgumentCaptor<EventType> argumentCaptor = ArgumentCaptor.forClass(EventType.class);

        // When
        consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        verify(xs2aEventService, times(1)).recordAisTppRequest(eq(CONSENT_ID), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue()).isEqualTo(EventType.GET_CONSENT_SCA_STATUS_REQUEST_RECEIVED);
    }

    @Test
    public void getConsentAuthorisationScaStatus_failure() {
        when(aisScaAuthorisationServiceResolver.getService()).thenReturn(redirectAisAuthorizationService);
        when(redirectAisAuthorizationService.getAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID))
            .thenReturn(Optional.empty());

        // When
        ResponseObject<ScaStatus> actual = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, WRONG_AUTHORISATION_ID);

        // Then
        assertTrue(actual.hasError());
        assertNull(actual.getBody());
    }

    @Test
    public void getConsentAuthorisationScaStatus_withInvalidConsent_shouldReturnValidationError() {
        // Given
        when(getConsentAuthorisationScaStatusValidator.validate(any(CommonConsentObject.class)))
            .thenReturn(ValidationResult.invalid(VALIDATION_ERROR));

        // When
        ResponseObject<ScaStatus> actualResponse = consentService.getConsentAuthorisationScaStatus(CONSENT_ID, AUTHORISATION_ID);

        // Then
        AccountConsent accountConsent = getAccountConsent(CONSENT_ID, DATE, 0);

        verify(getConsentAuthorisationScaStatusValidator).validate(new CommonConsentObject(accountConsent));
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.hasError()).isTrue();
        assertThat(actualResponse.getError()).isEqualTo(VALIDATION_ERROR);
    }

    /**
     * Basic test AccountDetails used in all cases
     */

    private SpiAccountReference getSpiReference(String iban, Currency currency) {
        return new SpiAccountReference(null, iban, null, null, null, null, currency);
    }

    private AccountReference getXs2aReference(String iban, Currency currency) {
        return new AccountReference(ASPSP_ACCOUNT_ID, null, iban, null, null, null, null, currency);
    }

    private Optional<SpiAccountAccess> getSpiAccountAccessOptional(List<SpiAccountReference> accounts, List<SpiAccountReference> balances, List<SpiAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return Optional.of(getSpiAccountAccess(accounts, balances, transactions, allAccounts, allPsd2));
    }

    private SpiAccountAccess getSpiAccountAccess(List<SpiAccountReference> accounts, List<SpiAccountReference> balances, List<SpiAccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new SpiAccountAccess(accounts, balances, transactions, allAccounts ? AccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null);
    }

    private Xs2aAccountAccess getXs2aAccountAccess(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new Xs2aAccountAccess(accounts, balances, transactions, allAccounts ? AccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null);
    }

    private AccountConsent getConsent(String id, Xs2aAccountAccess access, boolean withBalance) {
        return new AccountConsent(id, access, false, DATE, 4, null, ConsentStatus.VALID, withBalance, false, null, buildTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), STATUS_CHANGE_TIMESTAMP, 0);
    }

    private SpiAccountConsent getSpiConsent(String consentId, SpiAccountAccess access, boolean withBalance) {
        return new SpiAccountConsent(consentId, access, false, DATE, 4, null, ConsentStatus.VALID, withBalance, false, null, buildTppInfo(), AisConsentRequestType.GLOBAL, STATUS_CHANGE_TIMESTAMP);
    }

    private AccountConsent getAccountConsent(String consentId, LocalDate validUntil, int usageCounter) {
        Xs2aAccountAccess access = getXs2aAccountAccess(Collections.singletonList(getXs2aReference(CORRECT_IBAN, CURRENCY)), null, null, false, false);

        return new AccountConsent(consentId, access, false, validUntil, 4, null, ConsentStatus.VALID, false, false, null, buildTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), OffsetDateTime.MAX, usageCounter);
    }

    private AccountConsent getAccountConsentFinalised(String consentId, Xs2aAccountAccess access, boolean withBalance) {
        return new AccountConsent(consentId, access, false, DATE, 4, null, ConsentStatus.REJECTED, withBalance, false, null, buildTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), STATUS_CHANGE_TIMESTAMP, 0);
    }

    private AccountConsent getAccountConsentDateValidYesterday(String consentId, Xs2aAccountAccess access, boolean withBalance) {
        return new AccountConsent(consentId, access, false, YESTERDAY, 4, null, ConsentStatus.VALID, withBalance, false, null, buildTppInfo(), AisConsentRequestType.GLOBAL, false, Collections.emptyList(), STATUS_CHANGE_TIMESTAMP, 0);
    }

    private CreateConsentReq getCreateConsentRequest(Xs2aAccountAccess access) {
        CreateConsentReq req = new CreateConsentReq();
        req.setAccess(access);
        req.setValidUntil(DATE);
        req.setFrequencyPerDay(4);
        req.setCombinedServiceIndicator(false);
        req.setRecurringIndicator(false);
        return req;
    }

    private Xs2aAccountAccess getAccess(List<AccountReference> accounts, List<AccountReference> balances, List<AccountReference> transactions, boolean allAccounts, boolean allPsd2) {
        return new Xs2aAccountAccess(accounts, balances, transactions, allAccounts ? AccountAccessType.ALL_ACCOUNTS : null, allPsd2 ? AccountAccessType.ALL_ACCOUNTS : null);
    }

    private List<AccountReference> getReferenceList() {
        List<AccountReference> list = new ArrayList<>();
        list.add(getReference(CORRECT_IBAN, CURRENCY));
        list.add(getReference(CORRECT_IBAN_1, CURRENCY_2));

        return list;
    }

    private AccountReference getReference(String iban, Currency currency) {
        AccountReference ref = new AccountReference();
        ref.setIban(iban);
        ref.setCurrency(currency);
        return ref;
    }

    private ValidationResult createValidationResult(boolean isValid, MessageError messageError) {
        return isValid
                   ? ValidationResult.valid()
                   : ValidationResult.invalid(messageError);
    }

    private MessageError createMessageError(ErrorType errorType, MessageErrorCode errorCode) {
        return new MessageError(errorType, of(errorCode));
    }

    private UpdateConsentPsuDataReq buildUpdateConsentPsuDataReq() {
        UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();
        request.setAuthorizationId(AUTHORISATION_ID);
        request.setConsentId(CONSENT_ID);
        return request;
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_ID);
        tppInfo.setTppRedirectUri(buildTppRedirectUri());
        return tppInfo;
    }

    private TppRedirectUri buildTppRedirectUri() {
        return new TppRedirectUri("redirectUri", "nokRedirectUri");
    }
}
