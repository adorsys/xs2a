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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.core.payment.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.core.payment.model.PaymentInitiationJson;
import de.adorsys.psd2.core.payment.model.PeriodicPaymentInitiationJson;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CmsCorePaymentMapperTest {

    private CmsCorePaymentMapper mapper;
    private final JsonReader jsonReader = new JsonReader();
    private static final CmsAddressMapper cmsAddressMapper = new CmsAddressMapperImpl();

    @BeforeEach
    void setUp() {
        mapper = new CmsCorePaymentMapper(cmsAddressMapper);
    }

    @Test
    void mapToPaymentInitiationJson() {
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        PaymentInitiationJson actual = mapper.mapToPaymentInitiationJson(pisPayment);

        PaymentInitiationJson expected = jsonReader.getObjectFromFile("json/service/mapper/payment-initiation-resp.json", PaymentInitiationJson.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToPaymentInitiationJsonList_emptyValueValue() {
        assertNull(mapper.mapToPaymentInitiationJson(Collections.emptyList()));
    }

    @Test
    void mapToPaymentInitiationJsonList() {
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        PaymentInitiationJson actual = mapper.mapToPaymentInitiationJson(Arrays.asList(pisPayment, new PisPayment()));

        PaymentInitiationJson expected = jsonReader.getObjectFromFile("json/service/mapper/payment-initiation-resp.json", PaymentInitiationJson.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToBulkPaymentInitiationJson() {
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        BulkPaymentInitiationJson actual = mapper.mapToBulkPaymentInitiationJson(Collections.singletonList(pisPayment));

        BulkPaymentInitiationJson expected = jsonReader.getObjectFromFile("json/service/mapper/bulk-payment-initiation-resp.json", BulkPaymentInitiationJson.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToBulkPaymentInitiationJson_nullValue() {
        assertNull(mapper.mapToBulkPaymentInitiationJson(null));
    }

    @Test
    void mapToPeriodicPaymentInitiationJson() {
        PisPayment pisPayment = jsonReader.getObjectFromFile("json/service/mapper/pis-payment.json", PisPayment.class);
        PeriodicPaymentInitiationJson actual = mapper.mapToPeriodicPaymentInitiationJson(Collections.singletonList(pisPayment));

        PeriodicPaymentInitiationJson expected = jsonReader.getObjectFromFile("json/service/mapper/periodic-payment-initiation-resp.json", PeriodicPaymentInitiationJson.class);
        assertEquals(expected, actual);
    }

    @Test
    void mapToPeriodicPaymentInitiationJson_nullValue() {
        assertNull(mapper.mapToPeriodicPaymentInitiationJson(null));
    }
}
