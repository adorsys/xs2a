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

import de.adorsys.psd2.model.AccountReference;
import de.adorsys.psd2.model.OtherType;
import de.adorsys.psd2.model.TrustedBeneficiariesList;
import de.adorsys.psd2.model.TrustedBeneficiary;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiaries;
import de.adorsys.psd2.xs2a.domain.account.Xs2aTrustedBeneficiariesList;
import de.adorsys.psd2.xs2a.web.mapper.Xs2aAddressMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", uses = {Xs2aAddressMapper.class})
public interface TrustedBeneficiariesModelMapper {

    default TrustedBeneficiariesList mapToTrustedBeneficiariesList(Xs2aTrustedBeneficiariesList xs2ATrustedBeneficiariesList) {
        List<Xs2aTrustedBeneficiaries> trustedBeneficiaries = xs2ATrustedBeneficiariesList.getTrustedBeneficiaries();

        List<TrustedBeneficiary> beneficiaries = trustedBeneficiaries.stream()
                                                     .map(this::mapToTrustedBeneficiaries)
                                                     .collect(Collectors.toList());

        TrustedBeneficiariesList result = new TrustedBeneficiariesList();
        result.addAll(beneficiaries);
        return result;
    }

    @Mapping(target = "creditorAccount", expression = "java(mapToAccountReference(trustedBeneficiaries.getCreditorAccount()))")
    @Mapping(target = "debtorAccount", expression = "java(mapToAccountReference(trustedBeneficiaries.getDebtorAccount()))")
    TrustedBeneficiary mapToTrustedBeneficiaries(Xs2aTrustedBeneficiaries trustedBeneficiaries);

    @Mapping(target = "currency", expression = "java(mapToCurrency(value.getCurrency()))")
    @Mapping(target = "other", expression = "java(mapToOtherType(value.getOther()))")
    AccountReference mapToAccountReference(de.adorsys.psd2.xs2a.core.profile.AccountReference value);

    default OtherType mapToOtherType(String other){
        return other == null
                   ? null
                   : new OtherType().identification(other);
    }

    default String mapToCurrency(Currency value){
        return value == null
                   ? null
                   : value.getCurrencyCode();
    }
}

