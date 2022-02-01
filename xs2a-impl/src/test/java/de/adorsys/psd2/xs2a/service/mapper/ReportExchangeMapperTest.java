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
