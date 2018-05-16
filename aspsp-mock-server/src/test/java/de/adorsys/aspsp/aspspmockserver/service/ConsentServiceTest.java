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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.repository.ConsentRepository;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.aspsp.xs2a.spi.domain.common.SpiTransactionStatus;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccess;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiConsentStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ConsentServiceTest {
    private static Date DATE = new Date(1122334455);
    private static final String CONSENT_ID = "777-888-999";
    private static final String WRONG_CONSENT_ID = "Wrong consent id";
    private static final String WRONG_ACCOUNT_ID = "Really wrong id";

    @Autowired
    private ConsentService consentService;
    @MockBean
    ConsentRepository consentRepository;

    @Before
    public void setUp() {

        when(consentRepository.findOne(CONSENT_ID))
            .thenReturn(getConsent(CONSENT_ID));
        when(consentRepository.findOne(WRONG_CONSENT_ID))
            .thenReturn(null);
        when(consentRepository.save(getConsent(CONSENT_ID)))
            .thenReturn(getConsent(CONSENT_ID));
        when(consentRepository.save(getConsent(WRONG_CONSENT_ID)))
            .thenReturn(null);
        doNothing().when(consentRepository).delete(CONSENT_ID);
        when(consentRepository.findAll())
            .thenReturn(Collections.singletonList(getConsent(CONSENT_ID)));
        when(consentRepository.exists(CONSENT_ID))
            .thenReturn(true);
        when(consentRepository.exists(WRONG_CONSENT_ID))
            .thenReturn(false);
    }

    @Test
    public void createConsentAndReturnId() {
        //When
        Optional<String> returnedId = consentService.createConsentAndReturnId(getConsent(CONSENT_ID));

        //Then
        assertThat(returnedId.get()).isEqualTo(CONSENT_ID);
    }

    @Test
    public void createConsentAndReturnId_Failure() {
        //When
        Optional<String> returnedId = consentService.createConsentAndReturnId(getConsent(WRONG_CONSENT_ID));

        //Then
        assertThat(returnedId).isEqualTo(Optional.empty());
    }

    @Test
    public void getConsent() {
        //When
        Optional<SpiAccountConsent> actualConsent = consentService.getConsent(CONSENT_ID);

        //Then
        assertThat(actualConsent.get()).isEqualTo(getConsent(CONSENT_ID));
    }

    @Test
    public void getConsent_Failure() {
        //When
        Optional<SpiAccountConsent> actualConsent = consentService.getConsent(WRONG_CONSENT_ID);

        //Then
        assertThat(actualConsent).isEqualTo(Optional.empty());
    }

    @Test
    public void getAllConsents() {
        //When
        List<SpiAccountConsent> actualConsentList = consentService.getAllConsents();

        //Then
        assertThat(actualConsentList).isEqualTo(Collections.singletonList(getConsent(CONSENT_ID)));
    }

    @Test
    public void deleteConsentById() {
        //When
        boolean isDeleted = consentService.deleteConsentById(CONSENT_ID);

        //Then
        assertThat(isDeleted).isEqualTo(true);
    }

    @Test
    public void deleteConsentById_Failure() {
        //When
        boolean isDeleted = consentService.deleteConsentById(WRONG_CONSENT_ID);

        //Then
        assertThat(isDeleted).isEqualTo(false);
    }

    private SpiAccountConsent getConsent(String id) {
        return new SpiAccountConsent(
            id, new SpiAccountAccess(), false, DATE, 4, null, SpiTransactionStatus.ACCP, SpiConsentStatus.VALID, true, true
        );
    }
}
