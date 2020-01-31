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

package de.adorsys.psd2.consent.domain.sha;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class AisConsentSha {
    private boolean recurringIndicator;
    private boolean combinedServiceIndicator;
    private LocalDate expireDate; //TODO: This variable should be renamed to validUntil https://git.adorsys.de/adorsys/xs2a/aspsp-xs2a/issues/1159
    private int tppFrequencyPerDay;
    private List<TppAccountAccessSha> accesses;
    private List<AspspAccountAccessSha> aspspAccountAccesses;
}
