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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PsuService {
    protected final CmsPsuService cmsPsuService;
    protected final PsuDataMapper psuDataMapper;

    public PsuData mapToPsuData(PsuIdData psuData, String instanceId) {
        return psuDataMapper.mapToPsuData(psuData, instanceId);
    }

    public Optional<PsuData> definePsuDataForAuthorisation(PsuData psuData, List<PsuData> psuDataList) {
        return cmsPsuService.definePsuDataForAuthorisation(psuData, psuDataList);
    }

    public List<PsuData> enrichPsuData(PsuData psuData, List<PsuData> psuDataList) {
        return cmsPsuService.enrichPsuData(psuData, psuDataList);
    }

    public boolean isPsuDataRequestCorrect(PsuData psuRequest, PsuData psuData) {
        return cmsPsuService.isPsuDataRequestCorrect(psuRequest, psuData);
    }
}
