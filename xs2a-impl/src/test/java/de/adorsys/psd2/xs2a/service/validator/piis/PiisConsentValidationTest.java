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

package de.adorsys.psd2.xs2a.service.validator.piis;


import de.adorsys.psd2.core.data.Consent;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.fund.PiisConsentValidationResult;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static de.adorsys.psd2.xs2a.core.error.ErrorType.PIIS_400;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PiisConsentValidationTest {
    private static final String PIIS_CONSENT_JSON_PATH = "json/service/validator/piis/piis-consent-request.json";
    private static final String WRONG_PIIS_CONSENT_JSON_PATH = "json/service/validator/piis/wrong-piis-consent-request.json";
    private static final String REVOKED_PIIS_CONSENT_JSON_PATH = "json/service/validator/piis/piis-consent-revoked.json";
    private static final String EXPIRED_PIIS_CONSENT_JSON_PATH = "json/service/validator/piis/piis-consent-expired.json";
    private static final String PIIS_CONSENT_WRONG_TPP_JSON_PATH = "json/service/validator/piis/piis-consent-wrong-tpp.json";
    private static final String AUTHORISATION_NUMBER = "12345987";
    private static final String DIFFERENT_AUTHORISATION_NUMBER = "different authorisation number";

    private JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private PiisConsentValidation piisConsentValidation;
    @Mock
    private TppService tppService;

    private TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    @Test
    void validatePiisConsentData_validList_successful() {
        // Given
        PiisConsent validConsent = buildConsent();
        PiisConsent wrongConsent = buildWrongPiisConsent();

        List<PiisConsent> piisConsents = new ArrayList<>();
        piisConsents.add(validConsent);
        piisConsents.add(wrongConsent);

        when(tppService.getTppInfo()).thenReturn(buildTppInfo(AUTHORISATION_NUMBER));

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertNotNull(validationResult);
        assertFalse(validationResult.hasError());
        assertEquals(validationResult.getConsent(), validConsent);
    }

    @Test
    void validatePiisConsentData_SeveralTppForOneAccount_successful() {
        // Given
        List<PiisConsent> piisConsents = Arrays.asList(buildConsent(AUTHORISATION_NUMBER), buildConsent(DIFFERENT_AUTHORISATION_NUMBER));
        Map<String, PiisConsent> piisConsentMap = piisConsents.stream().collect(Collectors.toMap(piisConsent -> Optional.of(piisConsent)
                                                                                                                    .map(Consent::getTppInfo)
                                                                                                                    .map(TppInfo::getAuthorisationNumber)
                                                                                                                    .orElse(null), Function.identity()));

        piisConsentMap.forEach((authNumber, piisConsent) -> {
            // When
            when(tppService.getTppInfo()).thenReturn(buildTppInfo(authNumber));
            PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);
            // Then
            assertNotNull(validationResult);
            assertFalse(validationResult.hasError());
            assertEquals(validationResult.getConsent(), piisConsentMap.get(authNumber));
        });
    }

    @Test
    void validatePiisConsentData_emptyList_shouldReturnError() {
        // Given
        List<PiisConsent> piisConsents = new ArrayList<>();

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.hasError());
        assertEquals(MessageErrorCode.NO_PIIS_ACTIVATION, validationResult.getErrorHolder().getTppMessageInformationList().iterator().next().getMessageErrorCode());
        assertEquals(PIIS_400, validationResult.getErrorHolder().getErrorType());
    }

    @Test
    void validatePiisConsentData_revokedConsent_shouldReturnConsentUnknown() {
        // Given
        PiisConsent revokedConsent = jsonReader.getObjectFromFile(REVOKED_PIIS_CONSENT_JSON_PATH, PiisConsent.class);
        List<PiisConsent> piisConsents = Collections.singletonList(revokedConsent);

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.hasError());
        ErrorHolder errorHolder = validationResult.getErrorHolder();
        assertEquals(PIIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void validatePiisConsentData_expiredConsent_shouldReturnConsentUnknown() {
        // Given
        PiisConsent expiredConsent = jsonReader.getObjectFromFile(EXPIRED_PIIS_CONSENT_JSON_PATH, PiisConsent.class);
        List<PiisConsent> piisConsents = Collections.singletonList(expiredConsent);

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.hasError());
        ErrorHolder errorHolder = validationResult.getErrorHolder();
        assertEquals(PIIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());
    }

    @Test
    void validatePiisConsentData_consentForWrongTpp_shouldReturnConsentUnknown() {
        // Given
        PiisConsent consentForWrongTpp = jsonReader.getObjectFromFile(PIIS_CONSENT_WRONG_TPP_JSON_PATH, PiisConsent.class);
        List<PiisConsent> piisConsents = Collections.singletonList(consentForWrongTpp);
        when(tppService.getTppInfo()).thenReturn(buildTppInfo(AUTHORISATION_NUMBER));

        // When
        PiisConsentValidationResult validationResult = piisConsentValidation.validatePiisConsentData(piisConsents);

        // Then
        assertNotNull(validationResult);
        assertTrue(validationResult.hasError());
        ErrorHolder errorHolder = validationResult.getErrorHolder();
        assertEquals(PIIS_400, errorHolder.getErrorType());
        assertEquals(Collections.singletonList(TppMessageInformation.of(MessageErrorCode.CONSENT_UNKNOWN_400)), errorHolder.getTppMessageInformationList());
    }

    private PiisConsent buildWrongPiisConsent() {
        return jsonReader.getObjectFromFile(WRONG_PIIS_CONSENT_JSON_PATH, PiisConsent.class);
    }

    private PiisConsent buildConsent() {
        return jsonReader.getObjectFromFile(PIIS_CONSENT_JSON_PATH, PiisConsent.class);
    }

    private PiisConsent buildConsent(String authorisationNumber) {
        PiisConsent piisConsent = buildConsent();
        piisConsent.setConsentTppInformation(buildConsentTppInformation(authorisationNumber));
        return piisConsent;
    }

    private ConsentTppInformation buildConsentTppInformation(String authorisationNumber) {
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        consentTppInformation.setTppInfo(tppInfo);
        return consentTppInformation;
    }

    private void assertThatErrorIs(PiisConsentValidationResult validationResult, MessageErrorCode consentUnknown400) {
        assertNotNull(validationResult);
        assertTrue(validationResult.hasError());
        assertEquals(consentUnknown400, validationResult.getErrorHolder().getTppMessageInformationList().iterator().next().getMessageErrorCode());
        assertEquals(PIIS_400, validationResult.getErrorHolder().getErrorType());
    }
}
