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

package de.adorsys.aspsp.xs2a.service.mapper.consent;

import de.adorsys.aspsp.xs2a.domain.Xs2aConsentData;
import de.adorsys.psd2.xs2a.spi.domain.consent.AspspConsentData;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
public class AspspConsentDataMapper {

    public AspspConsentData mapToAspspConsentData(Xs2aConsentData xs2aConsentData) {
        byte[] aspspConsentData = Optional.ofNullable(xs2aConsentData.getAspspConsentDataBase64())
                                      .map(s -> Base64.getDecoder().decode(s))
                                      .orElse(null);
        return new AspspConsentData(aspspConsentData, xs2aConsentData.getConsentId());
    }
}
