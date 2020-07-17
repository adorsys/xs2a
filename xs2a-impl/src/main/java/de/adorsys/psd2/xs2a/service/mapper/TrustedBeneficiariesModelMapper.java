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

package de.adorsys.psd2.xs2a.service.mapper;

import de.adorsys.psd2.model.TrustedBeneficiariesList;
import de.adorsys.psd2.model.TrustedBeneficiary;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiariesList;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapper;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {Xs2aAddressMapper.class})
public abstract class TrustedBeneficiariesModelMapper {

    public TrustedBeneficiariesList mapToTrustedBeneficiariesList(Xs2aTrustedBeneficiariesList xs2ATrustedBeneficiariesList) {
        List<Xs2aTrustedBeneficiaries> trustedBeneficiaries = xs2ATrustedBeneficiariesList.getTrustedBeneficiaries();

        List<TrustedBeneficiary> beneficiaries = trustedBeneficiaries.stream()
                                                     .map(this::mapToTrustedBeneficiaries)
                                                     .collect(Collectors.toList());

        TrustedBeneficiariesList result = new TrustedBeneficiariesList();
        result.addAll(beneficiaries);
        return result;
    }

    public abstract TrustedBeneficiary mapToTrustedBeneficiaries(Xs2aTrustedBeneficiaries trustedBeneficiaries);
}

