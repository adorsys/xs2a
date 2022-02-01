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
