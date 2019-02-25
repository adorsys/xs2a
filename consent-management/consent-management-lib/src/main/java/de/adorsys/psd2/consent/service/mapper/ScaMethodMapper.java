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

import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.domain.ScaMethod;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ScaMethodMapper {
    @NotNull
    public List<ScaMethod> mapToScaMethods(@NotNull List<CmsScaMethod> cmsScaMethods) {
        return cmsScaMethods.stream()
                   .map(this::mapToScaMethod)
                   .collect(Collectors.toList());
    }

    @NotNull
    private ScaMethod mapToScaMethod(@NotNull CmsScaMethod cmsScaMethod) {
        ScaMethod scaMethod = new ScaMethod();
        scaMethod.setAuthenticationMethodId(cmsScaMethod.getAuthenticationMethodId());
        scaMethod.setDecoupled(cmsScaMethod.isDecoupled());
        return scaMethod;
    }
}
