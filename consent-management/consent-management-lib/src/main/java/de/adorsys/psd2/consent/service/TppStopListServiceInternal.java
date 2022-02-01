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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.TppStopListService;
import de.adorsys.psd2.consent.domain.TppStopListEntity;
import de.adorsys.psd2.consent.repository.TppStopListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TppStopListServiceInternal implements TppStopListService {
    private final TppStopListRepository tppStopListRepository;

    @Value("${xs2a.cms.service.instance-id:UNDEFINED}")
    private String serviceInstanceId;

    @Override
    public CmsResponse<Boolean> checkIfTppBlocked(String tppAuthorisationNumber, String instanceId) {
        String requestedInstanceId = StringUtils.isBlank(instanceId) ? serviceInstanceId : instanceId;
        Optional<TppStopListEntity> stopListEntityOptional = tppStopListRepository.findByTppAuthorisationNumberAndInstanceId(tppAuthorisationNumber,
                                                                                                                             requestedInstanceId);

        Boolean blocked = stopListEntityOptional
                              .filter(TppStopListEntity::isBlocked)
                              .map(sl -> {
                                  log.info("TPP ID: [{}]. TPP has been blocked, because it's in stop list",
                                           tppAuthorisationNumber);
                                  return true;
                              })
                              .orElse(false);

        return CmsResponse.<Boolean>builder()
                   .payload(blocked)
                   .build();
    }
}
