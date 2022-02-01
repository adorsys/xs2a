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
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.consent.repository.PisCommonPaymentDataRepository;
import de.adorsys.psd2.consent.repository.PisPaymentDataRepository;
import de.adorsys.psd2.integration.test.BaseTest;
import de.adorsys.psd2.integration.test.TestDBConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ContextConfiguration(classes = TestDBConfiguration.class,
    initializers = {PisPaymentDataSpecificationIT.Initializer.class})
class PisPaymentDataSpecificationIT extends BaseTest {

    private static final String PAYMENT_ID = "cea9dda3-5154-420d-b1a7-6b4798fccb4b";
    private static final String INSTANCE_ID = "UNDEFINED";

    @Autowired
    private PisPaymentDataSpecification pisPaymentDataSpecification;

    @Autowired
    private PisCommonPaymentDataRepository pisCommonPaymentDataRepository;
    @Autowired
    private PisPaymentDataRepository pisPaymentDataRepository;

    @BeforeEach
    void setUp() {
        clearData();

        PisCommonPaymentData pisCommonPaymentData = jsonReader.getObjectFromFile("json/specification/common-payment-data.json", PisCommonPaymentData.class);
        pisCommonPaymentData.getPayments().forEach(p -> p.setPaymentData(pisCommonPaymentData));
        pisCommonPaymentDataRepository.save(pisCommonPaymentData);
    }

    @Test
    void byPaymentIdAndInstanceId() {
        Optional<PisPaymentData> actual = pisPaymentDataRepository.findOne(
            pisPaymentDataSpecification.byPaymentIdAndInstanceId(
                PAYMENT_ID,
                INSTANCE_ID
            ));

        assertTrue(actual.isPresent());
        assertEquals(PAYMENT_ID, actual.get().getPaymentId());
        assertEquals(INSTANCE_ID, actual.get().getInstanceId());
    }
}
