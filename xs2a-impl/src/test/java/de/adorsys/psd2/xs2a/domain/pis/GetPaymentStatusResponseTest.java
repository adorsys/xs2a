/*
 * Copyright 2018-2024 adorsys GmbH & Co KG
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
 * contact us at sales@adorsys.com.
 */

package de.adorsys.psd2.xs2a.domain.pis;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetPaymentStatusResponseTest {
    @Test
    void isResponseContentTypeJson_withJson() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.APPLICATION_JSON, null, null, null, null);

        assertTrue(getPaymentStatusResponse.isResponseContentTypeJson());
    }

    @Test
    void isResponseContentTypeJson_withUtf8Json() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.APPLICATION_JSON_UTF8, null, null, null, null);

        assertTrue(getPaymentStatusResponse.isResponseContentTypeJson());
    }

    @Test
    void isResponseContentTypeJson_withXml_shouldReturnFalse() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.APPLICATION_XML, null, null, null, null);

        assertFalse(getPaymentStatusResponse.isResponseContentTypeJson());
    }

    @Test
    void isResponseContentTypeJson_withAll_shouldReturnFalse() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.ALL, null, null, null, null);

        assertFalse(getPaymentStatusResponse.isResponseContentTypeJson());
    }

    @Test
    void isResponseContentTypeJson_withWildcardApplication_shouldReturnFalse() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.parseMediaType("application/*"), null, null, null, null);

        assertFalse(getPaymentStatusResponse.isResponseContentTypeJson());
    }
}
