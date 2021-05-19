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

import de.adorsys.psd2.core.data.ais.AisConsent;
import de.adorsys.psd2.xs2a.core.domain.ErrorHolder;
import de.adorsys.psd2.xs2a.core.domain.TppMessageInformation;
import de.adorsys.psd2.xs2a.core.error.ErrorType;
import de.adorsys.psd2.xs2a.core.error.MessageErrorCode;
import de.adorsys.psd2.xs2a.core.error.TppMessage;
import de.adorsys.psd2.xs2a.core.mapper.ServiceType;
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import de.adorsys.psd2.xs2a.domain.account.Xs2aBalancesReport;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aAisConsentMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiErrorMapper;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.SpiToXs2aBalanceReportMapper;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountBalance;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountConsent;
import de.adorsys.psd2.xs2a.spi.domain.account.SpiAccountReference;
import de.adorsys.psd2.xs2a.spi.domain.response.SpiResponse;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static de.adorsys.psd2.xs2a.core.error.MessageErrorCode.FORMAT_ERROR;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountMappersHolderTest {
    @Mock
    private SpiErrorMapper spiErrorMapper;

    @Mock
    private Xs2aAisConsentMapper xs2aAisConsentMapper;

    @Mock
    private SpiToXs2aBalanceReportMapper spiToXs2aBalanceReportMapper;

    @InjectMocks
    private AccountMappersHolder accountMappersHolder;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void mapToSpiAccountConsent() {
        AisConsent aisConsent = jsonReader.getObjectFromFile("json/service/mapper/consent/xs2a-account-consent.json", AisConsent.class);

        SpiAccountConsent expected = jsonReader.getObjectFromFile("json/service/mapper/consent/spi-account-consent.json", SpiAccountConsent.class);

        when(xs2aAisConsentMapper.mapToSpiAccountConsent(aisConsent)).thenReturn(expected);

        SpiAccountConsent actual = accountMappersHolder.mapToSpiAccountConsent(aisConsent);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToErrorHolder() {
        SpiResponse spiResponse = buildErrorSpiResponse(Collections.EMPTY_LIST);
        ErrorHolder expected = ErrorHolder.builder(ErrorType.AIS_400)
                                   .tppMessages(TppMessageInformation.of(MessageErrorCode.FORMAT_ERROR))
                                   .build();

        when(spiErrorMapper.mapToErrorHolder(spiResponse, ServiceType.AIS)).thenReturn(expected);

        ErrorHolder actual = accountMappersHolder.mapToErrorHolder(spiResponse, ServiceType.AIS);

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void mapToXs2aBalancesReportSpi() {
        SpiAccountReference spiAccountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json",
                                                                               SpiAccountReference.class);

        SpiAccountBalance spiAccountBalance = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-balance.json",
                                                                           SpiAccountBalance.class);

        List<SpiAccountBalance> balanceList = Collections.singletonList(spiAccountBalance);

        Xs2aBalancesReport expectedXs2aBalancesReport = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-balances-report.json",
                                                                                     Xs2aBalancesReport.class);

        when(spiToXs2aBalanceReportMapper.mapToXs2aBalancesReportSpi(spiAccountReference, balanceList)).thenReturn(expectedXs2aBalancesReport);

        Xs2aBalancesReport actual = accountMappersHolder.mapToXs2aBalancesReportSpi(spiAccountReference, balanceList);

        assertThat(actual).isEqualTo(expectedXs2aBalancesReport);
    }

    @Test
    void mapToXs2aBalancesReport() {
        AccountReference accountReference = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-reference.json",
                                                                         AccountReference.class);

        SpiAccountBalance spiAccountBalance = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/spi-account-balance.json",
                                                                           SpiAccountBalance.class);

        List<SpiAccountBalance> balanceList = Collections.singletonList(spiAccountBalance);

        Xs2aBalancesReport expectedXs2aBalancesReport = jsonReader.getObjectFromFile("json/service/mapper/spi_xs2a_mappers/xs2a-balances-report.json",
                                                                                     Xs2aBalancesReport.class);

        when(spiToXs2aBalanceReportMapper.mapToXs2aBalancesReport(accountReference, balanceList)).thenReturn(expectedXs2aBalancesReport);

        Xs2aBalancesReport actual = accountMappersHolder.mapToXs2aBalancesReport(accountReference, balanceList);

        assertThat(actual).isEqualTo(expectedXs2aBalancesReport);
    }

    private <T> SpiResponse<T> buildErrorSpiResponse(T payload) {
        return SpiResponse.<T>builder()
                   .payload(payload)
                   .error(new TppMessage(FORMAT_ERROR))
                   .build();
    }
}
