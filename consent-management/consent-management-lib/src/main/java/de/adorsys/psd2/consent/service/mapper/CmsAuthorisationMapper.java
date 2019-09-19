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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.payment.PisAuthorization;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CmsAuthorisationMapper {
    private final PsuDataMapper psuDataMapper;

    @NotNull
    List<Authorisation> mapToAuthorisations(@NotNull List<PisAuthorization> pisAuthorisations) {
        return pisAuthorisations.stream()
                   .map(this::mapToAuthorisation)
                   .collect(Collectors.toList());
    }

    @NotNull
    private Authorisation mapToAuthorisation(@NotNull PisAuthorization pisAuthorisation) {
        PsuIdData psuIdData = psuDataMapper.mapToPsuIdData(pisAuthorisation.getPsuData());
        return new Authorisation(pisAuthorisation.getExternalId(), pisAuthorisation.getScaStatus(), psuIdData);
    }
}
