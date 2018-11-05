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

package de.adorsys.aspsp.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.aspsp.xs2a.domain.OtpFormat;
import de.adorsys.psd2.xs2a.spi.domain.payment.SpiOtpFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by cbr on 31.10.18.
 */
@Component
@RequiredArgsConstructor
public class SpiToXs2aOtpFormatMapper {
    public OtpFormat mapToOtpFormat(SpiOtpFormat otpFormat) {
        return Optional.ofNullable(otpFormat)
            .map(SpiOtpFormat::name)
            .map(OtpFormat::valueOf)
            .orElse(null);
    }
}
