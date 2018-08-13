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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.consent.api.AccountInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.aspsp.xs2a.consent.api.ais.AisAccountConsent;
import de.adorsys.aspsp.xs2a.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.aspsp.xs2a.domain.AisConsent;
import de.adorsys.aspsp.xs2a.repository.AisConsentRepository;
import de.adorsys.aspsp.xs2a.service.mapper.ConsentMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AisConsentServiceTest {
    @InjectMocks
    private AISConsentService aisConsentService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private ConsentMapper consentMapper;
    @Mock
    private AisConsentRepository aisConsentRepository;

    private AisConsent aisConsent;
    private final long CONSENT_ID = 1;
    private final String EXTERNAL_CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";

    @Before
    public void setUp() {
        aisConsent = buildConsent();
    }

    @Test
    public void shouldReturnAisConsent_whenGetConsentByIdIsCalled() {
        // When
        when(aisConsentRepository.findByExternalId(EXTERNAL_CONSENT_ID)).thenReturn(Optional.ofNullable(aisConsent));
        when(consentMapper.mapToAisAccountConsent(aisConsent)).thenReturn(buildSpiAccountConsent());

        // Then
        Optional<AisAccountConsent> retrievedConsent = aisConsentService.getAisAccountConsentById(EXTERNAL_CONSENT_ID);

        // Assert
        assertTrue(retrievedConsent.isPresent());
        assertThat(retrievedConsent.get().getId(), is(equalTo(aisConsent.getId().toString())));
    }

    @Test
    public void shouldReturnExternalId_WhenCreateConsentIsCalled() {
        // When
        when(aisConsentRepository.save(any(AisConsent.class))).thenReturn(aisConsent);
        when(aspspProfileService.getMinFrequencyPerDay(anyInt())).thenReturn(anyInt());

        // Then
        Optional<String> externalId = aisConsentService.createConsent(buildCorrectCreateAisConsentRequest());

        // Assert
        assertTrue(externalId.isPresent());
        assertThat(externalId.get(), is(equalTo(aisConsent.getExternalId())));
    }

    private AisConsent buildConsent() {
        AisConsent aisConsent = new AisConsent();
        aisConsent.setId(CONSENT_ID);
        aisConsent.setExternalId(EXTERNAL_CONSENT_ID);
        aisConsent.setExpireDate(LocalDate.now());
        return aisConsent;
    }

    private CreateAisConsentRequest buildCorrectCreateAisConsentRequest() {
        CreateAisConsentRequest request = new CreateAisConsentRequest();
        request.setAccess(buildAccess());
        request.setCombinedServiceIndicator(true);
        request.setFrequencyPerDay(5);
        request.setPsuId("psu-id-1");
        request.setRecurringIndicator(true);
        request.setTppId("tpp-id-1");
        request.setValidUntil(LocalDate.now());
        request.setTppRedirectPreferred(true);
        return request;
    }

    private AisAccountAccessInfo buildAccess() {
        AisAccountAccessInfo info = new AisAccountAccessInfo();
        info.setAccounts(buildAccountsInfo());
        return info;
    }

    private List<AccountInfo> buildAccountsInfo() {
        return Collections.singletonList(new AccountInfo("iban-1", "EUR"));
    }

    private AisAccountConsent buildSpiAccountConsent() {
        return new AisAccountConsent(aisConsent.getId().toString(),
            null, false,
            null, 0,
            null, null,
            false, false);
    }
}
