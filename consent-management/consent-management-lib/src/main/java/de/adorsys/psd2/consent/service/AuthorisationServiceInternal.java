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

package de.adorsys.psd2.consent.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.CmsScaMethod;
import de.adorsys.psd2.consent.api.authorisation.AuthorisationParentHolder;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationResponse;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.service.AuthorisationService;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.ScaMethod;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.service.authorisation.AuthService;
import de.adorsys.psd2.consent.service.authorisation.AuthServiceResolver;
import de.adorsys.psd2.consent.service.authorisation.AuthorisationClosingService;
import de.adorsys.psd2.consent.service.mapper.AuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.ScaMethodMapper;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.profile.ScaApproach;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthorisationScaApproachResponse;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.adorsys.psd2.consent.api.CmsError.LOGICAL_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorisationServiceInternal implements AuthorisationService {
    private final AuthorisationRepository authorisationRepository;
    private final ScaMethodMapper scaMethodMapper;
    private final AuthorisationMapper authorisationMapper;
    private final AuthServiceResolver authServiceResolver;
    private final AuthorisationClosingService authorisationClosingService;

    @Transactional
    @Override
    public CmsResponse<CreateAuthorisationResponse> createAuthorisation(AuthorisationParentHolder parentHolder, CreateAuthorisationRequest request) {
        String parentId = parentHolder.getParentId();
        AuthorisationType authorisationType = parentHolder.getAuthorisationType();
        AuthService authService = authServiceResolver.getAuthService(authorisationType);
        Optional<Authorisable> parentOptional = authService.getNotFinalisedAuthorisationParent(parentId);

        if (parentOptional.isEmpty()) {
            log.info("Authorisation type: [{}], Parent ID: [{}]. Create authorisation has failed, because authorisation's parent couldn't be found",
                     authorisationType, parentId);
            return CmsResponse.<CreateAuthorisationResponse>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        authorisationClosingService.closePreviousAuthorisationsByParent(parentId, authorisationType, request.getPsuData());

        Authorisable authorisationParent = parentOptional.get();
        AuthorisationEntity newAuthorisation = authService.saveAuthorisation(request, authorisationParent);

        CreateAuthorisationResponse response = new CreateAuthorisationResponse(newAuthorisation.getExternalId(),
                                                                               newAuthorisation.getScaStatus(),
                                                                               authorisationParent.getInternalRequestId(authorisationType),
                                                                               request.getPsuData(),
                                                                               newAuthorisation.getScaApproach());
        return CmsResponse.<CreateAuthorisationResponse>builder()
                   .payload(response)
                   .build();

    }

    @Transactional(readOnly = true)
    @Override
    public CmsResponse<Authorisation> getAuthorisationById(String authorisationId) {
        Optional<AuthorisationEntity> authorisationOptional = getAuthorisation(authorisationId);

        if (authorisationOptional.isEmpty()) {
            log.info("Authorisation ID: [{}]. Get authorisation has failed, because authorisation could not found",
                     authorisationId);

            return CmsResponse.<Authorisation>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        AuthorisationEntity authorisation = authorisationOptional.get();
        return CmsResponse.<Authorisation>builder()
                   .payload(authorisationMapper.mapToAuthorisation(authorisation))
                   .build();
    }

    @Transactional
    @Override
    public CmsResponse<Authorisation> updateAuthorisation(String authorisationId, UpdateAuthorisationRequest request) {
        Optional<AuthorisationEntity> authorisationOptional = getAuthorisation(authorisationId);

        if (authorisationOptional.isEmpty()) {
            log.info("Authorisation ID: [{}]. Update authorisation has failed, because authorisation couldn't be found",
                     authorisationId);
            return CmsResponse.<Authorisation>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        AuthorisationEntity authorisation = authorisationOptional.get();
        PsuIdData psuDataFromRequest = request.getPsuData();
        authorisationClosingService.closePreviousAuthorisationsByAuthorisation(authorisation, psuDataFromRequest);

        if (authorisation.getScaStatus().isFinalisedStatus()) {
            log.info("Authorisation ID: [{}], SCA status: [{}]. Update authorisation has failed, because authorisation has finalised status",
                     authorisationId, authorisation.getScaStatus().getValue());
            return CmsResponse.<Authorisation>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        AuthorisationEntity updatedAuthorisation = authServiceResolver.getAuthService(request.getAuthorisationType()).doUpdateAuthorisation(authorisation, request);

        return CmsResponse.<Authorisation>builder()
                   .payload(authorisationMapper.mapToAuthorisation(updatedAuthorisation))
                   .build();
    }

    @Transactional
    @Override
    public CmsResponse<Boolean> updateAuthorisationStatus(String authorisationId, ScaStatus scaStatus) {
        Optional<AuthorisationEntity> authorisationOptional = getAuthorisation(authorisationId);

        if (authorisationOptional.isEmpty()) {
            log.info("Authorisation ID: [{}]. Update authorisation status has failed, because authorisation couldn't be found by id",
                     authorisationId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        AuthorisationEntity authorisationEntity = authorisationOptional.get();
        authorisationEntity.setScaStatus(scaStatus);

        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    @Transactional
    @Override
    public CmsResponse<List<String>> getAuthorisationsByParentId(AuthorisationParentHolder parentHolder) {
        AuthService authService = authServiceResolver.getAuthService(parentHolder.getAuthorisationType());

        Optional<Authorisable> authorisationParentOptional = authService.getAuthorisationParent(parentHolder.getParentId());
        if (authorisationParentOptional.isEmpty()) {
            log.info("Parent ID: [{}]. Get the list of authorisation IDs has failed, because parent couldn't be found",
                     parentHolder.getParentId());
            return CmsResponse.<List<String>>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        Authorisable authorisationParent = authorisationParentOptional.get();
        Authorisable checkedParent = authService.checkAndUpdateOnConfirmationExpiration(authorisationParent);

        List<AuthorisationEntity> authorisations = authService.getAuthorisationsByParentId(checkedParent.getExternalId());
        List<String> authorisationIds = authorisations.stream()
                                            .map(AuthorisationEntity::getExternalId)
                                            .collect(Collectors.toList());

        return CmsResponse.<List<String>>builder()
                   .payload(authorisationIds)
                   .build();
    }

    @Transactional
    @Override
    public CmsResponse<ScaStatus> getAuthorisationScaStatus(String authorisationId, AuthorisationParentHolder parentHolder) {
        AuthService authService = authServiceResolver.getAuthService(parentHolder.getAuthorisationType());
        Optional<Authorisable> parentOptional = authService.getAuthorisationParent(parentHolder.getParentId());
        if (parentOptional.isEmpty()) {
            log.info("Parent ID: [{}], Authorisation ID: [{}]. Get authorisation SCA status has failed, because parent couldn't be found",
                     parentHolder.getParentId(), authorisationId);
            return CmsResponse.<ScaStatus>builder()
                       .error(LOGICAL_ERROR)
                       .build();
        }

        Authorisable parent = parentOptional.get();
        if (authService.isConfirmationExpired(parent)) {
            authService.updateOnConfirmationExpiration(parent);
            log.info("Parent ID: [{}], Authorisation ID: [{}]. Get authorisation SCA status has failed, because parent is expired",
                     parentHolder.getParentId(), authorisationId);
            return CmsResponse.<ScaStatus>builder()
                       .payload(ScaStatus.FAILED)
                       .build();
        }

        Optional<AuthorisationEntity> authorisation = findAuthorisationInParent(authorisationId, parentHolder.getAuthorisationType(), parent);
        if (authorisation.isPresent()) {
            return CmsResponse.<ScaStatus>builder()
                       .payload(authorisation.get().getScaStatus())
                       .build();
        }
        return CmsResponse.<ScaStatus>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    @Transactional(readOnly = true)
    @Override
    public CmsResponse<Boolean> isAuthenticationMethodDecoupled(String authorisationId, String authenticationMethodId) {
        Optional<AuthorisationEntity> authorisationOptional = getAuthorisation(authorisationId);

        Optional<Boolean> isDecoupledOptional = authorisationOptional.map(a -> a.getAvailableScaMethods()
                                                                                   .stream()
                                                                                   .filter(m -> Objects.equals(m.getAuthenticationMethodId(), authenticationMethodId))
                                                                                   .anyMatch(ScaMethod::isDecoupled));
        if (isDecoupledOptional.isPresent()) {
            return CmsResponse.<Boolean>builder()
                       .payload(isDecoupledOptional.get())
                       .build();
        }

        log.info("Authorisation ID: [{}]. Check whether authentication method is decoupled has failed, because authorisation with such method couldn't be found",
                 authorisationId);
        return CmsResponse.<Boolean>builder()
                   .payload(false)
                   .build();
    }

    @Transactional
    @Override
    public CmsResponse<Boolean> saveAuthenticationMethods(String authorisationId, List<CmsScaMethod> methods) {
        Optional<AuthorisationEntity> authorisationOptional = getAuthorisation(authorisationId);

        if (authorisationOptional.isEmpty()) {
            log.info("Authorisation ID: [{}]. Save authentication methods has failed, because authorisation couldn't be found", authorisationId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        AuthorisationEntity authorisation = authorisationOptional.get();
        List<ScaMethod> scaMethods = scaMethodMapper.mapToScaMethods(methods);
        if (!CollectionUtils.isEqualCollection(scaMethods, authorisation.getAvailableScaMethods())) {
            authorisation.setAvailableScaMethods(scaMethods);
        }
        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    @Transactional
    @Override
    public CmsResponse<Boolean> updateScaApproach(String authorisationId, ScaApproach scaApproach) {
        Optional<AuthorisationEntity> authorisationOptional = getAuthorisation(authorisationId);

        if (authorisationOptional.isEmpty()) {
            log.info("Authorisation ID: [{}]. Update SCA approach has failed, because authorisation couldn't be found",
                     authorisationId);
            return CmsResponse.<Boolean>builder()
                       .payload(false)
                       .build();
        }

        AuthorisationEntity authorisation = authorisationOptional.get();

        authorisation.setScaApproach(scaApproach);
        return CmsResponse.<Boolean>builder()
                   .payload(true)
                   .build();
    }

    @Transactional(readOnly = true)
    @Override
    public CmsResponse<AuthorisationScaApproachResponse> getAuthorisationScaApproach(String authorisationId) {
        Optional<AuthorisationScaApproachResponse> approachResponseOptional = authorisationRepository.findByExternalId(authorisationId)
                                                                                  .map(a -> new AuthorisationScaApproachResponse(a.getScaApproach()));

        if (approachResponseOptional.isPresent()) {
            return CmsResponse.<AuthorisationScaApproachResponse>builder()
                       .payload(approachResponseOptional.get())
                       .build();
        }

        log.info("Authorisation ID: [{}]. Get SCA approach has failed, because authorisation couldn't be found",
                 authorisationId);

        return CmsResponse.<AuthorisationScaApproachResponse>builder()
                   .error(LOGICAL_ERROR)
                   .build();
    }

    private Optional<AuthorisationEntity> findAuthorisationInParent(String authorisationId, AuthorisationType authorisationType, Authorisable parent) {
        AuthService authService = authServiceResolver.getAuthService(authorisationType);
        return authService.getAuthorisationById(authorisationId)
                   .filter(a -> a.getParentExternalId().equals(parent.getExternalId()));
    }

    private Optional<AuthorisationEntity> getAuthorisation(String authorisationId) {
        return authorisationRepository.findByExternalId(authorisationId);
    }
}
