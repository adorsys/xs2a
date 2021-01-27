/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

import de.adorsys.psd2.consent.api.AspspDataService;
import de.adorsys.psd2.consent.api.CmsAspspConsentDataBase64;
import de.adorsys.psd2.consent.psu.api.AspspConsentDataPsuApi;
import de.adorsys.psd2.xs2a.core.consent.AspspConsentData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class AspspConsentDataPsuApiController implements AspspConsentDataPsuApi {
    private final AspspDataService aspspDataService;

    @Override
    public ResponseEntity<CmsAspspConsentDataBase64> getAspspConsentData(String encryptedConsentId) {
        return aspspDataService.readAspspConsentData(encryptedConsentId)
            .map(AspspConsentData::getAspspConsentDataBytes)
            .map(Base64.getEncoder()::encodeToString)
            .map(aspspConsentDataBase64 -> new CmsAspspConsentDataBase64(encryptedConsentId, aspspConsentDataBase64))
            .map(cmsAspspConsentDataBase64 -> new ResponseEntity<>(cmsAspspConsentDataBase64, HttpStatus.OK))
            .orElseGet(ResponseEntity.notFound()::build);
    }

    @Override
    public ResponseEntity<Void> updateAspspConsentData(String encryptedConsentId, CmsAspspConsentDataBase64 request) {
        byte[] data = Optional.ofNullable(request.getAspspConsentDataBase64())
            .map(Base64.getDecoder()::decode)
            .orElse(null);

        AspspConsentData aspspConsentData = new AspspConsentData(data, encryptedConsentId);
        boolean updated = aspspDataService.updateAspspConsentData(aspspConsentData);

        if (!updated) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> deleteAspspConsentData(String encryptedConsentId) {
        boolean deleted = aspspDataService.deleteAspspConsentData(encryptedConsentId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
