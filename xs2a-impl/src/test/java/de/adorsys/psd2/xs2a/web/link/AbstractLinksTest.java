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

package de.adorsys.psd2.xs2a.web.link;

import de.adorsys.psd2.xs2a.domain.HrefType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AbstractLinksTest {
    private static final String ABSOLUTE_HTTP_URL = "http://localhost:8080";
    private static final String RELATIVE_HTTP_URL = "/myhost.com";
    private static final String PAYMENT_PATH = "/v1/{payment-service}/{payment-product}/{payment-id}";
    private static final String PAYMENT_SERVICE = "payments";
    private static final String PAYMENT_PRODUCT = "sepa-credit-transfers";
    private static final String PAYMENT_ID = "j5drwApSajpoFP1h5_pYxh9ftC4ogs6_6emI06HD6O_pllrbeOyAHl4YXhAXbDTnk0TyKYKH8uiQvSrzLsiMcs_aJzS3dI_tse0HueAjChY=_=_psGLvQpt9Q";
    private static final String RESULT_ABSOLUTE_PATH = "http://localhost:8080/v1/payments/sepa-credit-transfers/j5drwApSajpoFP1h5_pYxh9ftC4ogs6_6emI06HD6O_pllrbeOyAHl4YXhAXbDTnk0TyKYKH8uiQvSrzLsiMcs_aJzS3dI_tse0HueAjChY=_=_psGLvQpt9Q";
    private static final String RESULT_RELATIVE_PATH = "/myhost.com/v1/payments/sepa-credit-transfers/j5drwApSajpoFP1h5_pYxh9ftC4ogs6_6emI06HD6O_pllrbeOyAHl4YXhAXbDTnk0TyKYKH8uiQvSrzLsiMcs_aJzS3dI_tse0HueAjChY=_=_psGLvQpt9Q";

    @Test
    void buildPath_absolute_path() {
        //Given
        AbstractLinks links = new AbstractLinks(ABSOLUTE_HTTP_URL);
        HrefType expected = new HrefType(RESULT_ABSOLUTE_PATH);

        //When
        HrefType actual = links.buildPath(PAYMENT_PATH, PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID);

        //Then
        assertEquals(expected.getHref(), actual.getHref());
    }

    @Test
    void buildPath_relative_path() {
        //Given
        AbstractLinks links = new AbstractLinks(RELATIVE_HTTP_URL);
        HrefType expected = new HrefType(RESULT_RELATIVE_PATH);

        //When
        HrefType actual = links.buildPath(PAYMENT_PATH, PAYMENT_SERVICE, PAYMENT_PRODUCT, PAYMENT_ID);

        //Then
        assertEquals(expected.getHref(), actual.getHref());
    }
}
