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
import de.adorsys.psd2.xs2a.core.profile.AccountReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
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

    public List<AccountReference> mapToAccountReferenceList(List<AccountReferenceEntity> accountReferenceEntities) {
        return accountReferenceEntities.stream()
                   .map(this::mapToAccountReferenceEntity)
                   .collect(Collectors.toList());
    }

    private AccountReference mapToAccountReferenceEntity(AccountReferenceEntity accountReferenceEntity) {
        return Optional.ofNullable(accountReferenceEntity)
                   .map(ref -> {
                       AccountReference accountReference = new AccountReference();
                       accountReference.setIban(ref.getIban());
                       accountReference.setBban(ref.getBban());
                       accountReference.setPan(ref.getPan());
                       accountReference.setMaskedPan(ref.getMaskedPan());
                       accountReference.setMsisdn(ref.getMsisdn());
                       accountReference.setCurrency(ref.getCurrency());

                       return accountReference;
                   }).orElse(null);
    }

    public List<AccountReferenceEntity> mapToAccountReferenceEntityList(List<AccountReference> cmsAccountReferences) {
        return cmsAccountReferences.stream()
                   .map(this::mapToAccountReferenceEntity)
                   .collect(Collectors.toList());
    }

    private AccountReferenceEntity mapToAccountReferenceEntity(AccountReference accountReference) {
        return Optional.ofNullable(accountReference)
                   .map(ref -> {
                       AccountReferenceEntity accountReferenceEntity = new AccountReferenceEntity();
                       accountReferenceEntity.setIban(ref.getIban());
                       accountReferenceEntity.setBban(ref.getBban());
                       accountReferenceEntity.setPan(ref.getPan());
                       accountReferenceEntity.setMaskedPan(ref.getMaskedPan());
                       accountReferenceEntity.setMsisdn(ref.getMsisdn());
                       accountReferenceEntity.setCurrency(ref.getCurrency());

                       return accountReferenceEntity;
                   }).orElse(null);
    }
}
