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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.domain.consent.UpdateConsentPsuDataReq;
import de.adorsys.psd2.xs2a.service.validator.TppNotificationDataValidator;
import de.adorsys.psd2.xs2a.service.validator.TppUriHeaderValidator;
import de.adorsys.psd2.xs2a.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.service.validator.ais.CommonConsentObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.*;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentAuthorisationObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ConsentValidationServiceTest {
    private static final String PSU_ID = "123456789";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(PSU_ID, null, null, null, null);
    private static final String AUTHORISATION_ID = "authorisation id";
    private static final String INVALID_DOMAIN_MESSAGE = "TPP URIs are not compliant with the domain secured by the eIDAS QWAC certificate of the TPP in the field CN or SubjectAltName of the certificate";

    @InjectMocks
    private ConsentValidationService service;

    @Mock
    private CreateConsentRequestValidator createConsentRequestValidator;
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
    @Mock
    private TppUriHeaderValidator tppUriHeaderValidator;
    @Mock
    private TppNotificationDataValidator tppNotificationDataValidator;

    private JsonReader jsonReader = new JsonReader();
    private AccountConsent accountConsent;
    private ArgumentCaptor<CommonConsentObject> commonConsentObjectCaptor;
    private ArgumentCaptor<CreateConsentAuthorisationObject> consentAuthorisationPOArgumentCaptor;

    @Before
    public void setUp() {
        accountConsent = jsonReader.getObjectFromFile("json/service/account-consent.json", AccountConsent.class);
        commonConsentObjectCaptor = ArgumentCaptor.forClass(CommonConsentObject.class);
        consentAuthorisationPOArgumentCaptor = ArgumentCaptor.forClass(CreateConsentAuthorisationObject.class);
    }

    @Test
    public void validateConsentOnCreate() {
        ArgumentCaptor<CreateConsentRequestObject> createConsentRequestObjectCaptor = ArgumentCaptor.forClass(CreateConsentRequestObject.class);
        CreateConsentReq createConsentReq = new CreateConsentReq();
        when(createConsentRequestValidator.validate(createConsentRequestObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentOnCreate(createConsentReq, PSU_ID_DATA);

        verify(createConsentRequestValidator).validate(any(CreateConsentRequestObject.class));
        assertEquals(createConsentReq, createConsentRequestObjectCaptor.getValue().getCreateConsentReq());
        assertEquals(PSU_ID_DATA, createConsentRequestObjectCaptor.getValue().getPsuIdData());
    }

    @Test
    public void validateConsentOnGettingStatusById() {
        when(getAccountConsentsStatusByIdValidator.validate(commonConsentObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentOnGettingStatusById(accountConsent);

        verify(getAccountConsentsStatusByIdValidator).validate(any(CommonConsentObject.class));
        assertEquals(accountConsent, commonConsentObjectCaptor.getValue().getAccountConsent());
    }

    @Test
    public void validateConsentOnDelete() {
        when(deleteAccountConsentsByIdValidator.validate(commonConsentObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentOnDelete(accountConsent);

        verify(deleteAccountConsentsByIdValidator).validate(any(CommonConsentObject.class));
        assertEquals(accountConsent, commonConsentObjectCaptor.getValue().getAccountConsent());
    }

    @Test
    public void validateConsentOnGettingById() {
        when(getAccountConsentByIdValidator.validate(commonConsentObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentOnGettingById(accountConsent);

        verify(getAccountConsentByIdValidator).validate(any(CommonConsentObject.class));
        assertEquals(accountConsent, commonConsentObjectCaptor.getValue().getAccountConsent());
    }

    @Test
    public void validateConsentAuthorisationOnCreate() {
        when(createConsentAuthorisationValidator.validate(consentAuthorisationPOArgumentCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentAuthorisationOnCreate(new CreateConsentAuthorisationObject(accountConsent, new PsuIdData(null, null, null, null, null)));

        verify(createConsentAuthorisationValidator).validate(any(CreateConsentAuthorisationObject.class));
        assertEquals(accountConsent, consentAuthorisationPOArgumentCaptor.getValue().getAccountConsent());
    }

    @Test
    public void validateConsentPsuDataOnUpdate() {
        ArgumentCaptor<UpdateConsentPsuDataRequestObject> updateConsentPsuDataRequestObjectCaptor = ArgumentCaptor.forClass(UpdateConsentPsuDataRequestObject.class);
        UpdateConsentPsuDataReq request = new UpdateConsentPsuDataReq();

        when(updateConsentPsuDataValidator.validate(updateConsentPsuDataRequestObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentPsuDataOnUpdate(accountConsent, request);

        verify(updateConsentPsuDataValidator).validate(any(UpdateConsentPsuDataRequestObject.class));
        assertEquals(accountConsent, updateConsentPsuDataRequestObjectCaptor.getValue().getAccountConsent());
        assertEquals(request, updateConsentPsuDataRequestObjectCaptor.getValue().getUpdateRequest());
    }

    @Test
    public void validateConsentAuthorisationOnGettingById() {
        when(getConsentAuthorisationsValidator.validate(commonConsentObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentAuthorisationOnGettingById(accountConsent);

        verify(getConsentAuthorisationsValidator).validate(any(CommonConsentObject.class));
        assertEquals(accountConsent, commonConsentObjectCaptor.getValue().getAccountConsent());
    }

    @Test
    public void validateConsentAuthorisationScaStatus() {
        ArgumentCaptor<GetConsentAuthorisationScaStatusPO> getConsentAuthorisationScaStatusPOCaptor = ArgumentCaptor.forClass(GetConsentAuthorisationScaStatusPO.class);

        when(getConsentAuthorisationScaStatusValidator.validate(getConsentAuthorisationScaStatusPOCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentAuthorisationScaStatus(accountConsent, AUTHORISATION_ID);

        verify(getConsentAuthorisationScaStatusValidator).validate(any(GetConsentAuthorisationScaStatusPO.class));
        assertEquals(accountConsent, getConsentAuthorisationScaStatusPOCaptor.getValue().getAccountConsent());
        assertEquals(AUTHORISATION_ID, getConsentAuthorisationScaStatusPOCaptor.getValue().getAuthorisationId());
    }

    @Test
    public void buildWarningMessages_emptySet() {
        // Given
        Set<TppMessageInformation> emptySet = new HashSet<>();
        CreateConsentReq createConsentReq = new CreateConsentReq();
        when(tppUriHeaderValidator.buildWarningMessages(any()))
            .thenReturn(emptySet);
        when(tppNotificationDataValidator.buildWarningMessages(any()))
            .thenReturn(emptySet);

        // When
        Set<TppMessageInformation> actual = service.buildWarningMessages(createConsentReq);

        // Then
        assertEquals(actual, emptySet);
        verify(tppUriHeaderValidator, times(1)).buildWarningMessages(any());
        verify(tppNotificationDataValidator, times(1)).buildWarningMessages(any());
    }

    @Test
    public void buildWarningMessages_warningsFromUriHeaderValidator() {
        // Given
        Set<TppMessageInformation> emptySet = new HashSet<>();
        Set<TppMessageInformation> uriHeaderValidatorSet = new HashSet<>();
        uriHeaderValidatorSet.add(TppMessageInformation.buildWarning(INVALID_DOMAIN_MESSAGE));

        CreateConsentReq createConsentReq = new CreateConsentReq();
        when(tppUriHeaderValidator.buildWarningMessages(any()))
            .thenReturn(uriHeaderValidatorSet);
        when(tppNotificationDataValidator.buildWarningMessages(any()))
            .thenReturn(emptySet);

        // When
        Set<TppMessageInformation> actual = service.buildWarningMessages(createConsentReq);

        // Then
        assertEquals(actual, uriHeaderValidatorSet);
        verify(tppUriHeaderValidator, times(1)).buildWarningMessages(any());
        verify(tppNotificationDataValidator, times(1)).buildWarningMessages(any());
    }

    @Test
    public void buildWarningMessages_warningsNotificationDataValidator() {
        // Given
        Set<TppMessageInformation> emptySet = new HashSet<>();
        Set<TppMessageInformation> notificationDataValidatorSet = new HashSet<>();
        notificationDataValidatorSet.add(TppMessageInformation.buildWarning(INVALID_DOMAIN_MESSAGE));

        CreateConsentReq createConsentReq = new CreateConsentReq();
        when(tppUriHeaderValidator.buildWarningMessages(any()))
            .thenReturn(emptySet);
        when(tppNotificationDataValidator.buildWarningMessages(any()))
            .thenReturn(notificationDataValidatorSet);

        // When
        Set<TppMessageInformation> actual = service.buildWarningMessages(createConsentReq);

        // Then
        assertEquals(actual, notificationDataValidatorSet);
        verify(tppUriHeaderValidator, times(1)).buildWarningMessages(any());
        verify(tppNotificationDataValidator, times(1)).buildWarningMessages(any());
    }
}
