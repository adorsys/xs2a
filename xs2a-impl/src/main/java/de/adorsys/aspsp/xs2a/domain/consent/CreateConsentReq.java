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

package de.adorsys.aspsp.xs2a.domain.consent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.adorsys.aspsp.xs2a.domain.AccountReferenceCollector;
import de.adorsys.aspsp.xs2a.domain.account.AccountReference;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Data
@ApiModel(description = "Request creates an account information consent resource at the ASPSP regarding access to accounts specified in this request")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreateConsentReq implements AccountReferenceCollector {

    @ApiModelProperty(value = "Requested access services.", required = true)
    @NotNull
    private AccountAccess access;

    @ApiModelProperty(value = "'true', if the consent is for recurring access to the account data , 'false', if the consent is for one access to the account data", required = true)
    @NotNull
    private boolean recurringIndicator;

    @ApiModelProperty(value = "This parameter is requesting a valid until date for the requested consent. The content is the local ASPSP date in ISODate Format", required = true, example = "2017-10-30")
    @NotNull
    private LocalDate validUntil;

    @ApiModelProperty(value = "This field indicates the requested maximum frequency for an access per day. For a once-off access, this attribute is set to 1", required = true, example = "4")
    @NotNull
    private int frequencyPerDay;

    @ApiModelProperty(value = "If 'true' indicates that a payment initiation service will be addressed in the same 'session'", required = true)
    @NotNull
    private boolean combinedServiceIndicator;

    @JsonIgnore
    @Override
    public Set<AccountReference> getAccountReferences() {
        return getReferenceSet(this.access.getAccounts(), this.access.getBalances(), this.access.getTransactions());
    }

    @JsonIgnore
    @SafeVarargs
    private final Set<AccountReference> getReferenceSet(List<AccountReference>... referencesList) {
        return Arrays.stream(referencesList)
                   .map(this::getReferenceList)
                   .flatMap(Collection::stream)
                   .collect(Collectors.toSet());
    }

    @JsonIgnore
    private List<AccountReference> getReferenceList(List<AccountReference> reference) {
        return Optional.ofNullable(reference)
                   .orElse(Collections.emptyList());
    }
}
