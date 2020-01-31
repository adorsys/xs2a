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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.consent.AccountConsent;
import de.adorsys.psd2.xs2a.domain.consent.Xs2aAccountAccess;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Currency;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisScaAuthorisationServiceTest {
    @InjectMocks
    private AisScaAuthorisationService aisScaAuthorisationService;
    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Test
    void isOneFactorAuthorisation_AllAvailableConsent_OneAccessTypeTrue_ScaRequiredFalse() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired()).thenReturn(false);
        AccountConsent consent = buildAvailableAccountConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertTrue(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_AllAvailableConsent_OneAccessTypeTrue_ScaRequiredTrue() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired()).thenReturn(true);
        AccountConsent consent = buildAvailableAccountConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_AllAvailableConsent_OneAccessTypeFalse() {
        //Given
        AccountConsent consent = buildAvailableAccountConsent(false);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_GlobalConsent_OneAccessTypeTrue_ScaRequiredFalse() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired()).thenReturn(false);
        AccountConsent consent = buildGlobalConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertTrue(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_GlobalConsent_OneAccessTypeTrue_ScaRequiredTrue() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired()).thenReturn(true);
        AccountConsent consent = buildGlobalConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_GlobalConsent_OneAccessTypeFalse() {
        //Given
        AccountConsent consent = buildGlobalConsent(false);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_BankOfferedConsent_OneAccessTypeTrue() {
        //Given
        AccountConsent consent = buildBankOfferedConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_BankOfferedConsent_OneAccessTypeFalse() {
        //Given
        AccountConsent consent = buildBankOfferedConsent(false);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }


    @Test
    void isOneFactorAuthorisation_DedicatedConsent_OneAccessTypeTrue_ScaRequiredTrue() {
        //Given
        AccountConsent consent = buildDedicatedConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_DedicatedConsent_OneAccessTypeFalse_ScaRequiredTrue() {
        //Given
        AccountConsent consent = buildDedicatedConsent(false);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_DedicatedConsent_OneAccessTypeTrue_ScaRequiredFalse() {
        //Given
        AccountConsent consent = buildDedicatedConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);
        //Then
        assertFalse(oneFactorAuthorisation);
    }

    private AccountConsent buildAvailableAccountConsent(boolean oneAccessType) {
        Xs2aAccountAccess accountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), AccountAccessType.ALL_ACCOUNTS, null, null, null);
        return buildConsent(accountAccess, oneAccessType, AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS);
    }

    private AccountConsent buildGlobalConsent(boolean oneAccessType) {
        Xs2aAccountAccess accountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, AccountAccessType.ALL_ACCOUNTS, null, null);
        return buildConsent(accountAccess, oneAccessType, AisConsentRequestType.GLOBAL);
    }

    private AccountConsent buildBankOfferedConsent(boolean oneAccessType) {
        Xs2aAccountAccess accountAccess = new Xs2aAccountAccess(Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), null, null, null, null);
        return buildConsent(accountAccess, oneAccessType, AisConsentRequestType.BANK_OFFERED);
    }

    private AccountConsent buildDedicatedConsent(boolean oneAccessType) {
        Xs2aAccountAccess accountAccess = new Xs2aAccountAccess(Collections.singletonList(new AccountReference(AccountReferenceType.IBAN, "DE86500105176716126648", Currency.getInstance("EUR"))), Collections.emptyList(), Collections.emptyList(), null, null, null, null);
        return buildConsent(accountAccess, oneAccessType, AisConsentRequestType.DEDICATED_ACCOUNTS);
    }

    private AccountConsent buildConsent(Xs2aAccountAccess accountAccess, boolean oneAccessType, AisConsentRequestType consentRequestType) {
        LocalDate date = LocalDate.of(2019, 9, 19);
        int frequencyPerDay = oneAccessType ? 1 : 2;
        OffsetDateTime offsetDateTime = OffsetDateTime.of(2019, 9, 19, 12, 0, 0, 0, ZoneOffset.UTC);

        return new AccountConsent("some id", accountAccess, accountAccess, !oneAccessType, date, null, frequencyPerDay, date, ConsentStatus.RECEIVED, false, false, Collections.emptyList(), new TppInfo(), consentRequestType, false, Collections.emptyList(), offsetDateTime, Collections.emptyMap(), OffsetDateTime.now());
    }
}
