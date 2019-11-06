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

package de.adorsys.psd2.xs2a.domain.pis;

import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import org.junit.Test;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;

import static org.junit.Assert.*;

public class GetPaymentStatusResponseTest {
    @Test
    public void isResponseContentTypeJson_withJson() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.APPLICATION_JSON, null);

        assertTrue(getPaymentStatusResponse.isResponseContentTypeJson());
    }

    @Test
    public void isResponseContentTypeJson_withUtf8Json() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.APPLICATION_JSON_UTF8, null);

        assertTrue(getPaymentStatusResponse.isResponseContentTypeJson());
    }

    @Test
    public void isResponseContentTypeJson_withXml_shouldReturnFalse() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.APPLICATION_XML, null);

        assertFalse(getPaymentStatusResponse.isResponseContentTypeJson());
    }

    @Test
    public void isResponseContentTypeJson_withAll_shouldReturnFalse() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.ALL, null);

        assertFalse(getPaymentStatusResponse.isResponseContentTypeJson());
    }

    @Test
    public void isResponseContentTypeJson_withWildcardApplication_shouldReturnFalse() {
        GetPaymentStatusResponse getPaymentStatusResponse = new GetPaymentStatusResponse(TransactionStatus.ACSP, null, MediaType.parseMediaType("application/*"), null);

        assertFalse(getPaymentStatusResponse.isResponseContentTypeJson());
    }
}
