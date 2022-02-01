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
