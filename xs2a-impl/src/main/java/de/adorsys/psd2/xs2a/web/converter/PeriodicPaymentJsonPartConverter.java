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

package de.adorsys.psd2.xs2a.web.converter;

import de.adorsys.psd2.mapper.Xs2aObjectMapper;
import de.adorsys.psd2.model.PeriodicPaymentInitiationXmlPart2StandingorderTypeJson;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Converter, responsible for mapping incoming json_standingorderType parameter into PeriodicPaymentInitiationXmlPart2StandingorderTypeJson
 * in {@link de.adorsys.psd2.api.PaymentApi#initiatePayment} in case of multipart payment initiation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PeriodicPaymentJsonPartConverter implements Converter<String, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson> {
    private final Xs2aObjectMapper xs2aObjectMapper;

    @Override
    public PeriodicPaymentInitiationXmlPart2StandingorderTypeJson convert(String source) {
        if (StringUtils.isEmpty(source)) {
            return null;
        }

        try {
            return xs2aObjectMapper.readValue(source, PeriodicPaymentInitiationXmlPart2StandingorderTypeJson.class);
        } catch (IOException e) {
            log.info("Can't convert json to object: {}", e.getMessage());
            return null;
        }
    }
}
