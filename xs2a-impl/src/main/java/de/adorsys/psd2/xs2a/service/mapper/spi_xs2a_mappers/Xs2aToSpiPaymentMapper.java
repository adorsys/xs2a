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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.service.SpiPayment;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class Xs2aToSpiPaymentMapper {
    private final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    private final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    private final Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    private final Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    private final Xs2aToSpiPsuDataMapper xs2aToSpiPsuDataMapper;

    public SpiPayment mapToSpiPayment(GetPisAuthorisationResponse pisAuthorisationResponse,
                                         PaymentType paymentType, String paymentProduct) {
        if (CollectionUtils.isEmpty(pisAuthorisationResponse.getPayments())) {
            return mapToSpiPayment(pisAuthorisationResponse.getPaymentInfo());
        } else {
            return mapToSpiPayment(pisAuthorisationResponse.getPayments(), paymentType, paymentProduct, pisAuthorisationResponse.getPaymentId());
        }
    }

    private SpiPayment mapToSpiPayment(List<PisPayment> payments, PaymentType paymentType, String paymentProduct, String globalPaymentId) {
        if (PaymentType.SINGLE == paymentType) {
            SinglePayment singlePayment = cmsToXs2aPaymentMapper.mapToSinglePayment(payments.get(0));
            return xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(singlePayment, paymentProduct);
        }
        if (PaymentType.PERIODIC == paymentType) {
            PeriodicPayment periodicPayment = cmsToXs2aPaymentMapper.mapToPeriodicPayment(payments.get(0));
            return xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(periodicPayment, paymentProduct);
        } else {
            BulkPayment bulkPayment = cmsToXs2aPaymentMapper.mapToBulkPayment(payments);
            bulkPayment.setPaymentId(globalPaymentId);
            return xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(bulkPayment, paymentProduct);
        }
    }

    private SpiPayment mapToSpiPayment(PisPaymentInfo paymentInfo) {
        SpiPaymentInfo spiPaymentInfo = new SpiPaymentInfo(paymentInfo.getPaymentProduct());
        spiPaymentInfo.setPaymentId(paymentInfo.getPaymentId());
        spiPaymentInfo.setPaymentType(paymentInfo.getPaymentType());
        spiPaymentInfo.setStatus(paymentInfo.getTransactionStatus());
        spiPaymentInfo.setPaymentData(paymentInfo.getPaymentData());
        spiPaymentInfo.setPsuDataList(xs2aToSpiPsuDataMapper.mapToSpiPsuDataList(paymentInfo.getPsuDataList()));
        return spiPaymentInfo;
    }
}
