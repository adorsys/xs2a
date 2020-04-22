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

package de.adorsys.psd2.xs2a.service.mapper.payment;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.xs2a.domain.pis.BulkPayment;
import de.adorsys.psd2.xs2a.domain.pis.PeriodicPayment;
import de.adorsys.psd2.xs2a.domain.pis.SinglePayment;
import de.adorsys.psd2.xs2a.service.mapper.PaymentModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RawToXs2aPaymentMapper {
    private final PaymentModelMapper paymentModelMapper;
    private final Xs2aObjectMapper xs2aObjectMapper;

    public SinglePayment mapToSinglePayment(byte[] paymentBody) {
        if (ArrayUtils.isEmpty(paymentBody)) {
            return null;
        }

        PaymentInitiationJson paymentInitiationJson = readBytes(paymentBody, PaymentInitiationJson.class);
        SinglePayment singlePayment = paymentModelMapper.mapToXs2aPayment(paymentInitiationJson);

        if (singlePayment != null) {
            singlePayment.setPaymentData(paymentBody);
        }

        return singlePayment;
    }

    public PeriodicPayment mapToPeriodicPayment(byte[] paymentBody) {
        if (ArrayUtils.isEmpty(paymentBody)) {
            return null;
        }

        PeriodicPaymentInitiationJson paymentInitiationJson = readBytes(paymentBody, PeriodicPaymentInitiationJson.class);
        PeriodicPayment periodicPayment = paymentModelMapper.mapToXs2aPayment(paymentInitiationJson);

        if (periodicPayment != null) {
            periodicPayment.setPaymentData(paymentBody);
        }

        return periodicPayment;
    }

    public BulkPayment mapToBulkPayment(byte[] paymentBody) {
        if (ArrayUtils.isEmpty(paymentBody)) {
            return null;
        }

        BulkPaymentInitiationJson paymentInitiationJson = readBytes(paymentBody, BulkPaymentInitiationJson.class);
        BulkPayment bulkPayment = paymentModelMapper.mapToXs2aPayment(paymentInitiationJson);

        if (bulkPayment != null) {
            bulkPayment.setPaymentData(paymentBody);
        }

        return bulkPayment;
    }

    private <T> T readBytes(byte[] paymentBody, Class<T> clazz) {
        try {
            return xs2aObjectMapper.readValue(paymentBody, clazz);
        } catch (IOException e) {
            log.warn("Couldn't deserialize payment from bytes");
            return null;
        }
    }
}
