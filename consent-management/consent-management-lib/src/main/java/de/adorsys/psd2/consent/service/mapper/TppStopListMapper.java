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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class TppStopListMapper {

    public TppStopListRecord mapToTppStopListRecord(TppStopListEntity tppStopListEntity) {
        return Optional.ofNullable(tppStopListEntity)
                   .map(entity -> {
                       TppStopListRecord record = new TppStopListRecord();
                       record.setTppAuthorisationNumber(entity.getTppAuthorisationNumber());
                       record.setNationalAuthorityId(entity.getNationalAuthorityId());
                       record.setBlockingExpirationTimestamp(entity.getBlockingExpirationTimestamp());
                       record.setStatus(entity.getStatus());
                       return record;
                   })
                   .orElse(null);
    }
}
