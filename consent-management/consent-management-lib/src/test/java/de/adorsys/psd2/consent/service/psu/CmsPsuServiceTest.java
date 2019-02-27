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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.domain.PsuData;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CmsPsuServiceTest {
    private static final String PSU_ID_1 = "psu id 1";
    private static final String PSU_ID_2 = "psu id 2";
    private static final String PSU_ID_3 = "psu id 3";
    private static final String PSU_ID_TYPE = "psu type";
    private static final String PSU_CORPORATE_ID = "corp id";
    private static final String PSU_CORPORATE_ID_TYPE = "corp type";

    private final PsuData PSU_DATA_1 = new PsuData(PSU_ID_1, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
    private final PsuData PSU_DATA_2 = new PsuData(PSU_ID_2, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
    private final PsuData PSU_DATA_3 = new PsuData(PSU_ID_3, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
    private final List<PsuData> PSU_DATA_LIST = new ArrayList<>(Arrays.asList(PSU_DATA_1, PSU_DATA_2));

    @InjectMocks
    CmsPsuService cmsPsuService;

    @Test
    public void definePsuDataForAuthorisation_PsuIsExistInList() {
        Optional<PsuData> actualResult = cmsPsuService.definePsuDataForAuthorisation(PSU_DATA_1, PSU_DATA_LIST);

        assertThat(actualResult.isPresent()).isTrue();
        assertThat(actualResult.get()).isEqualTo(PSU_DATA_LIST.get(0));
    }

    @Test
    public void definePsuDataForAuthorisation_PsuIsNotExistInList() {
        Optional<PsuData> actualResult = cmsPsuService.definePsuDataForAuthorisation(PSU_DATA_3, PSU_DATA_LIST);

        assertThat(actualResult.isPresent()).isTrue();
        assertThat(actualResult.get()).isEqualTo(PSU_DATA_3);
    }

    @Test
    public void enrichPsuData_PsuIsExistInList() {
        List<PsuData> actualResult = cmsPsuService.enrichPsuData(PSU_DATA_1, PSU_DATA_LIST);

        assertThat(actualResult.size()).isEqualTo(2);
        assertThat(actualResult.containsAll(Arrays.asList(PSU_DATA_1, PSU_DATA_2))).isTrue();
    }

    @Test
    public void enrichPsuData_PsuIsNotExistInList() {
        List<PsuData> actualResult = cmsPsuService.enrichPsuData(PSU_DATA_3, PSU_DATA_LIST);

        assertThat(actualResult.size()).isEqualTo(3);
        assertThat(actualResult.containsAll(Arrays.asList(PSU_DATA_1, PSU_DATA_2, PSU_DATA_3))).isTrue();
    }

    @Test
    public void isPsuDataNew_PsuIsExistInList() {
        boolean actualResult = cmsPsuService.isPsuDataNew(PSU_DATA_1, PSU_DATA_LIST);

        assertThat(actualResult).isFalse();
    }

    @Test
    public void isPsuDataNew_PsuIsNull() {
        boolean actualResult = cmsPsuService.isPsuDataNew(null, PSU_DATA_LIST);

        assertThat(actualResult).isFalse();
    }

    @Test
    public void isPsuDataNew_PsuIsNotExistInList() {
        boolean actualResult = cmsPsuService.isPsuDataNew(PSU_DATA_3, PSU_DATA_LIST);

        assertThat(actualResult).isTrue();
    }

    @Test
    public void isPsuDataListEqual_shouldReturnTrue_sameList() {
        boolean actualResult = cmsPsuService.isPsuDataListEqual(PSU_DATA_LIST, PSU_DATA_LIST);

        assertThat(actualResult).isTrue();
    }

    @Test
    public void isPsuDataListEqual_shouldReturnTrue_sameContentExceptId() {
        List<PsuData> psuDataList = Arrays.asList(buildPsuDataWithId(PSU_ID_1, 1L), buildPsuDataWithId(PSU_ID_2, 2L));
        List<PsuData> anotherPsuDataList = Arrays.asList(buildPsuDataWithId(PSU_ID_1, 3L), buildPsuDataWithId(PSU_ID_2, 4L));

        boolean actualResult = cmsPsuService.isPsuDataListEqual(psuDataList, anotherPsuDataList);

        assertThat(actualResult).isTrue();
    }

    @Test
    public void isPsuDataListEqual_shouldReturnTrue_emptyLists() {
        boolean actualResult = cmsPsuService.isPsuDataListEqual(Collections.emptyList(), Collections.emptyList());

        assertThat(actualResult).isTrue();
    }

    @Test
    public void isPsuDataListEqual_shouldReturnFalse_differentPsuIds() {
        List<PsuData> psuDataList = Arrays.asList(PSU_DATA_1, PSU_DATA_2);
        List<PsuData> anotherPsuDataList = Arrays.asList(PSU_DATA_2, PSU_DATA_3);

        boolean actualResult = cmsPsuService.isPsuDataListEqual(psuDataList, anotherPsuDataList);

        assertThat(actualResult).isFalse();
    }

    @Test
    public void isPsuDataListEqual_shouldReturnFalse_oneEmptyList() {
        boolean actualResult = cmsPsuService.isPsuDataListEqual(PSU_DATA_LIST, Collections.emptyList());

        assertThat(actualResult).isFalse();
    }

    private PsuData buildPsuDataWithId(String psuId, Long databaseId) {
        PsuData newPsuData = new PsuData(psuId, PSU_ID_TYPE, PSU_CORPORATE_ID, PSU_CORPORATE_ID_TYPE);
        newPsuData.setId(databaseId);
        return newPsuData;
    }
}
