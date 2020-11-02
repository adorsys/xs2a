/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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
