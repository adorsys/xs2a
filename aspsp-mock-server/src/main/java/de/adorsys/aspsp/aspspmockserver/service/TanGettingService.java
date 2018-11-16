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

package de.adorsys.aspsp.aspspmockserver.service;

import de.adorsys.aspsp.aspspmockserver.domain.TanHolder;
import de.adorsys.aspsp.aspspmockserver.repository.TanRepository;
import de.adorsys.psd2.aspsp.mock.api.psu.Psu;
import de.adorsys.psd2.aspsp.mock.api.psu.TanStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Profile("test")
@RequiredArgsConstructor
@Service
public class TanGettingService {
    private final TanRepository tanRepository;
    private final AccountService accountService;

    public Optional<TanHolder> getUnusedTanNumberByPsuId(String psuId) {
        Optional<Psu> psuOptional = accountService.getPsuByPsuId(psuId);

        if (!psuOptional.isPresent()) {
            return Optional.empty();
        }

        return tanRepository.findByPsuIdAndTanStatus(psuId, TanStatus.UNUSED).stream()
                   .findFirst()
                   .map(tan -> new TanHolder(tan.getTanNumber()));
    }
}
