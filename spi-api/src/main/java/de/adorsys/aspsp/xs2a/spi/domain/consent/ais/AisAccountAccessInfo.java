/*
 * Copyright 2018-2018 adorsys GmbH & Co KG
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

package de.adorsys.aspsp.xs2a.spi.domain.consent.ais;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import de.adorsys.aspsp.xs2a.spi.domain.consent.SpiAccountAccessType;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AisAccountAccessInfo {
    private List<AccountInfo> accounts;
    private List<AccountInfo> balances;
    private List<AccountInfo> transactions;
    private SpiAccountAccessType availableAccounts;
    private SpiAccountAccessType allPsd2;

    public boolean isAllAccountAccess(){
        return isAllPsd2() || isAvailableAccounts();
    }

    public boolean isAllPsd2(){
        return SpiAccountAccessType.ALL_ACCOUNTS == allPsd2;
    }

    public boolean isAvailableAccounts(){
        return SpiAccountAccessType.ALL_ACCOUNTS == availableAccounts;
    }
}
