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

package de.adorsys.psd2.consent.config;

import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.*;

public class PisPaymentRemoteUrlsTest {

    private static final String BASE_URL = "http://base.url";

    private PisPaymentRemoteUrls pisPaymentRemoteUrls;

    @Before
    public void setUp() {
        pisPaymentRemoteUrls = new PisPaymentRemoteUrls();
        ReflectionTestUtils.setField(pisPaymentRemoteUrls, "paymentServiceBaseUrl", BASE_URL);
    }

    @Test
    public void updatePaymentStatus() {
        assertEquals("http://base.url/pis/payment/{payment-id}/status/{status}",
                     pisPaymentRemoteUrls.updatePaymentStatus());
    }

    @Test
    public void updatePaymentCancellationRedirectURIs() {
        assertEquals("http://base.url/pis/payment/{payment-id}/cancellation/redirects",
                     pisPaymentRemoteUrls.updatePaymentCancellationRedirectURIs());
    }
}
