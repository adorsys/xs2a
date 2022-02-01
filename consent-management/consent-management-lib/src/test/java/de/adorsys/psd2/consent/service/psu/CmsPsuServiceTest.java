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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CmsPsuServiceTest {
    private static final String PSU_ID_1 = "psu id 1";
    private static final String PSU_ID_2 = "psu id 2";
    private static final String PSU_ID_3 = "psu id 3";
    private static final String PSU_ID_TYPE = "psu type";
    private static final String PSU_CORPORATE_ID = "corp id";
    private static final String PSU_CORPORATE_ID_TYPE = "corp type";
    private static final String PSU_IP_ADDRESS = "ip address";

    private final PsuData PSU_DATA_1 = new PsuData(PSU_ID_1, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
    private final PsuData PSU_DATA_2 = new PsuData(PSU_ID_2, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
    private final PsuData PSU_DATA_3 = new PsuData(PSU_ID_3, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
    private final List<PsuData> PSU_DATA_LIST = new ArrayList<>(Arrays.asList(PSU_DATA_1, PSU_DATA_2));

    @InjectMocks
    CmsPsuService cmsPsuService;

    @Test
    void definePsuDataForAuthorisation_PsuIsExistInList() {
        Optional<PsuData> actualResult = cmsPsuService.definePsuDataForAuthorisation(PSU_DATA_1, PSU_DATA_LIST);

        assertTrue(actualResult.isPresent());
        assertEquals(PSU_DATA_LIST.get(0), actualResult.get());
    }

    @Test
    void definePsuDataForAuthorisation_PsuIsNotExistInList() {
        Optional<PsuData> actualResult = cmsPsuService.definePsuDataForAuthorisation(PSU_DATA_3, PSU_DATA_LIST);

        assertTrue(actualResult.isPresent());
        assertEquals(PSU_DATA_3, actualResult.get());
    }

    @Test
    void enrichPsuData_PsuIsExistInList() {
        List<PsuData> actualResult = cmsPsuService.enrichPsuData(PSU_DATA_1, PSU_DATA_LIST);

        assertEquals(2, actualResult.size());
        assertTrue(actualResult.containsAll(Arrays.asList(PSU_DATA_1, PSU_DATA_2)));
    }

    @Test
    void enrichPsuData_PsuIsNotExistInList() {
        List<PsuData> actualResult = cmsPsuService.enrichPsuData(PSU_DATA_3, PSU_DATA_LIST);

        assertEquals(3, actualResult.size());
        assertTrue(actualResult.containsAll(Arrays.asList(PSU_DATA_1, PSU_DATA_2, PSU_DATA_3)));
    }

    @Test
    void isPsuDataNew_PsuIsExistInList() {
        boolean actualResult = cmsPsuService.isPsuDataNew(PSU_DATA_1, PSU_DATA_LIST);

        assertFalse(actualResult);
    }

    @Test
    void isPsuDataNew_PsuIsNull() {
        boolean actualResult = cmsPsuService.isPsuDataNew(null, PSU_DATA_LIST);

        assertFalse(actualResult);
    }

    @Test
    void isPsuDataNew_PsuIsNotExistInList() {
        boolean actualResult = cmsPsuService.isPsuDataNew(PSU_DATA_3, PSU_DATA_LIST);

        assertTrue(actualResult);
    }

    @Test
    void isPsuDataListEqual_shouldReturnTrue_sameList() {
        boolean actualResult = cmsPsuService.isPsuDataListEqual(PSU_DATA_LIST, PSU_DATA_LIST);

        assertTrue(actualResult);
    }

    @Test
    void isPsuDataListEqual_shouldReturnTrue_sameContentExceptId() {
        List<PsuData> psuDataList = Arrays.asList(buildPsuDataWithId(PSU_ID_1, 1L), buildPsuDataWithId(PSU_ID_2, 2L));
        List<PsuData> anotherPsuDataList = Arrays.asList(buildPsuDataWithId(PSU_ID_1, 3L), buildPsuDataWithId(PSU_ID_2, 4L));

        boolean actualResult = cmsPsuService.isPsuDataListEqual(psuDataList, anotherPsuDataList);

        assertTrue(actualResult);
    }

    @Test
    void isPsuDataListEqual_shouldReturnTrue_emptyLists() {
        boolean actualResult = cmsPsuService.isPsuDataListEqual(Collections.emptyList(), Collections.emptyList());

        assertTrue(actualResult);
    }

    @Test
    void isPsuDataListEqual_shouldReturnFalse_differentPsuIds() {
        List<PsuData> psuDataList = Arrays.asList(PSU_DATA_1, PSU_DATA_2);
        List<PsuData> anotherPsuDataList = Arrays.asList(PSU_DATA_2, PSU_DATA_3);

        boolean actualResult = cmsPsuService.isPsuDataListEqual(psuDataList, anotherPsuDataList);

        assertFalse(actualResult);
    }

    @Test
    void isPsuDataListEqual_shouldReturnFalse_oneEmptyList() {
        boolean actualResult = cmsPsuService.isPsuDataListEqual(PSU_DATA_LIST, Collections.emptyList());

        assertFalse(actualResult);
    }

    private PsuData buildPsuDataWithId(String psuId, Long databaseId) {
        PsuData newPsuData = new PsuData(psuId, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE, PSU_IP_ADDRESS);
        newPsuData.setId(databaseId);
        return newPsuData;
    }
}
