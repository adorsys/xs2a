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
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SpiToXs2aAccountReferenceMapper {

    public Optional<Xs2aAccountReference> mapToXs2aAccountReference(SpiAccountReference spiAccountRef) {
        return Optional.ofNullable(spiAccountRef)
                   .map(acc -> {
                       Xs2aAccountReference accRef = new Xs2aAccountReference();
                       accRef.setIban(spiAccountRef.getIban());
                       accRef.setBban(spiAccountRef.getBban());
                       accRef.setPan(spiAccountRef.getPan());
                       accRef.setMaskedPan(spiAccountRef.getMaskedPan());
                       accRef.setMsisdn(spiAccountRef.getMsisdn());
                       accRef.setCurrency(spiAccountRef.getCurrency());
                       return Optional.of(accRef);
                   }).orElseGet(Optional::empty);
    }
}
