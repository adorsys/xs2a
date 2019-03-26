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

package de.adorsys.psd2.xs2a.integration.builder;

import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.xs2a.core.pis.TransactionStatus;
import de.adorsys.psd2.xs2a.core.profile.PaymentType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;

import java.util.Collections;

import static org.mockito.Matchers.any;

public class PisPaymentInfoBuilder {
    private final static TppInfo TPP_INFO = TppInfoBuilder.buildTppInfo();
    private final static PsuIdData PSU_DATA = PsuIdDataBuilder.buildPsuIdData();

    public static PisPaymentInfo buildPisPaymentInfo(String paymentProduct, PaymentType paymentType, String aspspAccountId) {
        PisPaymentInfo paymentInfo = new PisPaymentInfo();
        paymentInfo.setPaymentId(any(String.class));
        paymentInfo.setPaymentProduct(paymentProduct);
        paymentInfo.setPaymentType(paymentType);
        paymentInfo.setTransactionStatus(TransactionStatus.RCVD);
        paymentInfo.setTppInfo(TPP_INFO);
        paymentInfo.setPsuDataList(Collections.singletonList(PSU_DATA));
        paymentInfo.setAspspAccountId(aspspAccountId);
        return paymentInfo;
    }

}
