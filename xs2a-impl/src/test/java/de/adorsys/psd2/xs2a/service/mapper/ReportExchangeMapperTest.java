/*
 * Copyright 2018-2021 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.ReportExchangeRate;
import de.adorsys.psd2.model.ReportExchangeRateList;
import de.adorsys.psd2.xs2a.domain.Xs2aExchangeRate;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ReportExchangeMapperImpl.class})
class ReportExchangeMapperTest {
    @Autowired
    private ReportExchangeMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToReportExchangeRate_success() {
        Xs2aExchangeRate xs2aExchangeRate = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-xs2a-exchange-rate.json", Xs2aExchangeRate.class);
        ReportExchangeRate reportExchangeRate = mapper.mapToReportExchangeRate(xs2aExchangeRate);

        ReportExchangeRate expectedReportExchangeRate = jsonReader.getObjectFromFile("json/service/mapper/account-model-mapper/AccountModelMapper-report-exchange-rate-expected.json",
                                                                                     ReportExchangeRate.class);
        assertEquals(expectedReportExchangeRate, reportExchangeRate);

    }

    @Test
    void mapToReportExchangeRate_nullValue() {
        ReportExchangeRate reportExchangeRate = mapper.mapToReportExchangeRate(null);
        assertNull(reportExchangeRate);
    }

    @Test
    void mapToReportExchanges_nullValue() {
        ReportExchangeRateList reportExchangeRateList = mapper.mapToReportExchanges(null);
        assertNull(reportExchangeRateList);
    }

    @Test
    void mapToReportExchanges_emptyList() {
        ReportExchangeRateList reportExchangeRateList = mapper.mapToReportExchanges(Collections.emptyList());
        assertNull(reportExchangeRateList);
    }
}
