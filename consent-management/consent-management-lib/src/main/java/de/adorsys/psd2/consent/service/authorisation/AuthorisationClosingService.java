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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorisationClosingService {
    private final PsuDataMapper psuDataMapper;
    private final AuthServiceResolver authServiceResolver;
    private final AuthorisationRepository authorisationRepository;

    @Transactional
    public void closePreviousAuthorisationsByAuthorisation(AuthorisationEntity authorisation, PsuIdData psuIdData) {
        if (psuIdData == null || psuIdData.isEmpty()) {
            log.info("Closing previous authorisations by PSU is skipped, because no PSU data has been provided in the request");
            return;
        }

        String parentId = authorisation.getParentExternalId();
        List<AuthorisationEntity> parentAuthorisations = authServiceResolver.getAuthService(authorisation.getType()).getAuthorisationsByParentId(parentId);
        List<AuthorisationEntity> previousAuthorisations = parentAuthorisations.stream()
                                                               .filter(a -> !a.getExternalId().equals(authorisation.getExternalId()))
                                                               .collect(Collectors.toList());

        closePreviousAuthorisationsByPsu(authorisation.getType(), previousAuthorisations, psuIdData);
    }

    @Transactional
    public void closePreviousAuthorisationsByParent(String parentId, AuthorisationType authorisationType, PsuIdData psuIdData) {
        if (psuIdData == null || psuIdData.isEmpty()) {
            log.info("Closing previous authorisations by PSU is skipped, because no PSU data has been provided in the request");
            return;
        }

        List<AuthorisationEntity> parentAuthorisations = authServiceResolver.getAuthService(authorisationType).getAuthorisationsByParentId(parentId);
        closePreviousAuthorisationsByPsu(authorisationType, parentAuthorisations, psuIdData);
    }

    private void closePreviousAuthorisationsByPsu(AuthorisationType authorisationType, List<AuthorisationEntity> authorisations, PsuIdData psuIdData) {
        String instanceId = authorisations.isEmpty() ? null : authorisations.get(0).getInstanceId();
        PsuData psuData = psuDataMapper.mapToPsuData(psuIdData, instanceId);

        List<AuthorisationEntity> authorisationsToBeClosed = authorisations
                                                                 .stream()
                                                                 .filter(auth -> auth.getType().equals(authorisationType))
                                                                 .filter(auth -> Objects.nonNull(auth.getPsuData()) && auth.getPsuData().contentEquals(psuData))
                                                                 .collect(Collectors.toList());

        authorisationsToBeClosed.forEach(this::failAndExpireAuthorisation);
    }

    private void failAndExpireAuthorisation(AuthorisationEntity auth) {
        auth.setScaStatus(ScaStatus.FAILED);
        auth.setRedirectUrlExpirationTimestamp(OffsetDateTime.now());
        authorisationRepository.save(auth);
    }
}
