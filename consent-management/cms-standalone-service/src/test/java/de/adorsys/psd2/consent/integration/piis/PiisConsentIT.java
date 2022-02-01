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


package de.adorsys.psd2.consent.integration.piis;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.api.service.PiisConsentService;
import de.adorsys.psd2.consent.aspsp.api.PageData;
import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisService;
import de.adorsys.psd2.consent.aspsp.api.piis.CreatePiisConsentRequest;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.integration.config.IntegrationTestConfiguration;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceSelector;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("integration-test")
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {IntegrationTestConfiguration.class, MockHttpServletRequest.class})
@DataJpaTest
class PiisConsentIT {
    private static final String PSU_ID = "ID";
    private static final String PSU_ID_TYPE = "TYPE";
    private static final String PSU_CORPORATE_ID = "CORPORATE_ID";
    private static final String PSU_CORPORATE_ID_TYPE = "CORPORATE_ID_TYPE";
    private static final String PSU_IP_ADDRESS = "IP_ADDRESS";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";
    private static final String ASPSP_ACCOUNT_ID = "3278921mxl-n2131-13nw";
    private static final String ACCOUNT_ID = UUID.randomUUID().toString();
    private static final String IBAN = "Test IBAN";
    private static final String BBAN = "Test BBAN";
    private static final String PAN = "Test PAN";
    private static final String MASKED_PAN = "Test MASKED_PAN";
    private static final String MSISDN = "Test MSISDN";
    private static final Currency EUR_CURRENCY = Currency.getInstance("EUR");
    private static final String TPP_AUTHORISATION_NUMBER = "authorisation number";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("psu", null, "corpId", null, null);
    private static final Integer PAGE_INDEX = 0;
    private static final Integer ITEMS_PER_PAGE = 20;

    @Autowired
    private CmsAspspPiisService cmsAspspPiisServiceInternal;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private ConsentJpaRepository consentJpaRepository;
    @Autowired
    private PiisConsentService piisConsentService;

    @Test
    void createPiisConsent_successWithNewStatus() {
        // When
        cmsAspspPiisServiceInternal.createConsent(buildPsuIdData(), buildCreatePiisConsentRequest(), DEFAULT_SERVICE_INSTANCE_ID);
        flushAndClearPersistenceContext();
        Iterable<ConsentEntity> entities = consentJpaRepository.findAll();
        ConsentEntity savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertEquals(savedEntity.getStatusChangeTimestamp(), savedEntity.getCreationTimestamp());

        // When
        cmsAspspPiisServiceInternal.terminateConsent(savedEntity.getExternalId(), DEFAULT_SERVICE_INSTANCE_ID);
        flushAndClearPersistenceContext();

        // Then
        // Second, we update the status and check it and the updated timestamp
        entities = consentJpaRepository.findAll();
        ConsentEntity updatedEntity = entities.iterator().next();
        assertEquals(ConsentStatus.TERMINATED_BY_ASPSP, updatedEntity.getConsentStatus());
        assertTrue(updatedEntity.getStatusChangeTimestamp().isAfter(updatedEntity.getCreationTimestamp()));
    }

