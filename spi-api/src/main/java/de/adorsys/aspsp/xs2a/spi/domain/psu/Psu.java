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

package de.adorsys.aspsp.xs2a.spi.domain.psu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.adorsys.aspsp.xs2a.spi.domain.account.SpiAccountDetails;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;

import java.util.List;

@AllArgsConstructor
@Data
public class Psu {
    @Id
    private String id;
    private String email;
    private List<SpiAccountDetails> accountDetailsList;

    @JsonIgnore
    public boolean isValid() {
        return StringUtils.isNotBlank(this.email) && this.email.contains("@")
                   && CollectionUtils.isNotEmpty(this.accountDetailsList)
                   && StringUtils.isNotBlank(this.accountDetailsList.get(0).getIban());
    }
}
