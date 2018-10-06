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

package de.adorsys.aspsp.xs2a.service;

import de.adorsys.aspsp.xs2a.domain.TppInfo;
import de.adorsys.aspsp.xs2a.exception.CertificateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TppService {

    public String getTppId() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                   .map(authentication -> (TppInfo) authentication.getCredentials())
                   .map(TppInfo::getAuthorisationNumber)
                   .orElseThrow(CertificateException::new);
    }

    public TppInfo getTppInfo() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                   .map(authentication -> (TppInfo) authentication.getCredentials())
                   .orElseThrow(CertificateException::new);
    }
}
