/*
 * Copyright 2018-2020 adorsys GmbH & Co KG
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

package de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers;

import de.adorsys.psd2.xs2a.core.authorisation.AccountConsentAuthorization;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class Xs2aAccountConsentAuthorizationMapper {

    List<AccountConsentAuthorization> mapToAccountConsentAuthorisation(List<Authorisation> authorisations) {
        if (CollectionUtils.isEmpty(authorisations)) {
            return Collections.emptyList();
        }
        return authorisations.stream()
                   .map(this::mapToAccountConsentAuthorisation)
                   .collect(Collectors.toList());
    }

    private AccountConsentAuthorization mapToAccountConsentAuthorisation(Authorisation authorisation) {
        return Optional.ofNullable(authorisation)
                   .map(auth -> {
                       AccountConsentAuthorization accountConsentAuthorisation = new AccountConsentAuthorization();
                       accountConsentAuthorisation.setId(auth.getAuthorisationId());
                       accountConsentAuthorisation.setConsentId(auth.getParentId());
                       accountConsentAuthorisation.setPsuIdData(auth.getPsuIdData());
                       accountConsentAuthorisation.setScaStatus(auth.getScaStatus());
                       accountConsentAuthorisation.setPassword(auth.getPassword());
                       accountConsentAuthorisation.setChosenScaApproach(auth.getChosenScaApproach());
                       accountConsentAuthorisation.setAuthenticationMethodId(auth.getAuthenticationMethodId());
                       accountConsentAuthorisation.setScaAuthenticationData(auth.getScaAuthenticationData());
                       return accountConsentAuthorisation;
                   })
                   .orElse(null);
    }
}