    @Test
    void getConsentsForPsu_successWithDifferentPsu() {
        //Given
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest();
        PsuIdData aspsp = buildPsuIdData("aspsp", "aspsp corporate id");
        PsuIdData aspsp1 = buildPsuIdData("aspsp1", "aspsp1 corporate id");
        PsuIdData aspsp1NoCorporateId = buildPsuIdData("aspsp1", null);

        //When
        cmsAspspPiisServiceInternal.createConsent(aspsp, request, DEFAULT_SERVICE_INSTANCE_ID);
        cmsAspspPiisServiceInternal.createConsent(aspsp1, request, DEFAULT_SERVICE_INSTANCE_ID);
        cmsAspspPiisServiceInternal.createConsent(aspsp1NoCorporateId, request, DEFAULT_SERVICE_INSTANCE_ID);
        flushAndClearPersistenceContext();

        //Then
        PageData<List<CmsPiisConsent>> consentsAspsp = cmsAspspPiisServiceInternal.getConsentsForPsu(aspsp, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
        assertEquals(1, consentsAspsp.getData().size());
        assertEquals(aspsp, consentsAspsp.getData().get(0).getPsuData());

        PageData<List<CmsPiisConsent>> consentsAspsp1 = cmsAspspPiisServiceInternal.getConsentsForPsu(aspsp1, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
        assertEquals(1, consentsAspsp1.getData().size());
        assertEquals(aspsp1, consentsAspsp1.getData().get(0).getPsuData());

        PageData<List<CmsPiisConsent>> consentsAspsp1NoCorporateId = cmsAspspPiisServiceInternal.getConsentsForPsu(aspsp1NoCorporateId, DEFAULT_SERVICE_INSTANCE_ID, PAGE_INDEX, ITEMS_PER_PAGE);
        assertEquals(2, consentsAspsp1NoCorporateId.getData().size());
        assertEquals("aspsp1", consentsAspsp1NoCorporateId.getData().get(0).getPsuData().getPsuId());
        assertEquals("aspsp1", consentsAspsp1NoCorporateId.getData().get(1).getPsuData().getPsuId());
    }

    @Test
    void getPiisConsentListByAccountIdentifier_Success() {
        // Given
        Set<AccountReferenceSelector> selectors = new HashSet<>();
        selectors.add(createConsentAndGetSelector(AccountReferenceType.IBAN, "DE2310010010123456789"));
        selectors.add(createConsentAndGetSelector(AccountReferenceType.BBAN, "DE2310010010123452343"));
        selectors.add(createConsentAndGetSelector(AccountReferenceType.PAN, "1111222233334444"));
        selectors.add(createConsentAndGetSelector(AccountReferenceType.MASKED_PAN, "111122xxxxxx4444"));
        selectors.add(createConsentAndGetSelector(AccountReferenceType.MSISDN, "4905123123"));

        selectors.forEach(selector -> {
            // When
            CmsResponse<List<CmsConsent>> cmsResponse = piisConsentService.getPiisConsentListByAccountIdentifier(EUR_CURRENCY, selector);
            // Then
            List<CmsConsent> payload = cmsResponse.getPayload();
            assertEquals(1, payload.size());
            AccountAccess aspspAccountAccesses = payload.get(0).getAspspAccountAccesses();
            AccountReference account = aspspAccountAccesses.getAccounts().get(0);
            assertNotNull(account);
            assertEquals(selector, account.getUsedAccountReferenceSelector());
        });
    }

    private AccountReferenceSelector createConsentAndGetSelector(AccountReferenceType accountReferenceType, String accountReferenceValue) {
        AccountReference accountReference = new AccountReference(accountReferenceType, accountReferenceValue, EUR_CURRENCY);
        CreatePiisConsentRequest request = buildCreatePiisConsentRequest(accountReference);
        cmsAspspPiisServiceInternal.createConsent(PSU_ID_DATA, request, DEFAULT_SERVICE_INSTANCE_ID);

        return new AccountReferenceSelector(accountReferenceType, accountReferenceValue);
    }

    @NotNull
    private CreatePiisConsentRequest buildCreatePiisConsentRequest() {
        return buildCreatePiisConsentRequest(buildAccountReference());
    }

    @NotNull
    private CreatePiisConsentRequest buildCreatePiisConsentRequest(AccountReference accountReference) {
        CreatePiisConsentRequest request = new CreatePiisConsentRequest();
        request.setTppAuthorisationNumber(TPP_AUTHORISATION_NUMBER);
        request.setAccount(accountReference);
        request.setValidUntil(LocalDate.now().plusDays(1));
        return request;
    }

    private PsuIdData buildPsuIdData() {
        return new PsuIdData(PSU_ID, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
    }

    private PsuIdData buildPsuIdData(String psuId, String psuCorporateId) {
        return new PsuIdData(psuId, null, psuCorporateId, null, null);
    }

    private AccountReference buildAccountReference() {
        return new AccountReference(ASPSP_ACCOUNT_ID, ACCOUNT_ID, IBAN, BBAN, PAN, MASKED_PAN, MSISDN, EUR_CURRENCY, null);
    }

    /**
     * Flush and clear the persistence context to force the call to the database
     */
    private void flushAndClearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}
