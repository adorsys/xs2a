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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

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
        paymentInfo.setInstanceId(paymentInitiationParameters.getInstanceId());

        return paymentInfo;
    }
}
