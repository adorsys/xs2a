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
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class AccountMappersHolder {
    private final SpiToXs2aBalanceReportMapper balanceReportMapper;
    private final Xs2aAisConsentMapper consentMapper;
    private final SpiErrorMapper spiErrorMapper;

    public SpiAccountConsent mapToSpiAccountConsent(AisConsent aisConsent) {
        return consentMapper.mapToSpiAccountConsent(aisConsent);
    }

    public ErrorHolder mapToErrorHolder(SpiResponse<List<SpiAccountBalance>> spiResponse, ServiceType serviceType) {
        return spiErrorMapper.mapToErrorHolder(spiResponse, serviceType);
    }

    public Xs2aBalancesReport mapToXs2aBalancesReportSpi(SpiAccountReference spiAccountReference,
                                                         List<SpiAccountBalance> spiAccountBalances) {
        return balanceReportMapper.mapToXs2aBalancesReportSpi(spiAccountReference, spiAccountBalances);
    }

    public Xs2aBalancesReport mapToXs2aBalancesReport(AccountReference accountReference, List<SpiAccountBalance> spiAccountBalances) {
        return balanceReportMapper.mapToXs2aBalancesReport(accountReference, spiAccountBalances);
    }
}
