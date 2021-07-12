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

package de.adorsys.psd2.xs2a.service.payment.create;

import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.domain.pis.CommonPayment;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.ScaApproachResolver;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationChainResponsibilityService;
import de.adorsys.psd2.xs2a.service.authorization.AuthorisationMethodDecider;
import de.adorsys.psd2.xs2a.service.authorization.pis.PisScaAuthorisationServiceResolver;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPisCommonPaymentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPisCommonPaymentMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aToCmsPisCommonPaymentRequestMapper;
import de.adorsys.psd2.xs2a.service.payment.create.spi.CommonPaymentInitiationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CreateCommonPaymentService extends AbstractCreatePaymentService<CommonPayment, CommonPaymentInitiationService> {

    @Autowired
    public CreateCommonPaymentService(Xs2aPisCommonPaymentService pisCommonPaymentService,
                                      PisScaAuthorisationServiceResolver pisScaAuthorisationServiceResolver,
                                      AuthorisationMethodDecider authorisationMethodDecider,
                                      Xs2aPisCommonPaymentMapper xs2aPisCommonPaymentMapper,
                                      Xs2aToCmsPisCommonPaymentRequestMapper xs2aToCmsPisCommonPaymentRequestMapper,
                                      CommonPaymentInitiationService paymentInitiationService,
                                      RequestProviderService requestProviderService,
                                      LoggingContextService loggingContextService,
                                      AuthorisationChainResponsibilityService authorisationChainResponsibilityService,
                                      ScaApproachResolver scaApproachResolver) {
        super(pisCommonPaymentService, pisScaAuthorisationServiceResolver, authorisationMethodDecider,
              xs2aPisCommonPaymentMapper, xs2aToCmsPisCommonPaymentRequestMapper, paymentInitiationService,
              requestProviderService, loggingContextService, authorisationChainResponsibilityService, scaApproachResolver);
    }

    @Override
    protected CommonPayment getPaymentRequest(byte[] payment, PaymentInitiationParameters paymentInitiationParameters) {
        CommonPayment request = new CommonPayment();
        request.setPaymentType(paymentInitiationParameters.getPaymentType());
        request.setPaymentProduct(paymentInitiationParameters.getPaymentProduct());
        request.setPaymentData(payment);
        request.setPsuDataList(Collections.singletonList(paymentInitiationParameters.getPsuData()));
        request.setInstanceId(paymentInitiationParameters.getInstanceId());
        return request;
    }
}
