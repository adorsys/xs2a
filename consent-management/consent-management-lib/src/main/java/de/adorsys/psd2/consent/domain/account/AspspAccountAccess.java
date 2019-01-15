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

package de.adorsys.psd2.consent.domain.account;

import de.adorsys.psd2.consent.api.TypeAccess;
import de.adorsys.psd2.xs2a.core.profile.AccountReferenceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Currency;

@Getter
@Setter
@Embeddable
@NoArgsConstructor
@ApiModel(description = "Aspsp Account access", value = "AspspAccountAccess")
public class AspspAccountAccess extends AccountAccess {

    @Column(name = "resource_id", length = 100)
    @ApiModelProperty(value = "RESOURCE-ID: This identification is denoting the addressed account.")
    private String resourceId;

    @Column(name = "aspsp_account_id", length = 100)
    @ApiModelProperty(value = "Aspsp-Account-ID: Bank specific account ID", example = "26bb59a3-2f63-4027-ad38-67d87e59611a")
    private String aspspAccountId;

    public AspspAccountAccess(String accountIdentifier, TypeAccess typeAccess, AccountReferenceType accountReferenceType, Currency currency, String resourceId, String aspspAccountId) {
        super(accountIdentifier, typeAccess, accountReferenceType, currency);
        this.resourceId = resourceId;
        this.aspspAccountId = aspspAccountId;
    }
}
