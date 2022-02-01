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

package de.adorsys.psd2.consent.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.consent.api.pis.CmsBasePaymentResponse;
import de.adorsys.psd2.consent.api.pis.CmsCommonPayment;
import de.adorsys.psd2.consent.api.pis.CmsCommonPaymentMapper;
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

    public CmsBasePaymentResponse expandCommonPaymentWithCorePayment(CmsCommonPayment cmsCommonPayment) {
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
