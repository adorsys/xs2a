/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.spi.domain.response;

import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SpiResponseTest {

    private static final AspspConsentData SOME_ASPSP_CONSENT_DATA =
        new AspspConsentData(new byte[0], "Some consent ID");
    private static final String SOME_PAYLOAD = "some payload";
    private static final SpiResponseStatus SOME_STATUS = SpiResponseStatus.LOGICAL_FAILURE;

    @Test(expected = IllegalStateException.class)
    public void builder_should_fail_on_success_without_payload() {
        SpiResponse.SpiResponseBuilder<Object> builder = SpiResponse.builder();

        builder
            .aspspConsentData(SOME_ASPSP_CONSENT_DATA)
            .success();
    }

    @Test(expected = IllegalStateException.class)
    public void builder_should_fail_on_success_without_aspsp_data() {
        SpiResponse.SpiResponseBuilder<Object> builder = SpiResponse.builder();

        builder
            .payload(SOME_PAYLOAD)
            .success();
    }

    @Test(expected = IllegalArgumentException.class)
    public void builder_should_fail_on_failure_with_null_status() {
        SpiResponse.SpiResponseBuilder<Object> builder = SpiResponse.builder();

        //noinspection ConstantConditions
        builder
            .fail(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_should_fail_without_payload() {
        //noinspection ConstantConditions
        new SpiResponse<>(null, SOME_ASPSP_CONSENT_DATA);
    }

    @Test(expected = IllegalArgumentException.class)
    public void constructor_should_fail_without_aspsp_consent_data() {
        //noinspection ConstantConditions
        new SpiResponse<>(SOME_PAYLOAD, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void second_constructor_should_fail_without_aspsp_consent_data() {
        //noinspection ConstantConditions
        new SpiResponse<>(SOME_PAYLOAD, null, SOME_STATUS, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void second_constructor_should_fail_without_payload() {
        //noinspection ConstantConditions
        new SpiResponse<>(null, SOME_ASPSP_CONSENT_DATA, SOME_STATUS, null);
    }

    @Test
    public void second_constructor_without_status_defaults_to_error() {
        //noinspection ConstantConditions
        SpiResponse<Object> spiResponse = new SpiResponse<>(SOME_PAYLOAD, SOME_ASPSP_CONSENT_DATA, null, null);
        assertTrue(spiResponse.hasError());
        assertFalse(spiResponse.isSuccessful());
    }
}
