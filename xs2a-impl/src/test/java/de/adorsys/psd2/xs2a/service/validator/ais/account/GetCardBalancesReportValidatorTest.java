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

package de.adorsys.psd2.xs2a.service.validator.ais.account;

import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageError;
import de.adorsys.psd2.xs2a.core.service.validator.ValidationResult;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.validator.OauthConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountConsentValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.common.AccountReferenceAccessValidator;
import de.adorsys.psd2.xs2a.service.validator.ais.account.dto.GetCardAccountBalanceRequestObject;
import de.adorsys.psd2.xs2a.service.validator.tpp.AisAccountTppInfoValidator;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.CONSENT_INVALID;
import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.UNAUTHORIZED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetCardBalancesReportValidatorTest {
    private static final TppInfo TPP_INFO = buildTppInfo("authorisation number");
    private static final TppInfo INVALID_TPP_INFO = buildTppInfo("invalid authorisation number");
    private static final String REQUEST_URI = "/accounts";
    private static final String ACCOUNT_ID = "11111-999999999";

    private static final MessageError TPP_VALIDATION_ERROR =
        new MessageError(ErrorType.PIS_401, TppMessageInformation.of(UNAUTHORIZED));

    private static final MessageError CONSENT_INVALID_ERROR =
        new MessageError(ErrorType.AIS_401, TppMessageInformation.of(CONSENT_INVALID));

    @Mock
    private AccountConsentValidator accountConsentValidator;
    @Mock
    private AisAccountTppInfoValidator aisAccountTppInfoValidator;
    @Mock
    private AccountReferenceAccessValidator accountReferenceAccessValidator;
    @Mock
    private OauthConsentValidator oauthConsentValidator;

    @InjectMocks
    private GetCardBalancesReportValidator getCardBalancesReportValidator;

    private final JsonReader jsonReader = new JsonReader();
    private AccountAccess accountAccess;

    @BeforeEach
    void setUp() {
        accountAccess = jsonReader.getObjectFromFile("json/service/validator/ais/account/xs2a-account-access.json", AccountAccess.class);

        // Inject pisTppInfoValidator via setter
        getCardBalancesReportValidator.setAisAccountTppInfoValidator(aisAccountTppInfoValidator);
    }

    @Test
    void validate_withValidConsentObject_shouldReturnValid() {
        // Given
        AisConsent aisConsent = buildCardAccountConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getBalances(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS)).thenReturn(ValidationResult.valid());
        when(accountConsentValidator.validate(aisConsent, REQUEST_URI))
            .thenReturn(ValidationResult.valid());
        when(oauthConsentValidator.validate(aisConsent))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult = getCardBalancesReportValidator.validate(new GetCardAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isValid());
        assertNull(validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAccountReferenceAccess_error() {
        // Given
        AisConsent aisConsent = buildCardAccountConsent(TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());
        when(accountReferenceAccessValidator.validate(aisConsent, aisConsent.getAccess().getBalances(), ACCOUNT_ID, AisConsentRequestType.DEDICATED_ACCOUNTS))
            .thenReturn(ValidationResult.invalid(ErrorType.AIS_401, CONSENT_INVALID));

        // When
        ValidationResult validationResult = getCardBalancesReportValidator.validate(new GetCardAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertFalse(validationResult.isValid());

        verify(accountConsentValidator, never()).validate(any(AisConsent.class), anyString());
    }

    @Test
    void validate_withInvalidTppInConsent_shouldReturnTppValidationError() {
        // Given
        AisConsent aisConsent = buildCardAccountConsent(INVALID_TPP_INFO);
        when(aisAccountTppInfoValidator.validateTpp(INVALID_TPP_INFO))
            .thenReturn(ValidationResult.invalid(TPP_VALIDATION_ERROR));

        // When
        ValidationResult validationResult = getCardBalancesReportValidator.validate(new GetCardAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(TPP_VALIDATION_ERROR, validationResult.getMessageError());
    }

    @Test
    void validate_withInvalidAccessInConsent_shouldReturnConsentInvalidError() {
        // Given
        AisConsent aisConsent = buildCardAccountConsent(TPP_INFO);
        aisConsent.setTppAccountAccesses(accountAccess);
        aisConsent.setAspspAccountAccesses(accountAccess);

        when(aisAccountTppInfoValidator.validateTpp(TPP_INFO))
            .thenReturn(ValidationResult.valid());

        // When
        ValidationResult validationResult =
            getCardBalancesReportValidator.validate(new GetCardAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI));

        // Then
        verify(aisAccountTppInfoValidator).validateTpp(aisConsent.getTppInfo());

        assertNotNull(validationResult);
        assertTrue(validationResult.isNotValid());
        assertEquals(CONSENT_INVALID_ERROR, validationResult.getMessageError());
    }

    @Test
    void buildWarningMessages() {
        // Given
        AisConsent aisConsent = buildCardAccountConsent(TPP_INFO);
        GetCardAccountBalanceRequestObject getCardAccountBalanceRequestObject = new GetCardAccountBalanceRequestObject(aisConsent, ACCOUNT_ID, REQUEST_URI);

        //When
        Set<TppMessageInformation> actual =
            getCardBalancesReportValidator.buildWarningMessages(getCardAccountBalanceRequestObject);

        //Then
        assertThat(actual).isEmpty();
        verifyNoInteractions(accountConsentValidator);
        verifyNoInteractions(aisAccountTppInfoValidator);
        verifyNoInteractions(accountReferenceAccessValidator);
        verifyNoInteractions(oauthConsentValidator);
    }

    private static TppInfo buildTppInfo(String authorisationNumber) {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(authorisationNumber);
        return tppInfo;
    }

    private AisConsent buildCardAccountConsent(TppInfo tppInfo) {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/validator/ais/account/ais-consent-with-masked-pan.json", AisConsent.class);
        aisConsent.getConsentTppInformation().setTppInfo(tppInfo);
        aisConsent.setConsentData(AisConsentData.buildDefaultAisConsentData());

        return aisConsent;
    }
}
