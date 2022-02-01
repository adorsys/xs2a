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

package de.adorsys.psd2.consent.integration.pis;

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentService;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.integration.config.IntegrationTestConfiguration;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.xs2a.core.pis.InternalPaymentStatus;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
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
class PisCommonPaymentIT {
    private static final String TPP_ID = "Test TppId";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final PaymentType PAYMENT_SERVICE = PaymentType.SINGLE;
    private static final String AUTHORITY_ID = "test authority ID";
    private static final PsuIdData PSU_DATA = new PsuIdData(null, null, null, null, null);
    private static final String PAYMENT_ID = "payment id";
    private static final String DEFAULT_SERVICE_INSTANCE_ID = "UNDEFINED";

    @Autowired
    private PisCommonPaymentService pisCommonPaymentService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @MockBean
    private AspspProfileService aspspProfileService;

    private final JsonReader jsonReader = new JsonReader();

    @BeforeEach
    public void setUp() {
        AspspSettings aspspSettings = jsonReader.getObjectFromFile("json/aspect/aspsp-settings.json", AspspSettings.class);

        when(aspspProfileService.getAspspSettings(DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(aspspSettings);
    }

    @Test
    void createPisCommonPayment_successWithNewStatus() {
        // Given
        PisPaymentInfo pisPaymentInfo = buildPisPaymentInfo();

        // When
        pisCommonPaymentService.createCommonPayment(pisPaymentInfo);
        flushAndClearPersistenceContext();
        Iterable<PisCommonPaymentData> entities = pisCommonPaymentDataRepository.findAll();
        PisCommonPaymentData savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertEquals(savedEntity.getStatusChangeTimestamp(), savedEntity.getCreationTimestamp());

        // When
        pisCommonPaymentService.updateCommonPaymentStatusById(savedEntity.getPaymentId(), TransactionStatus.RJCT);
        flushAndClearPersistenceContext();

        // Then
        // Second, we update the status and check it and the updated timestamp
        entities = pisCommonPaymentDataRepository.findAll();
        PisCommonPaymentData updatedEntity = entities.iterator().next();
        assertEquals(TransactionStatus.RJCT, updatedEntity.getTransactionStatus());
        assertTrue(updatedEntity.getStatusChangeTimestamp().isAfter(updatedEntity.getCreationTimestamp()));
    }

    @Test
    void createPisCommonPayment_successWithTheSameStatus() {
        // Given
        PisPaymentInfo pisPaymentInfo = buildPisPaymentInfo();

        // When
        pisCommonPaymentService.createCommonPayment(pisPaymentInfo);
        flushAndClearPersistenceContext();
        Iterable<PisCommonPaymentData> entities = pisCommonPaymentDataRepository.findAll();
        PisCommonPaymentData savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertEquals(savedEntity.getStatusChangeTimestamp(), savedEntity.getCreationTimestamp());

        // When
        pisCommonPaymentService.updateCommonPaymentStatusById(savedEntity.getPaymentId(), TransactionStatus.RCVD);
        flushAndClearPersistenceContext();

        // Then
        // Second, we update the status and check it and the updated timestamp
        entities = pisCommonPaymentDataRepository.findAll();
        PisCommonPaymentData updatedEntity = entities.iterator().next();
        assertEquals(TransactionStatus.RCVD, updatedEntity.getTransactionStatus());
        assertEquals(updatedEntity.getStatusChangeTimestamp(), updatedEntity.getCreationTimestamp());
    }

    @Test
    void createPisCommonPayment_failShouldThrowException() {
        // Given
        PisPaymentInfo pisPaymentInfo = buildPisPaymentInfo();

        // When
        pisCommonPaymentService.createCommonPayment(pisPaymentInfo);
        flushAndClearPersistenceContext();
        Iterable<PisCommonPaymentData> entities = pisCommonPaymentDataRepository.findAll();
        PisCommonPaymentData savedEntity = entities.iterator().next();

        // Then
        // First, we check that creation timestamp is equals to status change timestamp
        assertEquals(savedEntity.getStatusChangeTimestamp(), savedEntity.getCreationTimestamp());

        // When
        // New status is null
        pisCommonPaymentService.updateCommonPaymentStatusById(savedEntity.getPaymentId(), null);

        assertThrows(
            PersistenceException.class, this::flushAndClearPersistenceContext
        );
    }

    private PisPaymentInfo buildPisPaymentInfo() {
        PisPaymentInfo pisPaymentInfo = new PisPaymentInfo();
        pisPaymentInfo.setPaymentProduct(PAYMENT_PRODUCT);
        pisPaymentInfo.setPaymentType(PAYMENT_SERVICE);
        pisPaymentInfo.setTppInfo(buildTppInfo());
        pisPaymentInfo.setPsuDataList(buildPsuIdDataList());
        pisPaymentInfo.setPaymentId(PAYMENT_ID);
        pisPaymentInfo.setTransactionStatus(TransactionStatus.RCVD);
        pisPaymentInfo.setInternalPaymentStatus(InternalPaymentStatus.INITIATED);
        pisPaymentInfo.setInstanceId(DEFAULT_SERVICE_INSTANCE_ID);
        return pisPaymentInfo;
    }

    private List<PsuIdData> buildPsuIdDataList() {
        return Collections.singletonList(PSU_DATA);
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber(TPP_ID);
        tppInfo.setAuthorityId(AUTHORITY_ID);
        return tppInfo;
    }

    /**
     * Flush and clear the persistence context to force the call to the database
     */
    private void flushAndClearPersistenceContext() {
        entityManager.flush();
        entityManager.clear();
    }
}
