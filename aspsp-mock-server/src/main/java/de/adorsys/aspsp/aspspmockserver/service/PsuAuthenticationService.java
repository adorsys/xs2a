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

import de.adorsys.aspsp.aspspmockserver.repository.EmailTanRepository;
import de.adorsys.aspsp.aspspmockserver.repository.PsuRepository;
import de.adorsys.aspsp.xs2a.spi.domain.psu.PsuLogin;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PsuAuthenticationService {
    private EmailTanRepository emailTanRepository;
    private PsuRepository psuRepository;

    @Autowired
    public PsuAuthenticationService(EmailTanRepository emailTanRepository) {
        this.emailTanRepository = emailTanRepository;
    }

    public boolean isPsuEmailAndPasswordValid(PsuLogin psuLogin) {
        return Optional.ofNullable(psuLogin.getEmail())
                   .flatMap(email -> psuRepository.findPsuByEmailIn(email)
                                         .map(p -> isPasswordCorrect(p.getPassword(), psuLogin.getPassword())))
                   .orElse(false);
    }

    public boolean isPsuEmailTanValid(String psuId, int tan) {
        return false;
    }

    private int generateEmailTanForPsu(String psuId) {
        return 0;
    }

    private boolean isPasswordCorrect(String originalPassword, String enteredPassword) {
        return StringUtils.isNotBlank(originalPassword) && StringUtils.isNotBlank(enteredPassword) && originalPassword.equals(enteredPassword);
    }
}
