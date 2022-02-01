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
    private final MultiPartBoundaryBuilder multiPartBoundaryBuilder;

    public byte[] mapToXs2aPayment() {
            return buildBinaryBodyData(httpServletRequest);
    }

    public byte[] mapToXs2aRawPayment(PaymentInitiationParameters requestParameters, Object xmlSct, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonStandingOrderType) {
        if (requestParameters.getPaymentType() == PERIODIC) {
            return buildPeriodicBinaryBodyData(httpServletRequest, xmlSct, jsonStandingOrderType);
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

    private byte[] buildPeriodicBinaryBodyData(HttpServletRequest httpServletRequest, Object xmlPart, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson jsonPart) {
        String serialisedJsonPart = null;
        try {
            serialisedJsonPart = xs2aObjectMapper.writeValueAsString(jsonPart);
        } catch (JsonProcessingException e) {
            log.info("Can't convert object to json: {}", e.getMessage());
        }
        if (xmlPart == null || serialisedJsonPart == null) {
            throw new IllegalArgumentException("Invalid body of the multipart request!");
        }
        String body = multiPartBoundaryBuilder.getMultiPartContent(httpServletRequest, (String) xmlPart, serialisedJsonPart);
        return body.getBytes(StandardCharsets.UTF_8);
    }
}
