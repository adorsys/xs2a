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

package de.adorsys.psd2.consent.repository.specification;

import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.integration.test.BaseTest;
import de.adorsys.psd2.integration.test.TestDBConfiguration;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestDBConfiguration.class,
    initializers = {PisCommonPaymentDataSpecificationIT.Initializer.class})
class PisCommonPaymentDataSpecificationIT extends BaseTest {

    private static final String PAYMENT_ID = "329d04d9-cb42-4fb5-8265-de58fa00366d";
    private static final String INSTANCE_ID = "UNDEFINED";
    private static final String TPP_AUTHORISATION_NUMBER = "12345987";
    private static final String ASPSP_ACCOUNT_ID = "123-DEDE89370400440532013000-EUR";

    @Autowired
    private PisCommonPaymentDataSpecification pisCommonPaymentDataSpecification;

    @Autowired
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    private PisCommonPaymentData pisCommonPaymentData;
    private PsuIdData psuIdData;

    @BeforeEach
    void setUp() {
        clearData();

        psuIdData = jsonReader.getObjectFromFile("json/specification/psu-id-data.json", PsuIdData.class);

        pisCommonPaymentData = jsonReader.getObjectFromFile("json/specification/common-payment-data.json", PisCommonPaymentData.class);
        pisCommonPaymentData.getPayments().forEach(p -> p.setPaymentData(pisCommonPaymentData));
        pisCommonPaymentDataRepository.save(pisCommonPaymentData);
    }

    @Test
    void byPaymentId() {
        Optional<PisCommonPaymentData> actual = pisCommonPaymentDataRepository.findOne(
            pisCommonPaymentDataSpecification.byPaymentId(
                PAYMENT_ID
            ));

        assertTrue(actual.isPresent());
        assertEquals(PAYMENT_ID, actual.get().getPaymentId());
    }

    @Test
    void byPaymentIdAndInstanceId() {
        Optional<PisCommonPaymentData> actual = pisCommonPaymentDataRepository.findOne(
            pisCommonPaymentDataSpecification.byPaymentIdAndInstanceId(
                PAYMENT_ID,
                INSTANCE_ID
            ));

        assertTrue(actual.isPresent());
        assertEquals(PAYMENT_ID, actual.get().getPaymentId());
        assertEquals(INSTANCE_ID, actual.get().getInstanceId());
    }

    @Test
    @Transactional
    void byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId() {
        OffsetDateTime from = pisCommonPaymentData.getCreationTimestamp().minusDays(1);
        OffsetDateTime to = pisCommonPaymentData.getCreationTimestamp().plusMinutes(1);
        List<PisCommonPaymentData> actual = pisCommonPaymentDataRepository.findAll(
            pisCommonPaymentDataSpecification.byTppIdAndCreationPeriodAndPsuIdDataAndInstanceId(
                TPP_AUTHORISATION_NUMBER,
                from.toLocalDate(),
                to.toLocalDate(),
                psuIdData,
                INSTANCE_ID
            )
        );

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        PisCommonPaymentData actualPayment = actual.get(0);
        assertEquals(TPP_AUTHORISATION_NUMBER, actualPayment.getTppInfo().getAuthorisationNumber());
        assertTrue(actualPayment.getCreationTimestamp().isAfter(from));
        assertTrue(actualPayment.getCreationTimestamp().isBefore(to));
        assertEquals(psuIdData.getPsuId(), actualPayment.getPsuDataList().get(0).getPsuId());
        assertEquals(INSTANCE_ID, actualPayment.getInstanceId());
    }

    @Test
    @Transactional
    void byPsuIdDataAndCreationPeriodAndInstanceId() {
        OffsetDateTime from = pisCommonPaymentData.getCreationTimestamp().minusMinutes(1);
        OffsetDateTime to = pisCommonPaymentData.getCreationTimestamp().plusMinutes(1);
        List<PisCommonPaymentData> actual = pisCommonPaymentDataRepository.findAll(
            pisCommonPaymentDataSpecification.byPsuIdDataAndCreationPeriodAndInstanceId(
                psuIdData,
                from.toLocalDate(),
                to.toLocalDate(),
                INSTANCE_ID
            )
        );

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        PisCommonPaymentData actualPayment = actual.get(0);
        assertTrue(actualPayment.getCreationTimestamp().isAfter(from));
        assertTrue(actualPayment.getCreationTimestamp().isBefore(to));
        assertEquals(psuIdData.getPsuId(), actualPayment.getPsuDataList().get(0).getPsuId());
        assertEquals(INSTANCE_ID, actualPayment.getInstanceId());
    }

    @Test
    void byAspspAccountIdAndCreationPeriodAndInstanceId() {
        OffsetDateTime from = pisCommonPaymentData.getCreationTimestamp().minusMinutes(1);
        OffsetDateTime to = pisCommonPaymentData.getCreationTimestamp().plusMinutes(1);
        List<PisCommonPaymentData> actual = pisCommonPaymentDataRepository.findAll(
            pisCommonPaymentDataSpecification.byAspspAccountIdAndCreationPeriodAndInstanceId(
                ASPSP_ACCOUNT_ID,
                from.toLocalDate(),
                to.toLocalDate(),
                INSTANCE_ID
            )
        );

        assertTrue(CollectionUtils.isNotEmpty(actual));
        assertEquals(1, actual.size());
        PisCommonPaymentData actualPayment = actual.get(0);
        assertEquals(ASPSP_ACCOUNT_ID, actualPayment.getAspspAccountId());
        assertTrue(actualPayment.getCreationTimestamp().isAfter(from));
        assertTrue(actualPayment.getCreationTimestamp().isBefore(to));
        assertEquals(INSTANCE_ID, actualPayment.getInstanceId());
    }
}
