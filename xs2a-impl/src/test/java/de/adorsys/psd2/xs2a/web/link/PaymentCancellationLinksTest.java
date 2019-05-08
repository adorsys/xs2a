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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.Links;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static de.adorsys.psd2.xs2a.core.pis.TransactionStatus.*;
import static org.junit.Assert.assertEquals;

public class PaymentCancellationLinksTest {

    private static final String HTTP_URL = "http://url";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "1111111111111";

    private PaymentCancellationLinks links;
    private Links expectedLinks;

    @Before
    public void setUp() {
        expectedLinks = new Links();
    }

    @Test
    public void isStartAuthorisationLinksNeeded_singlePayment() {
        links = new PaymentCancellationLinks(HTTP_URL, PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, true, TransactionStatus.PDNG);

        expectedLinks.setSelf("http://url/v1/payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisation("http://url/v1/payments/sepa-credit-transfers/1111111111111/cancellation-authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isStartAuthorisationLinksNeeded_bulkPayment() {
        links = new PaymentCancellationLinks(HTTP_URL, PaymentType.BULK, PAYMENT_PRODUCT, PAYMENT_ID, true, TransactionStatus.PDNG);

        expectedLinks.setSelf("http://url/v1/bulk-payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/bulk-payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisation("http://url/v1/bulk-payments/sepa-credit-transfers/1111111111111/cancellation-authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isStartAuthorisationLinksNeeded_periodicPayment() {
        links = new PaymentCancellationLinks(HTTP_URL, PaymentType.PERIODIC, PAYMENT_PRODUCT, PAYMENT_ID, true, TransactionStatus.PDNG);

        expectedLinks.setSelf("http://url/v1/periodic-payments/sepa-credit-transfers/1111111111111");
        expectedLinks.setStatus("http://url/v1/periodic-payments/sepa-credit-transfers/1111111111111/status");
        expectedLinks.setStartAuthorisation("http://url/v1/periodic-payments/sepa-credit-transfers/1111111111111/cancellation-authorisations");
        assertEquals(expectedLinks, links);
    }

    @Test
    public void isStartAuthorisationLinksNotNeeded() {
        //isScaRequired = false
        links = new PaymentCancellationLinks(HTTP_URL, PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, false, TransactionStatus.PDNG);
        assertEquals(expectedLinks, links);

        //isFinalisedStatus
        Arrays.asList(ACCC, ACSC, RJCT, CANC).forEach(ts -> {
            links = new PaymentCancellationLinks(HTTP_URL, PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, true, ts);
            assertEquals(expectedLinks, links);
        });

        //isPending
        links = new PaymentCancellationLinks(HTTP_URL, PaymentType.SINGLE, PAYMENT_PRODUCT, PAYMENT_ID, false, PDNG);
        assertEquals(expectedLinks, links);
    }
}
