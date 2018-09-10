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

package de.adorsys.aspsp.xs2a.service.mapper.consent;

import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.domain.consent.Xsa2CreatePisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentInitialisationResponse;
import de.adorsys.aspsp.xs2a.domain.pis.PaymentType;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.domain.pis.TppInfo;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTppInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiCreatePisConsentAuthorizationResponse;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiPisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiScaStatus;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPeriodicPayment;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiSinglePayment;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class Xs2aPisConsentMapper {

    private final PaymentMapper paymentMapper;

    public SpiPisConsentRequest mapToSpiPisConsentRequestForSinglePayment(CreatePisConsentData createPisConsentData, String paymentId) {
        SpiPisConsentRequest request = new SpiPisConsentRequest();
        request.setPayments(Arrays.asList(mapToSpiSinglePaymentWithPaymentId(createPisConsentData, paymentId)));
        request.setPaymentProduct(createPisConsentData.getPaymentProduct());
        request.setPaymentType(SpiPaymentType.SINGLE);
        request.setTppInfo(mapToSpiTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(createPisConsentData.getAspspConsentData());

        return request;
    }

    public SpiPisConsentRequest mapToSpiPisConsentRequestForPeriodicPayment(CreatePisConsentData createPisConsentData, String paymentId) {
        SpiPisConsentRequest request = new SpiPisConsentRequest();
        request.setPayments(Arrays.asList(mapToSpiPeriodicPaymentWithPaymentId(createPisConsentData, paymentId)));
        request.setPaymentProduct(createPisConsentData.getPaymentProduct());
        request.setPaymentType(SpiPaymentType.PERIODIC);
        request.setTppInfo(mapToSpiTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(createPisConsentData.getAspspConsentData());

        return request;
    }

    public SpiPisConsentRequest mapToSpiPisConsentRequestForBulkPayment(CreatePisConsentData createPisConsentData) {
        SpiPisConsentRequest request = new SpiPisConsentRequest();
        request.setPayments(mapToSpiSinglePaymentList(createPisConsentData.getPaymentIdentifierMap()));
        request.setPaymentProduct(createPisConsentData.getPaymentProduct());
        request.setPaymentType(SpiPaymentType.BULK);
        request.setTppInfo(mapToSpiTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(createPisConsentData.getAspspConsentData());

        return request;
    }

    private SpiSinglePayment mapToSpiSinglePaymentWithPaymentId(CreatePisConsentData createPisConsentData, String paymentId) {
        SpiSinglePayment spiSinglePayment = paymentMapper.mapToSpiSinglePayment(createPisConsentData.getSinglePayment());
        spiSinglePayment.setPaymentId(paymentId);

        return spiSinglePayment;
    }

    private SpiPeriodicPayment mapToSpiPeriodicPaymentWithPaymentId(CreatePisConsentData createPisConsentData, String paymentId) {
        SpiPeriodicPayment spiPeriodicPayment = paymentMapper.mapToSpiPeriodicPayment(createPisConsentData.getPeriodicPayment());
        spiPeriodicPayment.setPaymentId(paymentId);

        return spiPeriodicPayment;
    }

    private List<SpiSinglePayment> mapToSpiSinglePaymentList(Map<SinglePayment, PaymentInitialisationResponse> paymentIdentifierMap) {
        return paymentIdentifierMap.entrySet().stream()
                   .map(etr -> {
                       SpiSinglePayment spiSinglePayment = paymentMapper.mapToSpiSinglePayment(etr.getKey());
                       spiSinglePayment.setPaymentId(etr.getValue().getPaymentId());
                       return spiSinglePayment;
                   })
                   .collect(Collectors.toList());
    }

    private SpiTppInfo mapToSpiTppInfo(TppInfo tppInfo) {
        return Optional.ofNullable(tppInfo)
                   .map(info -> new SpiTppInfo(
                       info.getRegistrationNumber(),
                       info.getTppName(),
                       info.getTppRole(),
                       info.getNationalCompetentAuthority(),
                       info.getRedirectUri(),
                       info.getNokRedirectUri()
                   )).orElse(null);
    }

    public Optional<Xsa2CreatePisConsentAuthorizationResponse> mapToXsa2CreatePisConsentAuthorizationResponse(SpiCreatePisConsentAuthorizationResponse spi, PaymentType paymentType) {
        return Optional.ofNullable(spi)
                   .map(s -> new Xsa2CreatePisConsentAuthorizationResponse(s.getAuthorizationId(), SpiScaStatus.RECEIVED.name(), paymentType.getValue()));
    }
}
