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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.consent.CreateConsentReq;
import de.adorsys.psd2.xs2a.service.validator.TppNotificationDataValidator;
import de.adorsys.psd2.xs2a.service.validator.TppUriHeaderValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.CommonConsentObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.*;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentAuthorisationObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.CreateConsentRequestObject;
import de.adorsys.psd2.xs2a.service.validator.ais.consent.dto.UpdateConsentPsuDataRequestObject;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsentValidationServiceTest {
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
    private AisConsent aisConsent;
    private ArgumentCaptor<CommonConsentObject> commonConsentObjectCaptor;
    private ArgumentCaptor<CreateConsentAuthorisationObject> consentAuthorisationPOArgumentCaptor;

    @BeforeEach
    void setUp() {
        aisConsent = jsonReader.getObjectFromFile("json/service/ais-consent.json", AisConsent.class);
        commonConsentObjectCaptor = ArgumentCaptor.forClass(CommonConsentObject.class);
        consentAuthorisationPOArgumentCaptor = ArgumentCaptor.forClass(CreateConsentAuthorisationObject.class);
    }

    @Test
    void validateConsentOnCreate() {
        ArgumentCaptor<CreateConsentRequestObject> createConsentRequestObjectCaptor = ArgumentCaptor.forClass(CreateConsentRequestObject.class);
        CreateConsentReq createConsentReq = new CreateConsentReq();
        when(createConsentRequestValidator.validate(createConsentRequestObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentOnCreate(createConsentReq, PSU_ID_DATA);

        verify(createConsentRequestValidator).validate(any(CreateConsentRequestObject.class));
        assertEquals(createConsentReq, createConsentRequestObjectCaptor.getValue().getCreateConsentReq());
        assertEquals(PSU_ID_DATA, createConsentRequestObjectCaptor.getValue().getPsuIdData());
    }

    @Test
    void validateConsentOnGettingStatusById() {
        when(getAccountConsentsStatusByIdValidator.validate(commonConsentObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentOnGettingStatusById(aisConsent);

        verify(getAccountConsentsStatusByIdValidator).validate(any(CommonConsentObject.class));
        assertEquals(aisConsent, commonConsentObjectCaptor.getValue().getAisConsent());
    }

    @Test
    void validateConsentOnDelete() {
        when(deleteAccountConsentsByIdValidator.validate(commonConsentObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentOnDelete(aisConsent);

        verify(deleteAccountConsentsByIdValidator).validate(any(CommonConsentObject.class));
        assertEquals(aisConsent, commonConsentObjectCaptor.getValue().getAisConsent());
    }

    @Test
    void validateConsentOnGettingById() {
        when(getAccountConsentByIdValidator.validate(commonConsentObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentOnGettingById(aisConsent);

        verify(getAccountConsentByIdValidator).validate(any(CommonConsentObject.class));
        assertEquals(aisConsent, commonConsentObjectCaptor.getValue().getAisConsent());
    }

    @Test
    void validateConsentAuthorisationOnCreate() {
        when(createConsentAuthorisationValidator.validate(consentAuthorisationPOArgumentCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentAuthorisationOnCreate(new CreateConsentAuthorisationObject(aisConsent, new PsuIdData(null, null, null, null, null)));

        verify(createConsentAuthorisationValidator).validate(any(CreateConsentAuthorisationObject.class));
        assertEquals(aisConsent, consentAuthorisationPOArgumentCaptor.getValue().getAisConsent());
    }

    @Test
    void validateConsentPsuDataOnUpdate() {
        ArgumentCaptor<UpdateConsentPsuDataRequestObject> updateConsentPsuDataRequestObjectCaptor = ArgumentCaptor.forClass(UpdateConsentPsuDataRequestObject.class);
        ConsentAuthorisationsParameters request = new ConsentAuthorisationsParameters();

        when(updateConsentPsuDataValidator.validate(updateConsentPsuDataRequestObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentPsuDataOnUpdate(aisConsent, request);

        verify(updateConsentPsuDataValidator).validate(any(UpdateConsentPsuDataRequestObject.class));
        assertEquals(aisConsent, updateConsentPsuDataRequestObjectCaptor.getValue().getAisConsent());
        assertEquals(request, updateConsentPsuDataRequestObjectCaptor.getValue().getUpdateRequest());
    }

    @Test
    void validateConsentAuthorisationOnGettingById() {
        when(getConsentAuthorisationsValidator.validate(commonConsentObjectCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentAuthorisationOnGettingById(aisConsent);

        verify(getConsentAuthorisationsValidator).validate(any(CommonConsentObject.class));
        assertEquals(aisConsent, commonConsentObjectCaptor.getValue().getAisConsent());
    }

    @Test
    void validateConsentAuthorisationScaStatus() {
        ArgumentCaptor<GetConsentAuthorisationScaStatusPO> getConsentAuthorisationScaStatusPOCaptor = ArgumentCaptor.forClass(GetConsentAuthorisationScaStatusPO.class);

        when(getConsentAuthorisationScaStatusValidator.validate(getConsentAuthorisationScaStatusPOCaptor.capture())).thenReturn(ValidationResult.valid());

        service.validateConsentAuthorisationScaStatus(aisConsent, AUTHORISATION_ID);

        verify(getConsentAuthorisationScaStatusValidator).validate(any(GetConsentAuthorisationScaStatusPO.class));
        assertEquals(aisConsent, getConsentAuthorisationScaStatusPOCaptor.getValue().getAisConsent());
        assertEquals(AUTHORISATION_ID, getConsentAuthorisationScaStatusPOCaptor.getValue().getAuthorisationId());
    }

    @Test
    void buildWarningMessages_emptySet() {
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
    void buildWarningMessages_warningsFromUriHeaderValidator() {
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
    void buildWarningMessages_warningsNotificationDataValidator() {
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
