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

package piis;

import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.piis.PiisConsentEntity;
import de.adorsys.psd2.consent.integration.config.IntegrationTestConfiguration;
import de.adorsys.psd2.consent.repository.PiisConsentRepository;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("integration-test")
@RunWith(SpringRunner.class)
@SpringBootTest(classes = IntegrationTestConfiguration.class)
@DataJpaTest
public class PiisConsentIT {
    private static final String PSU_ID = "ID";
    private static final String PSU_ID_TYPE = "TYPE";
    private static final String PSU_CORPORATE_ID = "CORPORATE_ID";
    private static final String PSU_CORPORATE_ID_TYPE = "CORPORATE_ID_TYPE";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String ACCOUNT_ID = UUID.randomUUID().toString();
    private static final String IBAN = "Test IBAN";
    private static final String BBAN = "Test BBAN";
    private static final String PAN = "Test PAN";
    private static final String MASKED_PAN = "Test MASKED_PAN";
    private static final String MSISDN = "Test MSISDN";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");

    @Autowired
    private CmsAspspPiisService cmsAspspPiisServiceInternal;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PiisConsentRepository piisConsentRepository;

    @Test
    public void createPiisConsent_successWithNewStatus() {
        // When
        cmsAspspPiisServiceInternal.createConsent(buildPsuIdData(), null, buildAccountReferenceList(), LocalDate.now().plusDays(1), 1);
        flushAndClearPersistenceContext();
        Iterable<PiisConsentEntity> entities = piisConsentRepository.findAll();
        PiisConsentEntity savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertTrue(savedEntity.getStatusChangeTimestamp().equals(savedEntity.getCreationTimestamp()));

        // When
        cmsAspspPiisServiceInternal.terminateConsent(savedEntity.getExternalId(), DEFAULT_SERVICE_INSTANCE_ID);
        flushAndClearPersistenceContext();

        // Then
        // Second, we update the status and check it and the updated timestamp
        entities = piisConsentRepository.findAll();
        PiisConsentEntity updatedEntity = entities.iterator().next();
        assertEquals(ConsentStatus.TERMINATED_BY_ASPSP, updatedEntity.getConsentStatus());
        assertTrue(updatedEntity.getStatusChangeTimestamp().isAfter(updatedEntity.getCreationTimestamp()));
    }

    @Test
    public void getConsentsForPsu_successWithDifferentPsu() {
        //Given
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest();
        PsuIdData aspsp = buildPsuIdData("aspsp", "aspsp corporate id");
        PsuIdData aspsp1 = buildPsuIdData("aspsp1", "aspsp1 corporate id");
        PsuIdData aspsp1NoCorporateId = buildPsuIdData("aspsp1", null);

        //When
        cmsAspspPiisServiceInternal.createConsent(aspsp, request);
        cmsAspspPiisServiceInternal.createConsent(aspsp, request);
        cmsAspspPiisServiceInternal.createConsent(aspsp1, request);
        cmsAspspPiisServiceInternal.createConsent(aspsp1NoCorporateId, request);
        flushAndClearPersistenceContext();

        //Then
        List<PiisConsent> consentsAspsp = cmsAspspPiisServiceInternal.getConsentsForPsu(aspsp, DEFAULT_SERVICE_INSTANCE_ID);
        assertEquals(2, consentsAspsp.size());
        assertEquals(aspsp, consentsAspsp.get(0).getPsuData());

        List<PiisConsent> consentsAspsp1 = cmsAspspPiisServiceInternal.getConsentsForPsu(aspsp1, DEFAULT_SERVICE_INSTANCE_ID);
        assertEquals(1, consentsAspsp1.size());
        assertEquals(aspsp1, consentsAspsp1.get(0).getPsuData());

        List<PiisConsent> consentsAspsp1NoCorporateId = cmsAspspPiisServiceInternal.getConsentsForPsu(aspsp1NoCorporateId, DEFAULT_SERVICE_INSTANCE_ID);
        assertEquals(2, consentsAspsp1NoCorporateId.size());
        assertEquals("aspsp1", consentsAspsp1NoCorporateId.get(0).getPsuData().getPsuId());
        assertEquals("aspsp1", consentsAspsp1NoCorporateId.get(1).getPsuData().getPsuId());
    }

    @NotNull
    private CreatePiisConsentRequest buildCreatePiisConsentRequest() {
        CreatePiisConsentRequest request = new CreatePiisConsentRequest();
        request.setAccounts(buildAccountReferenceList());
        request.setValidUntil(LocalDate.now().plusDays(1));
        request.setAllowedFrequencyPerDay(1);
        return request;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
    }

    private PsuIdData buildPsuIdData(String psuId, String psuCorporateId) {
        return new PsuIdData(psuId, null, psuCorporateId, null);
    }

    private AccountReference buildAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY);
    }

    private List<AccountReference> buildAccountReferenceList() {
        return Collections.singletonList(buildAccountReference());
    }

    /**
     * Flush and clear the persistence context to force the call to the database
     */
    private void flushAndClearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}
