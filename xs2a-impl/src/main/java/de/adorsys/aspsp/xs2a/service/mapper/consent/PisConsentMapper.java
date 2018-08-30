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

import com.google.common.collect.Lists;
import de.adorsys.aspsp.xs2a.domain.consent.CreatePisConsentData;
import de.adorsys.aspsp.xs2a.domain.pis.TppInfo;
import de.adorsys.aspsp.xs2a.service.mapper.PaymentMapper;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiTppInfo;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiPisConsentRequest;
import de.adorsys.aspsp.xs2a.spi.domain.payment.SpiPaymentType;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@AllArgsConstructor
public class PisConsentMapper {

    private final PaymentMapper paymentMapper;

    public SpiPisConsentRequest mapToSpiPisConsentRequestForSinglePayment(CreatePisConsentData createPisConsentData) {
        SpiPisConsentRequest request = new SpiPisConsentRequest();
        request.setPayments(Arrays.asList(paymentMapper.mapToSpiSinglePayment(createPisConsentData.getSinglePayment())));
        request.setPaymentProduct(createPisConsentData.getPaymentProduct());
        request.setPaymentType(SpiPaymentType.SINGLE);
        request.setTppInfo(mapToSpiTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(createPisConsentData.getAspspConsentData());

        return request;
    }

    public SpiPisConsentRequest mapToSpiPisConsentRequestForBulkPayment(CreatePisConsentData createPisConsentData) {
        SpiPisConsentRequest request = new SpiPisConsentRequest();
        request.setPayments(paymentMapper.mapToSpiSinglePaymentList(Lists.newArrayList(createPisConsentData.getPaymentIdentifierMap().keySet())));
        request.setPaymentProduct(createPisConsentData.getPaymentProduct());
        request.setPaymentType(SpiPaymentType.BULK);
        request.setTppInfo(mapToSpiTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(createPisConsentData.getAspspConsentData());

        return request;
    }

    public SpiPisConsentRequest mapToSpiPisConsentRequestForPeriodicPayment(CreatePisConsentData createPisConsentData) {
        SpiPisConsentRequest request = new SpiPisConsentRequest();
        request.setPayments(Arrays.asList(paymentMapper.mapToSpiPeriodicPayment(createPisConsentData.getPeriodicPayment())));
        request.setPaymentProduct(createPisConsentData.getPaymentProduct());
        request.setPaymentType(SpiPaymentType.PERIODIC);
        request.setTppInfo(mapToSpiTppInfo(createPisConsentData.getTppInfo()));
        request.setAspspConsentData(createPisConsentData.getAspspConsentData());

        return request;
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
}
