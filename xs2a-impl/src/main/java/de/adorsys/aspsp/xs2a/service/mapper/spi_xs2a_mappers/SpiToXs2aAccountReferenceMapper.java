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

import de.adorsys.aspsp.xs2a.domain.account.Xs2aAccountReference;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountReference;
import org.springframework.stereotype.Component;

@Component
public class SpiToXs2aAccountReferenceMapper {

    public Xs2aAccountReference mapToXs2aAccountReference(SpiAccountReference spiReference) {
        Xs2aAccountReference reference = new Xs2aAccountReference();

        reference.setIban(spiReference.getIban());
        reference.setBban(spiReference.getBban());
        reference.setPan(spiReference.getPan());
        reference.setMaskedPan(spiReference.getMaskedPan());
        reference.setMsisdn(spiReference.getMsisdn());
        reference.setCurrency(spiReference.getCurrency());

        return reference;
    }
}
