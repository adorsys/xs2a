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

package de.adorsys.psd2.xs2a.service.consent;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.service.PisCommonPaymentServiceEncrypted;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PisPsuDataService {
    private final PisCommonPaymentServiceEncrypted pisCommonPaymentServiceEncrypted;

    public List<PsuIdData> getPsuDataByPaymentId(String paymentId) {
        CmsResponse<List<PsuIdData>> cmsResponse = pisCommonPaymentServiceEncrypted.getPsuDataListByPaymentId(paymentId);

        if (cmsResponse.hasError()) {
            log.info("Payment-ID [{}]. Can't get PsuData by payment ID because PsuData list not found by id at cms.",
                     paymentId);
            return Collections.emptyList();
        }

        return cmsResponse.getPayload();
    }
}
