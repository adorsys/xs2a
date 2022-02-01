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

package de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {SpiToXs2aBalanceReportMapperImpl.class, SpiToXs2aBalanceMapperImpl.class,
    SpiToXs2aAccountReferenceMapperImpl.class, SpiToXs2aAmountMapperImpl.class})
class SpiToXs2aBalanceReportMapperTest {

    @Autowired
    private SpiToXs2aBalanceReportMapper mapper;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToXs2aBalancesReportSpi() {
        //Given
        SpiAccountBalance spiAccountBalance =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-balance.json",
                SpiAccountBalance.class);
        SpiAccountReference spiAccountReference =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json",
                SpiAccountReference.class);
        Xs2aBalancesReport expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-balances-report.json",
                Xs2aBalancesReport.class);

        //When
        Xs2aBalancesReport actual = mapper.mapToXs2aBalancesReportSpi(spiAccountReference, Collections.singletonList(spiAccountBalance));

        //Then
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToXs2aBalancesReportSpi_nullValue() {
        //When
        Xs2aBalancesReport actual = mapper.mapToXs2aBalancesReportSpi(null, null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aBalancesReport_nullValue() {
        //When
        Xs2aBalancesReport actual = mapper.mapToXs2aBalancesReport(null, null);

        //Then
        assertThat(actual).isNull();
    }

    @Test
    void mapToXs2aBalancesReport() {
        //Given
        SpiAccountBalance spiAccountBalance =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-balance.json",
                SpiAccountBalance.class);
        AccountReference accountReference =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json",
                AccountReference.class);
        Xs2aBalancesReport expected =
            jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-balances-report.json",
                Xs2aBalancesReport.class);

        //When
        Xs2aBalancesReport actual = mapper.mapToXs2aBalancesReport(accountReference, Collections.singletonList(spiAccountBalance));

        //Then
        assertThat(actual).isEqualTo(expected);
    }
}
