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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.aspsp.api.piis.CmsAspspPiisFundsExportService;
import de.adorsys.psd2.xs2a.core.piis.PiisConsent;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CmsAspspPiisFundsExportServiceInternal implements CmsAspspPiisFundsExportService {
    @Override
    public Collection<PiisConsent> exportConsentsByTpp(String tppAuthorisationNumber,
                                                       @Nullable LocalDate createDateFrom,
                                                       @Nullable LocalDate createDateTo, @Nullable PsuIdData psuIdData,
                                                       @NotNull String instanceId
                                                      ) {
        // TODO implement method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/613
        return Collections.emptyList();
    }

    @Override
    public Collection<PiisConsent> exportConsentsByPsu(PsuIdData psuIdData, @Nullable LocalDate createDateFrom,
                                                       @Nullable LocalDate createDateTo, @NotNull String instanceId
                                                      ) {
        // TODO implement method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/613
        return Collections.emptyList();
    }

    @Override
    public Collection<PiisConsent> exportConsentsByAccountId(@NotNull String aspspAccountId,
                                                             @Nullable LocalDate createDateFrom,
                                                             @Nullable LocalDate createDateTo,
                                                             @NotNull String instanceId
                                                            ) {
        // TODO implement method https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/613
        return Collections.emptyList();
    }
}
