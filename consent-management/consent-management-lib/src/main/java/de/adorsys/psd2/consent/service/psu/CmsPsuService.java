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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.domain.PsuData;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CmsPsuService {
    /**
     * Checks if the specified psuData is in the psu list, if it is true then returns an element from the list.
     * If psuData is absent then returns this psu
     *
     * @param psuDataForCheck psu which will search in the list
     * @param psuDataList     list where will search psuData
     * @return if the psuData is not in the psuDataList then will return this psuData otherwise return element form list
     */
    public Optional<PsuData> definePsuDataForAuthorisation(PsuData psuDataForCheck, List<PsuData> psuDataList) {
        return Optional.ofNullable(psuDataForCheck)
                   .map(psuData -> psuDataList.stream()
                                       .filter(psu -> StringUtils.equals(psu.getPsuId(), psuData.getPsuId()))
                                       .findFirst()
                                       .orElse(psuDataForCheck));
    }

    /**
     * Adds psuData to psuDataList if it is not there
     *
     * @param psuData     psu which will search in the list
     * @param psuDataList list where will search psuData
     * @return List with psuData
     */
    public List<PsuData> enrichPsuData(PsuData psuData, List<PsuData> psuDataList) {
        if (isPsuDataNew(psuData, psuDataList)) {
            psuDataList.add(psuData);
        }
        return psuDataList;
    }

    /**
     * Checks if the specified psuData is in the psu list
     *
     * @param psuData     psu which will search in the list
     * @param psuDataList list where will search psuData
     * @return if the psuData is not in the psuDataList then will return true otherwise return false
     */
    public boolean isPsuDataNew(PsuData psuData, List<PsuData> psuDataList) {
        return Optional.ofNullable(psuData)
                   .map(psu -> !isPsuDataInList(psu, psuDataList))
                   .orElseGet(() -> {
                       log.info("Check is psu data a new instance failed - psuData is null");
                       return false;
                   });
    }

    /**
     * Checks whether two specified lists of PSU Data are equals
     * <p>
     * This method ignores internal entity identifier of PsuData when comparing lists.
     *
     * @param psuDataList        the first list to be compared, must not be null
     * @param anotherPsuDataList the second list to be compared, must not be null
     * @return <code>true</code> if two lists are equal, <code>false</code> otherwise
     */
    public boolean isPsuDataListEqual(@NotNull List<PsuData> psuDataList, @NotNull List<PsuData> anotherPsuDataList) {
        if (psuDataList.size() != anotherPsuDataList.size()) {
            return false;
        }
        return psuDataList.stream()
                   .allMatch(psuData -> isPsuDataInList(psuData, anotherPsuDataList));
    }

    /**
     * Checks if the specified psu in request equals psu in authorisation
     *
     * @param psuRequest psu in request
     * @param psuAuth    psu in authorisation
     * @return true if psu in authorisation is null or equals psu in request
     */
    public boolean isPsuDataRequestCorrect(PsuData psuRequest, PsuData psuAuth) {
        return Optional.ofNullable(psuRequest)
                   .map(psu -> psuAuth == null || psu.contentEquals(psuAuth))
                   .orElse(false);
    }

    /**
     * Checks if the specified psuData is in the psu list
     *
     * @param psuData     psu which will search in the list
     * @param psuDataList list where will search psuData
     * @return if the psuData is not in the psuDataList then will return false otherwise will return true
     */
    private boolean isPsuDataInList(PsuData psuData, List<PsuData> psuDataList) {
        return psuData.isNotEmpty()
                   && psuDataList.stream()
                          .anyMatch(psuData::contentEquals);
    }
}
