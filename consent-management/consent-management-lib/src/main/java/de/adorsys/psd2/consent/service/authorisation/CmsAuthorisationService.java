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

package de.adorsys.psd2.consent.service.authorisation;

import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.domain.Authorisable;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.repository.AuthorisationRepository;
import de.adorsys.psd2.consent.service.ConfirmationExpirationService;
import de.adorsys.psd2.consent.service.mapper.AuthorisationMapper;
import de.adorsys.psd2.consent.service.mapper.PsuDataMapper;
import de.adorsys.psd2.consent.service.psu.CmsPsuService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public abstract class CmsAuthorisationService<T extends Authorisable> implements AuthService {
    protected final CmsPsuService cmsPsuService;
    protected final PsuDataMapper psuDataMapper;
    protected final AspspProfileService aspspProfileService;
    protected final AuthorisationMapper authorisationMapper;
    protected final AuthorisationRepository authorisationRepository;
    protected final ConfirmationExpirationService<T> confirmationExpirationService;

    @Override
    public List<AuthorisationEntity> getAuthorisationsByParentId(String parentId) {
        return authorisationRepository.findAllByParentExternalIdAndType(parentId, getAuthorisationType());
    }

    @Override
    public Optional<AuthorisationEntity> getAuthorisationById(String authorisationId) {
        return authorisationRepository.findByExternalIdAndType(authorisationId, getAuthorisationType());
    }

    @Override
    public AuthorisationEntity saveAuthorisation(CreateAuthorisationRequest request, Authorisable authorisationParent) {
        List<PsuData> psuDataList = authorisationParent.getPsuDataList();
        Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(psuDataMapper.mapToPsuData(request.getPsuData(), authorisationParent.getInstanceId()), psuDataList);

        psuDataOptional.ifPresent(psuData -> authorisationParent.setPsuDataList(cmsPsuService.enrichPsuData(psuData, psuDataList)));
        authorisationParent.setPsuDataList(psuDataList);

        CommonAspspProfileSetting commonAspspProfileSetting = aspspProfileService.getAspspSettings(authorisationParent.getInstanceId()).getCommon();
        AuthorisationEntity entity = authorisationMapper.prepareAuthorisationEntity(authorisationParent, request, psuDataOptional, getAuthorisationType(),
                                                                                    commonAspspProfileSetting.getRedirectUrlExpirationTimeMs(),
                                                                                    commonAspspProfileSetting.getAuthorisationExpirationTimeMs());
        return authorisationRepository.save(entity);
    }

    @Override
    public AuthorisationEntity doUpdateAuthorisation(AuthorisationEntity authorisationEntity, UpdateAuthorisationRequest updateAuthorisationRequest) {
        PsuData psuRequest = psuDataMapper.mapToPsuData(updateAuthorisationRequest.getPsuData(), authorisationEntity.getInstanceId());
        if (ScaStatus.RECEIVED == authorisationEntity.getScaStatus()) {

            if (!cmsPsuService.isPsuDataRequestCorrect(psuRequest, authorisationEntity.getPsuData())) {
                log.info("Authorisation ID: [{}], SCA status: [{}]. Update authorisation failed, because psu data request does not match stored psu data",
                         authorisationEntity.getExternalId(), authorisationEntity.getScaStatus().getValue());
                return authorisationEntity;
            }

            Optional<Authorisable> aisConsentOptional = getAuthorisationParent(authorisationEntity.getParentExternalId());
            if (aisConsentOptional.isEmpty()) {
                log.info("Authorisation ID: [{}], Parent ID: [{}]. Update authorisation failed, couldn't find parent by ID from the authorisation",
                         authorisationEntity.getExternalId(), authorisationEntity.getParentExternalId());
                return authorisationEntity;
            }

            Authorisable authorisationParent = aisConsentOptional.get();
            Optional<PsuData> psuDataOptional = cmsPsuService.definePsuDataForAuthorisation(psuRequest, authorisationParent.getPsuDataList());

            if (psuDataOptional.isPresent()) {
                PsuData psuData = psuDataOptional.get();
                authorisationParent.setPsuDataList(cmsPsuService.enrichPsuData(psuData, authorisationParent.getPsuDataList()));
                authorisationEntity.setPsuData(psuData);
                updateAuthorisable(authorisationParent);
            }
        } else {
            boolean isPsuCorrect = Objects.nonNull(authorisationEntity.getPsuData())
                                       && isPsuDataCorrectIfPresent(psuRequest, authorisationEntity);
            if (!isPsuCorrect) {
                log.info("Authorisation ID: [{}], SCA status: [{}]. Update authorisation failed, because PSU data request does not match stored PSU data",
                         authorisationEntity.getExternalId(), authorisationEntity.getScaStatus().getValue());
                return authorisationEntity;
            }
        }

        if (ScaStatus.SCAMETHODSELECTED == updateAuthorisationRequest.getScaStatus()) {
            authorisationEntity.setAuthenticationMethodId(updateAuthorisationRequest.getAuthenticationMethodId());
        }

        authorisationEntity.setScaStatus(updateAuthorisationRequest.getScaStatus());
        return authorisationRepository.save(authorisationEntity);
    }

    private boolean isPsuDataCorrectIfPresent(PsuData psuRequest, AuthorisationEntity authorisationEntity) {
        if (psuRequest != null) {
            return authorisationEntity.getPsuData().contentEquals(psuRequest);
        } else {
            return true;
        }
    }

    @Override
    public Authorisable checkAndUpdateOnConfirmationExpiration(Authorisable authorisable) {
        return confirmationExpirationService.checkAndUpdateOnConfirmationExpiration(castToParent(authorisable));
    }

    @Override
    public boolean isConfirmationExpired(Authorisable authorisable) {
        return confirmationExpirationService.isConfirmationExpired(castToParent(authorisable));
    }

    @Override
    public Authorisable updateOnConfirmationExpiration(Authorisable authorisable) {
        return confirmationExpirationService.updateOnConfirmationExpiration(castToParent(authorisable));
    }

    protected void updateAuthorisable(Object authorisable) { //NOPMD
        //do nothing
    }

    abstract AuthorisationType getAuthorisationType();

    abstract T castToParent(Authorisable authorisable);
}
