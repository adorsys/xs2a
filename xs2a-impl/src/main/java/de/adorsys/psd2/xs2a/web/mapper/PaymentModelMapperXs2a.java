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

package de.adorsys.psd2.xs2a.web.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import de.adorsys.psd2.xs2a.domain.pis.PaymentInitiationParameters;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static de.adorsys.psd2.xs2a.core.profile.PaymentType.PERIODIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentModelMapperXs2a {
    private final HttpServletRequest httpServletRequest;
    private final Xs2aObjectMapper xs2aObjectMapper;

    public byte[] mapToXs2aPayment() {
            return buildBinaryBodyData(httpServletRequest);
    }

    public byte[] mapToXs2aRawPayment(PaymentInitiationParameters requestParameters, Object xmlSct, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingOrderType) {
        if (requestParameters.getPaymentType() == PERIODIC) {
            return buildPeriodicBinaryBodyData(xmlSct, jsonStandingOrderType);
        }

        return buildBinaryBodyData(httpServletRequest);
    }

    private byte[] buildBinaryBodyData(HttpServletRequest httpServletRequest) {
        try {
            return IOUtils.toByteArray(httpServletRequest.getInputStream());
        } catch (IOException e) {
            log.warn("Cannot deserialize httpServletRequest body!", e);
            return new byte[0];
        }
    }

    private byte[] buildPeriodicBinaryBodyData(Object xmlPart, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonPart) {
        String serialisedJsonPart = null;
        try {
            serialisedJsonPart = xs2aObjectMapper.writeValueAsString(jsonPart);
        } catch (JsonProcessingException e) {
            log.info("Can't convert object to json: {}", e.getMessage());
        }
        if (xmlPart == null || serialisedJsonPart == null) {
            throw new IllegalArgumentException("Invalid body of the multipart request!");
        }

        String body = xmlPart + "\n" + serialisedJsonPart;
        return body.getBytes(StandardCharsets.UTF_8);
    }
}
