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

package de.adorsys.psd2.xs2a.spi.domain.account;

import de.adorsys.psd2.xs2a.spi.domain.common.SpiAmount;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Currency;
import java.util.List;

@Data
@AllArgsConstructor
public class SpiCardAccountDetails {
    /**
     * Bank specific account identifier. Should not be provided for TPP (for these purposes resourceId should be used)
     */
    private String aspspAccountId;

    private String resourceId;
    private String maskedPan;
    private Currency currency;
    private String name;
    private String displayName;
    private String product;
    private SpiAccountStatus spiAccountStatus;
    private SpiAccountType cashSpiAccountType;
    private SpiUsageType usageType;
    private String details;
    private SpiAmount creditLimit;
    private List<SpiAccountBalance> balances;
    private String ownerName;
    private Boolean debitAccounting;
}
