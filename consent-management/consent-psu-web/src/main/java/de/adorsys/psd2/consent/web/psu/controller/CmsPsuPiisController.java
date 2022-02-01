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

package de.adorsys.psd2.consent.web.psu.controller;

import de.adorsys.psd2.consent.api.piis.v1.CmsPiisConsent;
import de.adorsys.psd2.consent.psu.api.CmsPsuPiisApi;
import de.adorsys.psd2.consent.psu.api.CmsPsuPiisService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class CmsPsuPiisController implements CmsPsuPiisApi {
    private final CmsPsuPiisService cmsPsuPiisService;

    @Override
    public ResponseEntity<CmsPiisConsent> getConsent(String consentId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        return cmsPsuPiisService.getConsent(psuIdData, consentId, instanceId)
                   .map(con -> new ResponseEntity<>(con, HttpStatus.OK))
                   .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @Override
    public ResponseEntity<List<CmsPiisConsent>> getConsentsForPsu(String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId, Integer pageIndex, Integer itemsPerPage) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        return new ResponseEntity<>(cmsPsuPiisService.getConsentsForPsu(psuIdData, instanceId, pageIndex, itemsPerPage), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Boolean> revokeConsent(String consentId, String psuId, String psuIdType, String psuCorporateId, String psuCorporateIdType, String instanceId) {
        PsuIdData psuIdData = new PsuIdData(psuId, psuIdType, psuCorporateId, psuCorporateIdType, null);
        return new ResponseEntity<>(cmsPsuPiisService.revokeConsent(psuIdData, consentId, instanceId), HttpStatus.OK);
    }
}
