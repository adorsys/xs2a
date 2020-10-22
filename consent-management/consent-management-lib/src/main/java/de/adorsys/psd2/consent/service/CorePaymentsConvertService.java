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

package de.adorsys.psd2.consent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.consent.api.pis.CmsCommonPayment;
import de.adorsys.psd2.consent.api.pis.CmsCommonPaymentMapper;
import de.adorsys.psd2.consent.api.pis.CmsPayment;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.service.mapper.CmsCorePaymentMapper;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CorePaymentsConvertService {
    private final CmsCorePaymentMapper cmsCorePaymentMapper;
    private final Xs2aObjectMapper xs2aObjectMapper;
    private final PaymentMapperResolver paymentMapperResolver;

    public byte[] buildPaymentData(List<PisPayment> pisPayments, PaymentType paymentType) {
        switch (paymentType) {
            case SINGLE:
                return writeValueAsBytes(cmsCorePaymentMapper.mapToPaymentInitiationJson(pisPayments));
            case BULK:
                return writeValueAsBytes(cmsCorePaymentMapper.mapToBulkPaymentInitiationJson(pisPayments));
            case PERIODIC:
                return writeValueAsBytes(cmsCorePaymentMapper.mapToPeriodicPaymentInitiationJson(pisPayments));
            default:
                return new byte[0];
        }
    }

    public CmsPayment expandCommonPaymentWithCorePayment(CmsCommonPayment cmsCommonPayment) {
        CmsCommonPaymentMapper cmsCommonPaymentMapper = paymentMapperResolver.getCmsCommonPaymentMapper(cmsCommonPayment.getPaymentProduct());
        switch (cmsCommonPayment.getPaymentType()) {
            case SINGLE:
                return cmsCommonPaymentMapper.mapToCmsSinglePayment(cmsCommonPayment);
            case BULK:
                return cmsCommonPaymentMapper.mapToCmsBulkPayment(cmsCommonPayment);
            case PERIODIC:
                return cmsCommonPaymentMapper.mapToCmsPeriodicPayment(cmsCommonPayment);
            default:
                return cmsCommonPayment;
        }
    }

    private byte[] writeValueAsBytes(Object object) {
        if (object == null) {
            return new byte[0];
        }

        try {
            return xs2aObjectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.warn("Can't convert object to byte[] : {}", e.getMessage());
            return new byte[0];
        }
    }
}
