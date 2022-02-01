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

package de.adorsys.psd2.xs2a.service.authorization;

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.core.data.ais.AisConsentData;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentTppInformation;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.service.authorization.ais.AisScaAuthorisationService;
import de.adorsys.psd2.xs2a.service.profile.AspspProfileServiceWrapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AisScaAuthorisationServiceTest {

    private static final String CONSENT_ID = "03f0fccb-8462-43e8-9f7e-fce2887bcf38";

    @InjectMocks
    private AisScaAuthorisationService aisScaAuthorisationService;

    @Mock
    private AspspProfileServiceWrapper aspspProfileServiceWrapper;

    @Test
    void isOneFactorAuthorisation_AllAvailableConsent_OneAccessTypeTrue_ScaRequiredFalse() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired())
            .thenReturn(false);
        AisConsent consent = buildAvailableAccountConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertTrue(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_AllAvailableConsent_OneAccessTypeTrue_ScaRequiredTrue() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeAvailableAccountsConsentRequired())
            .thenReturn(true);
        AisConsent consent = buildAvailableAccountConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_AllAvailableConsent_OneAccessTypeFalse() {
        //Given
        AisConsent consent = buildAvailableAccountConsent(false);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_GlobalConsent_OneAccessTypeTrue_ScaRequiredFalse() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired())
            .thenReturn(false);
        AisConsent consent = buildGlobalConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertTrue(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_GlobalConsent_OneAccessTypeTrue_ScaRequiredTrue() {
        //Given
        when(aspspProfileServiceWrapper.isScaByOneTimeGlobalConsentRequired())
            .thenReturn(true);
        AisConsent consent = buildGlobalConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_GlobalConsent_OneAccessTypeFalse() {
        //Given
        AisConsent consent = buildGlobalConsent(false);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_BankOfferedConsent_OneAccessTypeTrue() {
        //Given
        AisConsent consent = buildBankOfferedConsent(true);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @Test
    void isOneFactorAuthorisation_BankOfferedConsent_OneAccessTypeFalse() {
        //Given
        AisConsent consent = buildBankOfferedConsent(false);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void isOneFactorAuthorisation_DedicatedConsent(boolean oneAccessType) {
        //Given
        AisConsent consent = buildDedicatedConsent(oneAccessType);

        //When
        boolean oneFactorAuthorisation = aisScaAuthorisationService.isOneFactorAuthorisation(consent);

        //Then
        assertFalse(oneFactorAuthorisation);
    }

    private AisConsent buildAvailableAccountConsent(boolean oneAccessType) {
        return buildConsent(oneAccessType, AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS);
    }

    private AisConsent buildGlobalConsent(boolean oneAccessType) {
        return buildConsent(oneAccessType, AisConsentRequestType.GLOBAL);
    }

    private AisConsent buildBankOfferedConsent(boolean oneAccessType) {
        return buildConsent(oneAccessType, AisConsentRequestType.BANK_OFFERED);
    }

    private AisConsent buildDedicatedConsent(boolean oneAccessType) {
        return buildConsent(oneAccessType, AisConsentRequestType.DEDICATED_ACCOUNTS);
    }

    private AisConsent buildConsent(boolean oneAccessType, AisConsentRequestType consentRequestType) {
        return createConsent(LocalDate.of(2019, 9, 19), OffsetDateTime.of(2019, 9, 19, 12, 0, 0, 0, ZoneOffset.UTC), oneAccessType, consentRequestType);
    }

    private AisConsent createConsent(LocalDate validUntil, OffsetDateTime statusChangeTimeStamp, boolean oneAccessType, AisConsentRequestType consentRequestType) {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setConsentData(buildAisConsentData(consentRequestType));
        aisConsent.setId(CONSENT_ID);
        aisConsent.setValidUntil(validUntil);
        aisConsent.setFrequencyPerDay(oneAccessType ? 1 : 2);
        aisConsent.setConsentStatus(ConsentStatus.VALID);
        aisConsent.setAuthorisations(Collections.emptyList());
        aisConsent.setConsentTppInformation(buildConsentTppInformation());
        aisConsent.setStatusChangeTimestamp(statusChangeTimeStamp);
        aisConsent.setUsages(Collections.emptyMap());
        aisConsent.setStatusChangeTimestamp(OffsetDateTime.now());
        aisConsent.setRecurringIndicator(!oneAccessType);

        return aisConsent;
    }

    private AisConsentData buildAisConsentData(AisConsentRequestType requestType) {
        if (AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS == requestType) {
            return new AisConsentData(AccountAccessType.ALL_ACCOUNTS, null, null, false);
        }

        if (AisConsentRequestType.GLOBAL == requestType) {
            return new AisConsentData(null, AccountAccessType.ALL_ACCOUNTS, null, false);
        }

        if (AisConsentRequestType.DEDICATED_ACCOUNTS == requestType) {
            return AisConsentData.buildDefaultAisConsentData();
        }

        if (AisConsentRequestType.BANK_OFFERED == requestType) {
            return AisConsentData.buildDefaultAisConsentData();
        }

        return null;
    }

    private ConsentTppInformation buildConsentTppInformation() {
        ConsentTppInformation consentTppInformation = new ConsentTppInformation();
        consentTppInformation.setTppInfo(new TppInfo());
        return consentTppInformation;
    }
}
