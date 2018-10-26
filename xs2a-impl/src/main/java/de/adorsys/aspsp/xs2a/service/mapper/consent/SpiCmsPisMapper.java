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

import de.adorsys.aspsp.xs2a.domain.consent.pis.Xs2aUpdatePisConsentPsuDataRequest;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaConfirmation;
import de.adorsys.psd2.xs2a.spi.domain.authorisation.SpiScaMethod;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated since 1.10. Will be removed in 1.11.
 * This class should not exist, since no dependencies between spi-api and consent-api are allowed.
 */
@Component
@AllArgsConstructor
@Deprecated
public class SpiCmsPisMapper {

    public SpiScaConfirmation buildSpiScaConfirmation(Xs2aUpdatePisConsentPsuDataRequest request, String consentId) {
        SpiScaConfirmation paymentConfirmation = new SpiScaConfirmation();
        paymentConfirmation.setPaymentId(request.getPaymentId());
        paymentConfirmation.setTanNumber(request.getScaAuthenticationData());
        paymentConfirmation.setConsentId(consentId);
        paymentConfirmation.setPsuId(request.getPsuId());
        return paymentConfirmation;
    }

    public List<CmsScaMethod> mapToCmsScaMethods(List<SpiScaMethod> spiScaMethods) {
        return spiScaMethods.stream()
                   .map(this::mapToCmsScaMethod)
                   .collect(Collectors.toList());
    }

    private CmsScaMethod mapToCmsScaMethod(@NotNull SpiScaMethod spiScaMethod) {
        return CmsScaMethod.valueOf(spiScaMethod.name());
    }
}
