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

package de.adorsys.psd2.xs2a.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.adorsys.psd2.model.BulkPaymentInitiationJson;
import de.adorsys.psd2.model.PaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationJson;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import de.adorsys.psd2.xs2a.component.JsonConverter;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import de.adorsys.psd2.xs2a.service.validator.ValueValidatorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.PERIODIC;
import static de.adorsys.psd2.xs2a.core.profile.PaymentType.SINGLE;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentModelMapperXs2a {
    private final ObjectMapper mapper;
    private final ValueValidatorService validationService;
    private final HttpServletRequest httpServletRequest;
    private final JsonConverter jsonConverter;
    private final PaymentModelMapper paymentModelMapper;

    public Object mapToXs2aPayment(Object payment, PaymentInitiationParameters requestParameters) {
        if (requestParameters.getPaymentType() == SINGLE) {
            return paymentModelMapper.mapToXs2aPayment(validatePayment(payment, PaymentInitiationJson.class));
        } else if (requestParameters.getPaymentType() == PERIODIC) {
            return paymentModelMapper.mapToXs2aPayment(validatePayment(payment, PeriodicPaymentInitiationJson.class));
        } else {
            return paymentModelMapper.mapToXs2aPayment(validatePayment(payment, BulkPaymentInitiationJson.class));
        }
    }

    public Object mapToXs2aRawPayment(PaymentInitiationParameters requestParameters, Object xmlSct, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingOrderType) {
        if (requestParameters.getPaymentType() == PERIODIC) {
            return buildPeriodicBinaryBodyData(xmlSct, jsonStandingOrderType);
        }

        return buildBinaryBodyData(httpServletRequest);
    }

    private <R> R validatePayment(Object payment, Class<R> clazz) {
        R result = mapper.convertValue(payment, clazz);
        validationService.validate(result);
        return result;
    }

    private byte[] buildBinaryBodyData(HttpServletRequest httpServletRequest) {
        try {
            return IOUtils.toByteArray(httpServletRequest.getInputStream());
        } catch (IOException e) {
            log.warn("Cannot deserialize httpServletRequest body!", e);
        }

        return null;
    }

    private byte[] buildPeriodicBinaryBodyData(Object xmlPart, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonPart) {
        Optional<String> serialisedJsonPart = jsonConverter.toJson(jsonPart);
        if (xmlPart == null || !serialisedJsonPart.isPresent()) {
            throw new IllegalArgumentException("Invalid body of the multipart request!");
        }

        String body = xmlPart + "\n" + serialisedJsonPart.get();
        return body.getBytes(Charset.forName("UTF-8"));
    }
}
