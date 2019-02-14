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

package de.adorsys.psd2.xs2a.service.authorization.pis.stage;

import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.consent.api.pis.authorisation.GetPisAuthorisationResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.domain.consent.pis.Xs2aUpdatePisCommonPaymentPsuDataRequest;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiBulkPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPeriodicPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiSinglePaymentMapper;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiPaymentInfo;
import de.adorsys.psd2.xs2a.spi.service.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public abstract class PisScaStage<T, U, R> implements BiFunction<T, U, R> {
    private final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    private final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    private final Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    private final Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;
    private final ApplicationContext applicationContext;

    protected PaymentSpi getPaymentService(GetPisAuthorisationResponse pisAuthorisationResponse, PaymentType paymentType) {
        // todo implementation should be changed https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/534
        if (CollectionUtils.isEmpty(pisAuthorisationResponse.getPayments())) {
            return applicationContext.getBean(CommonPaymentSpi.class);
        }

        if (PaymentType.SINGLE == paymentType) {
            return applicationContext.getBean(SinglePaymentSpi.class);
        } else if (PaymentType.PERIODIC == paymentType) {
            return applicationContext.getBean(PeriodicPaymentSpi.class);
        } else {
            return applicationContext.getBean(BulkPaymentSpi.class);
        }
    }

    protected SpiPayment mapToSpiPayment(GetPisAuthorisationResponse pisAuthorisationResponse,
                                         PaymentType paymentType, String paymentProduct) {
        if (CollectionUtils.isEmpty(pisAuthorisationResponse.getPayments())) {
            return mapToSpiPayment(pisAuthorisationResponse.getPaymentInfo());
        } else {
            return mapToSpiPayment(pisAuthorisationResponse.getPayments(), paymentType, paymentProduct);
        }
    }

    private SpiPayment mapToSpiPayment(List<PisPayment> payments, PaymentType paymentType, String paymentProduct) {
        if (PaymentType.SINGLE == paymentType) {
            SinglePayment singlePayment = cmsToXs2aPaymentMapper.mapToSinglePayment(payments.get(0));
            return xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(singlePayment, paymentProduct);
        }
        if (PaymentType.PERIODIC == paymentType) {
            PeriodicPayment periodicPayment = cmsToXs2aPaymentMapper.mapToPeriodicPayment(payments.get(0));
            return xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(periodicPayment, paymentProduct);
        } else {
            BulkPayment bulkPayment = cmsToXs2aPaymentMapper.mapToBulkPayment(payments);
            return xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(bulkPayment, paymentProduct);
        }
    }

    private SpiPayment mapToSpiPayment(PisPaymentInfo paymentInfo) {
        SpiPaymentInfo spiPaymentInfo = new SpiPaymentInfo(paymentInfo.getPaymentProduct());
        spiPaymentInfo.setPaymentId(paymentInfo.getPaymentId());
        spiPaymentInfo.setPaymentType(paymentInfo.getPaymentType());
        spiPaymentInfo.setStatus(paymentInfo.getTransactionStatus());
        spiPaymentInfo.setPaymentData(paymentInfo.getPaymentData());

        return spiPaymentInfo;
    }

    protected PsuIdData extractPsuIdData(Xs2aUpdatePisCommonPaymentPsuDataRequest request, boolean paymentCancellation) {
        PsuIdData psuDataInRequest = request.getPsuData();
        if (isPsuExist(psuDataInRequest)) {
            return psuDataInRequest;
        }

        return getGetPisAuthorisationResponse(request.getAuthorisationId(), paymentCancellation)
                   .map(GetPisAuthorisationResponse::getPsuId)
                   .filter(StringUtils::isNotBlank)
                   .map(id -> new PsuIdData(id, null, null, null))
                   .orElse(psuDataInRequest);
    }

    protected boolean isPsuExist(PsuIdData psuIdData) {
        return Optional.ofNullable(psuIdData)
                   .map(PsuIdData::isNotEmpty)
                   .orElse(false);
    }

    private Optional<GetPisAuthorisationResponse> getGetPisAuthorisationResponse(String authorisationId, boolean paymentCancellation) {
        return paymentCancellation
                   ? pisCommonPaymentServiceEncrypted.getPisCancellationAuthorisationById(authorisationId)
                   : pisCommonPaymentServiceEncrypted.getPisAuthorisationById(authorisationId);
    }
}
