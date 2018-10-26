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

package de.adorsys.aspsp.xs2a.service.authorization.pis.stage;

import de.adorsys.aspsp.xs2a.domain.pis.BulkPayment;
import de.adorsys.aspsp.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.aspsp.xs2a.domain.pis.SinglePayment;
import de.adorsys.aspsp.xs2a.service.consent.PisConsentDataService;
import de.adorsys.aspsp.xs2a.service.mapper.consent.CmsToXs2aPaymentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.consent.Xs2aPisConsentMapper;
import de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers.*;
import de.adorsys.psd2.consent.api.pis.PisPayment;
import de.adorsys.psd2.xs2a.core.profile.PaymentProduct;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.spi.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.function.BiFunction;

@RequiredArgsConstructor
public abstract class PisScaStage<T, U, R> implements BiFunction<T, U, R> {
    protected final PaymentAuthorisationSpi paymentAuthorisationSpi;
    protected final PisConsentDataService pisConsentDataService;
    protected final CmsToXs2aPaymentMapper cmsToXs2aPaymentMapper;
    protected final Xs2aToSpiPeriodicPaymentMapper xs2aToSpiPeriodicPaymentMapper;
    protected final Xs2aToSpiSinglePaymentMapper xs2aToSpiSinglePaymentMapper;
    protected final Xs2aToSpiBulkPaymentMapper xs2aToSpiBulkPaymentMapper;
    protected final SpiToXs2aAuthenticationObjectMapper spiToXs2aAuthenticationObjectMapper;
    protected final Xs2aPisConsentMapper xs2aPisConsentMapper;
    protected final SpiErrorMapper spiErrorMapper;

    @Autowired
    private ApplicationContext applicationContext;

    protected PaymentSpi getPaymentService(PaymentType paymentType) {
        if (PaymentType.SINGLE == paymentType) {
            return applicationContext.getBean(SinglePaymentSpi.class);
        } else if (PaymentType.PERIODIC == paymentType) {
            return applicationContext.getBean(PeriodicPaymentSpi.class);
        } else {
            return applicationContext.getBean(BulkPaymentSpi.class);
        }
    }

    // TODO pass actual PaymentProduct https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/442
    protected SpiPayment mapToSpiPayment(List<PisPayment> payments, PaymentType paymentType) {
        if (PaymentType.SINGLE == paymentType) {
            SinglePayment singlePayment = cmsToXs2aPaymentMapper.mapToSinglePayment(payments.get(0));
            return xs2aToSpiSinglePaymentMapper.mapToSpiSinglePayment(singlePayment, PaymentProduct.SEPA);
        }
        if (PaymentType.PERIODIC == paymentType) {
            PeriodicPayment periodicPayment = cmsToXs2aPaymentMapper.mapToPeriodicPayment(payments.get(0));
            return xs2aToSpiPeriodicPaymentMapper.mapToSpiPeriodicPayment(periodicPayment, PaymentProduct.SEPA);
        } else {
            BulkPayment bulkPayment = cmsToXs2aPaymentMapper.mapToBulkPayment(payments);
            return xs2aToSpiBulkPaymentMapper.mapToSpiBulkPayment(bulkPayment, PaymentProduct.SEPA);
        }
    }
}
