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

package de.adorsys.psd2.consent.integration.ais;

import de.adorsys.psd2.consent.api.ais.AisAccountAccessInfo;
import de.adorsys.psd2.consent.api.ais.AisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CreateAisConsentRequest;
import de.adorsys.psd2.consent.api.service.AisConsentService;
import de.adorsys.psd2.consent.domain.account.AisConsent;
import de.adorsys.psd2.consent.integration.config.IntegrationTestConfiguration;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.repository.AisConsentRepository;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("integration-test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = IntegrationTestConfiguration.class)
@DataJpaTest
public class AisConsentIT {
    private static final String TPP_ID = "Test TppId";
    private static final int FREQUENCY_PER_DAY = 5;
    private static final LocalDate VALID_UNTIL = LocalDate.now().plusDays(1);
    private static final String AUTHORITY_ID = "test authority ID";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    @Autowired
    private AisConsentService aisConsentService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private AisConsentRepository aisConsentRepository;
    @Autowired
    private CmsPsuAisService cmsPsuAisService;

    @Test
    public void createAisConsent_successWithNewStatus() {
        // Given
        CreateAisConsentRequest createAisConsentRequest = buildCreateAisConsentRequest();

        // When
        aisConsentService.createConsent(createAisConsentRequest);
        flushAndClearPersistenceContext();
        Iterable<AisConsent> entities = aisConsentRepository.findAll();
        AisConsent savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertTrue(savedEntity.getStatusChangeTimestamp().equals(savedEntity.getCreationTimestamp()));

        // When
        aisConsentService.updateConsentStatusById(savedEntity.getExternalId(), ConsentStatus.EXPIRED);
        flushAndClearPersistenceContext();

        // Then
        // Second, we update the status and check it and the updated timestamp
        entities = aisConsentRepository.findAll();
        AisConsent updatedEntity = entities.iterator().next();
        assertEquals(ConsentStatus.EXPIRED, updatedEntity.getConsentStatus());
        assertTrue(updatedEntity.getStatusChangeTimestamp().isAfter(updatedEntity.getCreationTimestamp()));
    }

    @Test
    public void createAisConsent_successWithTheSameStatus() {
        // Given
        CreateAisConsentRequest createAisConsentRequest = buildCreateAisConsentRequest();

        // When
        aisConsentService.createConsent(createAisConsentRequest);
        flushAndClearPersistenceContext();
        Iterable<AisConsent> entities = aisConsentRepository.findAll();
        AisConsent savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertTrue(savedEntity.getStatusChangeTimestamp().equals(savedEntity.getCreationTimestamp()));

        // When
        aisConsentService.updateConsentStatusById(savedEntity.getExternalId(), ConsentStatus.RECEIVED);
        flushAndClearPersistenceContext();

        // Then
        // Second, we update the status for the same and check it and the updated timestamp
        entities = aisConsentRepository.findAll();
        AisConsent updatedEntity = entities.iterator().next();
        assertEquals(ConsentStatus.RECEIVED, updatedEntity.getConsentStatus());
        assertTrue(updatedEntity.getStatusChangeTimestamp().equals(updatedEntity.getCreationTimestamp()));
    }

    @Test(expected = PersistenceException.class)
    public void createAisConsent_failShouldThrowException() {
        // Given
        CreateAisConsentRequest createAisConsentRequest = buildCreateAisConsentRequest();

        // When
        aisConsentService.createConsent(createAisConsentRequest);
        flushAndClearPersistenceContext();
        Iterable<AisConsent> entities = aisConsentRepository.findAll();
        AisConsent savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertTrue(savedEntity.getStatusChangeTimestamp().equals(savedEntity.getCreationTimestamp()));

        // When
        // New status is null
        aisConsentService.updateConsentStatusById(savedEntity.getExternalId(), null);

        // Then
        // Here the exception should be thrown
        flushAndClearPersistenceContext();
    }

    @Test
    public void getConsentsForPsu_successWithDifferentPsu() {
        //Given
        PsuIdData aspsp = buildPsuIdData("aspsp", "aspsp corporate id");
        PsuIdData aspsp1 = buildPsuIdData("aspsp1", "aspsp1 corporate id");
        PsuIdData aspsp1NoCorporateId = buildPsuIdData("aspsp1", null);

        //When
        aisConsentService.createConsent(buildCreateAisConsentRequestWithPsuData(aspsp));
        aisConsentService.createConsent(buildCreateAisConsentRequestWithPsuData(aspsp));
        aisConsentService.createConsent(buildCreateAisConsentRequestWithPsuData(aspsp1));
        aisConsentService.createConsent(buildCreateAisConsentRequestWithPsuData(aspsp1NoCorporateId));
        flushAndClearPersistenceContext();

        //Then
        List<AisAccountConsent> consentsAspsp = cmsPsuAisService.getConsentsForPsu(aspsp, DEFAULT_SERVICE_INSTANCE_ID);
        assertEquals(2, consentsAspsp.size());
        assertEquals(aspsp, consentsAspsp.get(0).getPsuIdDataList().get(0));

        List<AisAccountConsent> consentsAspsp1 = cmsPsuAisService.getConsentsForPsu(aspsp1, DEFAULT_SERVICE_INSTANCE_ID);
        assertEquals(1, consentsAspsp1.size());
        assertEquals(aspsp1, consentsAspsp1.get(0).getPsuIdDataList().get(0));

        List<AisAccountConsent> consentsAspsp1NoCorporateId = cmsPsuAisService.getConsentsForPsu(aspsp1NoCorporateId, DEFAULT_SERVICE_INSTANCE_ID);
        assertEquals(2, consentsAspsp1NoCorporateId.size());
        assertEquals("aspsp1", consentsAspsp1NoCorporateId.get(0).getPsuIdDataList().get(0).getPsuId());
        assertEquals("aspsp1", consentsAspsp1NoCorporateId.get(1).getPsuIdDataList().get(0).getPsuId());
    }

    private PsuIdData buildPsuIdData(String psuId, String psuCorporateId) {
        return new PsuIdData(psuId, null, psuCorporateId, null);
    }

    private CreateAisConsentRequest buildCreateAisConsentRequestWithPsuData(PsuIdData psuIdData) {
        CreateAisConsentRequest createAisConsentRequest = buildCreateAisConsentRequest();
        createAisConsentRequest.setPsuData(psuIdData);
        return createAisConsentRequest;
    }

    private CreateAisConsentRequest buildCreateAisConsentRequest() {
        CreateAisConsentRequest createAisConsentRequest = new CreateAisConsentRequest();
        createAisConsentRequest.setAllowedFrequencyPerDay(FREQUENCY_PER_DAY);
        createAisConsentRequest.setAccess(buildAisAccountAccessInfo());
        createAisConsentRequest.setRecurringIndicator(false);
        createAisConsentRequest.setValidUntil(VALID_UNTIL);
        createAisConsentRequest.setTppInfo(buildTppInfo());
        return createAisConsentRequest;
    }

    private AisAccountAccessInfo buildAisAccountAccessInfo() {
        return new AisAccountAccessInfo();
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_ID);
        tppInfo.setAuthorityId(AUTHORITY_ID);
        tppInfo.setTppRedirectUri(buildTppRedirectUri());
        return tppInfo;
    }

    private TppRedirectUri buildTppRedirectUri() {
        return new TppRedirectUri("redirectUri", "nokRedirectUri");
    }

    /**
     * Flush and clear the persistence context to force the call to the database
     */
    private void flushAndClearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}
