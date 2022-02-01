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

package de.adorsys.psd2.consent.web.aspsp.controller;

import de.adorsys.psd2.consent.aspsp.api.CmsAspspStopListApi;
import de.adorsys.psd2.consent.aspsp.api.tpp.CmsAspspTppService;
import de.adorsys.psd2.xs2a.core.tpp.TppStopListRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
public class CmsAspspStopListController implements CmsAspspStopListApi {
    private final CmsAspspTppService cmsAspspTppService;

    @Override
    public ResponseEntity<TppStopListRecord> getTppStopListRecord(String tppAuthorisationNumber, String instanceId) {
        return cmsAspspTppService.getTppStopListRecord(tppAuthorisationNumber, instanceId)
                   .map(tppStopListRecord -> new ResponseEntity<>(tppStopListRecord, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<Boolean> blockTpp(String tppAuthorisationNumber, String instanceId, Long lockPeriod) {
        Duration lockPeriodDuration = lockPeriod != null ? Duration.ofMillis(lockPeriod) : null;
        boolean isBlocked = cmsAspspTppService.blockTpp(tppAuthorisationNumber, instanceId, lockPeriodDuration);
        return new ResponseEntity<>(isBlocked, HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> unblockTpp(String tppAuthorisationNumber, String instanceId) {
        boolean isUnblocked = cmsAspspTppService.unblockTpp(tppAuthorisationNumber, instanceId);
        return new ResponseEntity<>(isUnblocked, HttpStatus.OK);
    }
}
