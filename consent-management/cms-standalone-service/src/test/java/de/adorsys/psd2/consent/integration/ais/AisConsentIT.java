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

package de.adorsys.psd2.consent.integration.ais;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsAisAccountConsent;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.service.ConsentService;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.integration.config.IntegrationTestConfiguration;
import de.adorsys.psd2.consent.psu.api.CmsPsuAisService;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class, MockHttpServletRequest.class})
@DataJpaTest
class AisConsentIT {
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final Integer DEFAULT_PAGE_INDEX = 0;
    private static final Integer DEFAULT_ITEMS_PER_PAGE = 20;

    @Autowired
    private ConsentService consentService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ConsentJpaRepository consentJpaRepository;
    @Autowired
    private CmsPsuAisService cmsPsuAisService;

    @MockBean
    private AspspProfileService aspspProfileService;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    public void setUp() {
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);
        when(aspspProfileService.getAspspSettings(DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(aspspSettings);
    }

    @Test
    void createAisConsent_successWithNewStatus() throws WrongChecksumException {
        // Given
        CmsConsent createAisConsentRequest = buildCreateAisConsentRequest();

        // When
        consentService.createConsent(createAisConsentRequest);
        flushAndClearPersistenceContext();
        Iterable<ConsentEntity> entities = consentJpaRepository.findAll();
        ConsentEntity savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertEquals(savedEntity.getStatusChangeTimestamp(), savedEntity.getCreationTimestamp());

        // When
        consentService.updateConsentStatusById(savedEntity.getExternalId(), ConsentStatus.EXPIRED);
        flushAndClearPersistenceContext();

        // Then
        // Second, we update the status and check it and the updated timestamp
        entities = consentJpaRepository.findAll();
        ConsentEntity updatedEntity = entities.iterator().next();
        assertEquals(ConsentStatus.EXPIRED, updatedEntity.getConsentStatus());
        assertTrue(updatedEntity.getStatusChangeTimestamp().isAfter(updatedEntity.getCreationTimestamp()));
    }

    @Test
    void createAisConsent_successWithTheSameStatus() throws WrongChecksumException {
        // Given
        CmsConsent createAisConsentRequest = buildCreateAisConsentRequest();

        // When
        consentService.createConsent(createAisConsentRequest);
        flushAndClearPersistenceContext();
        Iterable<ConsentEntity> entities = consentJpaRepository.findAll();
        ConsentEntity savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertEquals(savedEntity.getStatusChangeTimestamp(), savedEntity.getCreationTimestamp());

        // When
        consentService.updateConsentStatusById(savedEntity.getExternalId(), ConsentStatus.RECEIVED);
        flushAndClearPersistenceContext();

        // Then
        // Second, we update the status for the same and check it and the updated timestamp
        entities = consentJpaRepository.findAll();
        ConsentEntity updatedEntity = entities.iterator().next();
        assertEquals(ConsentStatus.RECEIVED, updatedEntity.getConsentStatus());
        assertEquals(updatedEntity.getStatusChangeTimestamp(), updatedEntity.getCreationTimestamp());
    }

    @Test
    void createAisConsent_failShouldThrowException() throws WrongChecksumException {
        // Given
        CmsConsent createAisConsentRequest = buildCreateAisConsentRequest();

        // When
        consentService.createConsent(createAisConsentRequest);
        flushAndClearPersistenceContext();
        Iterable<ConsentEntity> entities = consentJpaRepository.findAll();
        ConsentEntity savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertEquals(savedEntity.getStatusChangeTimestamp(), savedEntity.getCreationTimestamp());

        // When
        // New status is null
        consentService.updateConsentStatusById(savedEntity.getExternalId(), null);

        assertThrows(
            PersistenceException.class, this::flushAndClearPersistenceContext
        );
    }

    @Test
    void getConsentsForPsu_successWithDifferentPsu() throws WrongChecksumException {
        //Given
        PsuIdData aspsp = buildPsuIdData("aspsp", "aspsp corporate id");
        PsuIdData aspsp1 = buildPsuIdData("aspsp1", "aspsp1 corporate id");
        PsuIdData aspsp1NoCorporateId = buildPsuIdData("aspsp1", null);

        //When
        consentService.createConsent(buildCreateAisConsentRequestWithPsuData(aspsp));
        consentService.createConsent(buildCreateAisConsentRequestWithPsuData(aspsp));
        consentService.createConsent(buildCreateAisConsentRequestWithPsuData(aspsp1));
        consentService.createConsent(buildCreateAisConsentRequestWithPsuData(aspsp1NoCorporateId));
        flushAndClearPersistenceContext();

        //Then
        List<CmsAisAccountConsent> consentsAspsp = cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(aspsp, DEFAULT_SERVICE_INSTANCE_ID, null, null, null, DEFAULT_PAGE_INDEX, DEFAULT_ITEMS_PER_PAGE);
        assertEquals(2, consentsAspsp.size());
        assertEquals(aspsp, consentsAspsp.get(0).getPsuIdDataList().get(0));

        List<CmsAisAccountConsent> consentsAspsp1 = cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(aspsp1, DEFAULT_SERVICE_INSTANCE_ID, null, null, null, DEFAULT_PAGE_INDEX, DEFAULT_ITEMS_PER_PAGE);
        assertEquals(1, consentsAspsp1.size());
        assertEquals(aspsp1, consentsAspsp1.get(0).getPsuIdDataList().get(0));

        List<CmsAisAccountConsent> consentsAspsp1NoCorporateId = cmsPsuAisService.getConsentsForPsuAndAdditionalTppInfo(aspsp1NoCorporateId, DEFAULT_SERVICE_INSTANCE_ID, null, null, null, DEFAULT_PAGE_INDEX, DEFAULT_ITEMS_PER_PAGE);
        assertEquals(2, consentsAspsp1NoCorporateId.size());
        assertEquals("aspsp1", consentsAspsp1NoCorporateId.get(0).getPsuIdDataList().get(0).getPsuId());
        assertEquals("aspsp1", consentsAspsp1NoCorporateId.get(1).getPsuIdDataList().get(0).getPsuId());
    }

    private PsuIdData buildPsuIdData(String psuId, String psuCorporateId) {
        return new PsuIdData(psuId, null, psuCorporateId, null, null);
    }

    private CmsConsent buildCreateAisConsentRequestWithPsuData(PsuIdData psuIdData) {
        CmsConsent createAisConsentRequest = buildCreateAisConsentRequest();
        createAisConsentRequest.setPsuIdDataList(Collections.singletonList(psuIdData));
        return createAisConsentRequest;
    }

    private CmsConsent buildCreateAisConsentRequest() {
        CmsConsent cmsConsent = jsonReader.getObjectFromFile("json/consent/integration/ais/cms-consent.json", CmsConsent.class);
        cmsConsent.setAspspAccountAccesses(AccountAccess.EMPTY_ACCESS);
        cmsConsent.setTppAccountAccesses(AccountAccess.EMPTY_ACCESS);
        cmsConsent.setConsentData(jsonReader.getBytesFromFile("json/consent/integration/ais/ais-consent-data.json"));
        cmsConsent.setInstanceId(DEFAULT_SERVICE_INSTANCE_ID);
        return cmsConsent;
    }

    /**
     * Flush and clear the persistence context to force the call to the database
     */
    private void flushAndClearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}
