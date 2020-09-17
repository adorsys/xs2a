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

package de.adorsys.psd2.xs2a.domain.pis;

import de.adorsys.psd2.xs2a.core.pis.CoreCommonPayment;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.CustomContentTypeProvider;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class CommonPayment implements CustomContentTypeProvider, CoreCommonPayment {
    protected String paymentId;
    protected String paymentProduct;
    protected TransactionStatus transactionStatus;
    protected PaymentType paymentType;
    protected byte[] paymentData;
    protected List<PsuIdData> psuDataList;
    protected OffsetDateTime statusChangeTimestamp;
    protected String creditorId;
    protected OffsetDateTime creationTimestamp;
    private String contentType;
    protected String instanceId;

    @Override
    public MediaType getCustomContentType() {
        if (StringUtils.isBlank(contentType)) {
            return MediaType.APPLICATION_JSON;
        }
        return MediaType.parseMediaType(contentType);
    }
}
