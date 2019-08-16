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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.reader.JsonReader;
import de.adorsys.psd2.xs2a.core.ais.AccountAccessType;
import de.adorsys.psd2.xs2a.core.consent.AisConsentRequestType;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class AisConsentRequestTypeServiceTest {
    private AisConsentRequestTypeService aisConsentRequestTypeService;
    private JsonReader jsonReader = new JsonReader();
    private AisConsent aisConsent;
    private AisAccountAccessInfo accessInfo;

    @Before
    public void setUp() {
        aisConsentRequestTypeService = new AisConsentRequestTypeService();
        aisConsent = jsonReader.getObjectFromFile("json/ais-consent.json", AisConsent.class);
        accessInfo = jsonReader.getObjectFromFile("json/access-info.json", AisAccountAccessInfo.class);
    }

    @Test
    public void getRequestTypeFromConsent_DEDICATED_ACCOUNTS() {
        // Given
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromConsent(aisConsent);

        // Then
        assertEquals(AisConsentRequestType.DEDICATED_ACCOUNTS, aisConsentRequestType);
    }

    @Test
    public void getRequestTypeFromConsent_GLOBAL() {
        // Given
        aisConsent.setAspspAccountAccesses(Collections.emptyList());
        aisConsent.setAllPsd2(AccountAccessType.ALL_ACCOUNTS);
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromConsent(aisConsent);

        // Then
        assertEquals(AisConsentRequestType.GLOBAL, aisConsentRequestType);
    }

    @Test
    public void getRequestTypeFromConsent_BANK_OFFERED() {
        // Given
        aisConsent.setAspspAccountAccesses(Collections.emptyList());
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromConsent(aisConsent);

        // Then
        assertEquals(AisConsentRequestType.BANK_OFFERED, aisConsentRequestType);
    }

    @Test
    public void getRequestTypeFromConsent_ALL_AVAILABLE_ACCOUNTS() {
        // Given
        aisConsent.setAspspAccountAccesses(Collections.emptyList());
        aisConsent.setAvailableAccounts(AccountAccessType.ALL_ACCOUNTS);
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromConsent(aisConsent);

        // Then
        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentRequestType);
    }

    @Test
    public void determineAisConsentRequestTypeByAisConsent_ALL_AVAILABLE_ACCOUNTS_WITH_BALANCES() {
        // Given
        aisConsent.setAspspAccountAccesses(Collections.emptyList());
        aisConsent.setAvailableAccountsWithBalance(AccountAccessType.ALL_ACCOUNTS);
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromConsent(aisConsent);

        // Then
        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentRequestType);
    }

    @Test
    public void getRequestTypeFromAccess_DEDICATED_ACCOUNTS() {
        // Given
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromAccess(accessInfo);

        // Then
        assertEquals(AisConsentRequestType.DEDICATED_ACCOUNTS, aisConsentRequestType);
    }

    @Test
    public void getRequestTypeFromAccess_GLOBAL() {
        // Given
        accessInfo.setAccounts(Collections.emptyList());
        accessInfo.setAllPsd2(AccountAccessType.ALL_ACCOUNTS);
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromAccess(accessInfo);

        // Then
        assertEquals(AisConsentRequestType.GLOBAL, aisConsentRequestType);
    }

    @Test
    public void getRequestTypeFromAccess_BANK_OFFERED() {
        // Given
        accessInfo.setAccounts(Collections.emptyList());
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromAccess(accessInfo);

        // Then
        assertEquals(AisConsentRequestType.BANK_OFFERED, aisConsentRequestType);
    }

    @Test
    public void getRequestTypeFromAccess_ALL_AVAILABLE_ACCOUNTS() {
        // Given
        accessInfo.setAccounts(Collections.emptyList());
        accessInfo.setAvailableAccounts(AccountAccessType.ALL_ACCOUNTS);
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromAccess(accessInfo);

        // Then
        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentRequestType);
    }

    @Test
    public void getRequestTypeFromAccess_ALL_AVAILABLE_ACCOUNTS_WITH_BALANCES() {
        // Given
        accessInfo.setAccounts(Collections.emptyList());
        accessInfo.setAvailableAccountsWithBalance(AccountAccessType.ALL_ACCOUNTS);
        // When
        AisConsentRequestType aisConsentRequestType = aisConsentRequestTypeService.getRequestTypeFromAccess(accessInfo);

        // Then
        assertEquals(AisConsentRequestType.ALL_AVAILABLE_ACCOUNTS, aisConsentRequestType);
    }
}
