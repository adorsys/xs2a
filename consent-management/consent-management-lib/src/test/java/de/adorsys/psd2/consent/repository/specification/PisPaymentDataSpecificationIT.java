/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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
