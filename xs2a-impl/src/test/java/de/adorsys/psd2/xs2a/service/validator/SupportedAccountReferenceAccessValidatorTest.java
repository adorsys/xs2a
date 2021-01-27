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

package de.adorsys.psd2.xs2a.service.validator;

import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.domain.account.SupportedAccountReferenceField;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.discovery.ServiceTypeDiscoveryService;
import de.adorsys.psd2.xs2a.service.mapper.psd2.ServiceTypeToErrorTypeMapper;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Currency;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupportedAccountReferenceAccessValidatorTest {
    private static final MessageError IBAN_NOT_SUPPORTED_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR_ATTRIBUTE_NOT_SUPPORTED, "IBAN"));
    private static final MessageError BBAN_NOT_SUPPORTED_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR_ATTRIBUTE_NOT_SUPPORTED, "BBAN"));
    private static final MessageError ONLY_ONE_ATTRIBUTE_ALLOWED_ERROR =
        new MessageError(ErrorType.AIS_400, TppMessageInformation.of(FORMAT_ERROR_MULTIPLE_ACCOUNT_REFERENCES));
    private static final ServiceType SERVICE_TYPE = ServiceType.AIS;
    private static final AccountReference ACCOUNT_REFERENCE_IBAN =
        new AccountReference(AccountReferenceType.IBAN, "iban value", Currency.getInstance("EUR"));
    private static final AccountReference ACCOUNT_REFERENCE_BBAN =
        new AccountReference(AccountReferenceType.BBAN, "bban value", Currency.getInstance("EUR"));
    private static final AccountReference ACCOUNT_REFERENCE_ALL =
        new AccountReference("account ID", "resource ID", "iban value", "bban value", "pan value", "maskedpan value", "msisdn value", Currency.getInstance("EUR"), null);

    @Mock
    private AspspProfileServiceWrapper aspspProfileService;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private ServiceTypeDiscoveryService serviceTypeDiscoveryService;
    @Mock
    private ServiceTypeToErrorTypeMapper errorTypeMapper;

    @InjectMocks
    private SupportedAccountReferenceValidator supportedAccountReferenceValidator;

    @Test
    void validate_withSupportedAccountReferences_shouldReturnValid() {
        // Given
        when(aspspProfileService.getSupportedAccountReferenceFields())
            .thenReturn(Collections.singletonList(SupportedAccountReferenceField.IBAN));
        Collection<AccountReference> accountReferences = Collections.singletonList(ACCOUNT_REFERENCE_IBAN);

        //When
        ValidationResult validationResult = supportedAccountReferenceValidator.validate(accountReferences);

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_emptyCollection_shouldReturnValid() {
        // Given

        //When
        ValidationResult validationResult = supportedAccountReferenceValidator.validate(Collections.emptyList());

        //Then
        assertTrue(validationResult.isValid());
    }

    @Test
    void validate_withUnsupportedAccountReferences_shouldReturnFormatError() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode())).thenReturn(ErrorType.AIS_400);
        when(aspspProfileService.getSupportedAccountReferenceFields())
            .thenReturn(Collections.singletonList(SupportedAccountReferenceField.IBAN));
        Collection<AccountReference> accountReferences = Collections.singletonList(ACCOUNT_REFERENCE_BBAN);

        //When
        ValidationResult validationResult = supportedAccountReferenceValidator.validate(accountReferences);

        //Then
        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorTypeMapper).mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode());

        assertTrue(validationResult.isNotValid());
        assertEquals(BBAN_NOT_SUPPORTED_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withOneUnsupportedAccountReferences_shouldReturnFormatError() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode())).thenReturn(ErrorType.AIS_400);
        when(aspspProfileService.getSupportedAccountReferenceFields())
            .thenReturn(Collections.singletonList(SupportedAccountReferenceField.IBAN));
        Collection<AccountReference> accountReferences = Arrays.asList(ACCOUNT_REFERENCE_IBAN, ACCOUNT_REFERENCE_BBAN);

        //When
        ValidationResult validationResult = supportedAccountReferenceValidator.validate(accountReferences);

        //Then
        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorTypeMapper).mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode());

        assertTrue(validationResult.isNotValid());
        assertEquals(BBAN_NOT_SUPPORTED_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withSeveralAccountReferences_shouldReturnFormatError() {
        // Given
        Collection<AccountReference> accountReferences = Collections.singletonList(ACCOUNT_REFERENCE_ALL);
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode())).thenReturn(ErrorType.AIS_400);

        //When
        ValidationResult validationResult = supportedAccountReferenceValidator.validate(accountReferences);

        //Then
        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorTypeMapper).mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode());

        assertTrue(validationResult.isNotValid());
        assertEquals(ONLY_ONE_ATTRIBUTE_ALLOWED_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withNoSupportedAccountReferencesInProfile_shouldReturnFormatError() {
        // Given
        when(serviceTypeDiscoveryService.getServiceType()).thenReturn(SERVICE_TYPE);
        when(errorTypeMapper.mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode())).thenReturn(ErrorType.AIS_400);
        when(aspspProfileService.getSupportedAccountReferenceFields()).thenReturn(Collections.emptyList());
        Collection<AccountReference> accountReferences = Collections.singletonList(ACCOUNT_REFERENCE_IBAN);

        //When
        ValidationResult validationResult = supportedAccountReferenceValidator.validate(accountReferences);

        //Then
        verify(serviceTypeDiscoveryService).getServiceType();
        verify(errorTypeMapper).mapToErrorType(SERVICE_TYPE, FORMAT_ERROR.getCode());

        assertTrue(validationResult.isNotValid());
        assertEquals(IBAN_NOT_SUPPORTED_ERROR, validationResult.getMessageError());
    }
}
