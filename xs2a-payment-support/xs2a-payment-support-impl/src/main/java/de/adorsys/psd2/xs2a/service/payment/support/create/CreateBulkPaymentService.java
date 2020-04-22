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

package de.adorsys.psd2.xs2a.service.payment.support.create;

import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.consent.Xs2aToCmsPisCommonPaymentRequestMapper;
import de.adorsys.psd2.xs2a.service.mapper.payment.RawToXs2aPaymentMapper;
import de.adorsys.psd2.xs2a.service.payment.create.AbstractCreatePaymentService;
import de.adorsys.psd2.xs2a.service.payment.support.create.spi.BulkPaymentInitiationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateBulkPaymentService extends AbstractCreatePaymentService<BulkPayment, BulkPaymentInitiationService> {
    private final RawToXs2aPaymentMapper rawToXs2aPaymentMapper;

    @Autowired
    public CreateBulkPaymentService(Xs2aPisCommonPaymentService pisCommonPaymentService,
                                    PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver,
                                    AuthorisationMethodDecider authorisationMethodDecider,
                                    Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper,
                                    Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper,
                                    BulkPaymentInitiationService paymentInitiationService,
                                    RequestProviderService requestProviderService,
                                    RawToXs2aPaymentMapper rawToXs2aPaymentMapper) {
        super(pisCommonPaymentService, pisScaAuthorisationServiceResolver, authorisationMethodDecider,
              xs2aPisCommonPaymentMapper, xs2aToCmsPisCommonPaymentRequestMapper, paymentInitiationService, requestProviderService);
        this.rawToXs2aPaymentMapper = rawToXs2aPaymentMapper;
    }

    @Override
    protected BulkPayment getPaymentRequest(byte[] payment, PaymentInitiationParameters paymentInitiationParameters) {
        return rawToXs2aPaymentMapper.mapToBulkPayment(payment);
    }
}
