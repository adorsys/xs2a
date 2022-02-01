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

import de.adorsys.psd2.consent.aspsp.api.CmsAspspPsuConsentManagementApi;
import de.adorsys.psd2.consent.aspsp.api.psu.CmsAspspPsuAccountService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CmsAspspPsuConsentManagementController implements CmsAspspPsuConsentManagementApi {
    private final CmsAspspPsuAccountService cmsAspspPsuAccountService;

    @Override
    public ResponseEntity<Boolean> closeAllConsents(String aspspAccountId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        PsuIdData psuIdData = getPsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType);

        if (StringUtils.isBlank(aspspAccountId)
                && psuIdData.isEmpty()) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }

        boolean result = cmsAspspPsuAccountService.revokeAllConsents(aspspAccountId, psuIdData, instanceId);
        return new ResponseEntity<>(result, result ? HttpStatus.OK : HttpStatus.BAD_REQUEST);
    }

    private PsuIdData getPsuIdData(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType) {
        return new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
    }
}
