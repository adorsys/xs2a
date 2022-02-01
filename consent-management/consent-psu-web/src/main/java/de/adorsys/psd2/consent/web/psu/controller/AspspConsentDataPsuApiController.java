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
