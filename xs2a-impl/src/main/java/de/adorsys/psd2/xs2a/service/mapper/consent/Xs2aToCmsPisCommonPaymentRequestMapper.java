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

package de.adorsys.psd2.xs2a.service.mapper.consent;

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppNotificationData;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationResponse;
import de.adorsys.psd2.xs2a.service.payment.create.PisPaymentInfoCreationObject;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;

@Component
@AllArgsConstructor
public class Xs2aToCmsPisCommonPaymentRequestMapper {

    public PisPaymentInfo mapToPisPaymentInfo(PisPaymentInfoCreationObject creationObject) {
        PaymentInitiationParameters paymentInitiationParameters = creationObject.getPaymentInitiationParameters();
        PaymentInitiationResponse response = creationObject.getResponse();

        PisPaymentInfo paymentInfo = new PisPaymentInfo();
        paymentInfo.setPaymentProduct(paymentInitiationParameters.getPaymentProduct());
        paymentInfo.setPaymentType(paymentInitiationParameters.getPaymentType());
        paymentInfo.setTransactionStatus(response.getTransactionStatus());
        paymentInfo.setTppInfo(creationObject.getTppInfo());
        paymentInfo.setPaymentId(response.getPaymentId());
        paymentInfo.setPsuDataList(Collections.singletonList(paymentInitiationParameters.getPsuData()));
        paymentInfo.setMultilevelScaRequired(response.isMultilevelScaRequired());
        paymentInfo.setAspspAccountId(response.getAspspAccountId());
        paymentInfo.setTppRedirectUri(paymentInitiationParameters.getTppRedirectUri());
        paymentInfo.setInternalRequestId(creationObject.getInternalRequestId());
        paymentInfo.setPaymentData(creationObject.getPaymentData());
        paymentInfo.setCreationTimestamp(creationObject.getCreationTimestamp());
        paymentInfo.setTppNotificationUri(Optional.ofNullable(paymentInitiationParameters.getTppNotificationData())
                                              .map(TppNotificationData::getTppNotificationUri)
                                              .orElse(null));
        paymentInfo.setNotificationSupportedModes(Optional.ofNullable(paymentInitiationParameters.getTppNotificationData())
                                                      .map(TppNotificationData::getNotificationModes)
                                                      .orElse(Collections.emptyList()));
        paymentInfo.setContentType(creationObject.getContentType());
        paymentInfo.setTppBrandLoggingInformation(paymentInitiationParameters.getTppBrandLoggingInformation());

        return paymentInfo;
    }
}
