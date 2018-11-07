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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.ais.CmsAccountReference;
import de.adorsys.psd2.consent.domain.AccountReferenceEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AccountReferenceMapper {
    CmsAccountReference mapToCmsAccountReference(AccountReferenceEntity accountReferenceEntity) {
        return Optional.ofNullable(accountReferenceEntity)
                   .map(ref -> new CmsAccountReference(null,
                                                       ref.getIban(),
                                                       ref.getBban(),
                                                       ref.getPan(),
                                                       ref.getMaskedPan(),
                                                       ref.getMsisdn(),
                                                       ref.getCurrency())
                   ).orElse(null);
    }

    AccountReferenceEntity mapToAccountReferenceEntity(CmsAccountReference cmsAccountReference) {
        return Optional.ofNullable(cmsAccountReference)
                   .map(ref -> {
                       AccountReferenceEntity accountReferenceEntity = new AccountReferenceEntity();
                       accountReferenceEntity.setIban(cmsAccountReference.getIban());
                       accountReferenceEntity.setBban(cmsAccountReference.getBban());
                       accountReferenceEntity.setPan(cmsAccountReference.getPan());
                       accountReferenceEntity.setMaskedPan(cmsAccountReference.getMaskedPan());
                       accountReferenceEntity.setMsisdn(cmsAccountReference.getMsisdn());
                       accountReferenceEntity.setCurrency(cmsAccountReference.getCurrency());

                       return accountReferenceEntity;
                   }).orElse(null);
    }
}
