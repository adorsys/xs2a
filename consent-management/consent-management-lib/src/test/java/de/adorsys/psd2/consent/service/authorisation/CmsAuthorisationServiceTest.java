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

import de.adorsys.psd2.aspsp.profile.domain.AspspSettings;
import de.adorsys.psd2.aspsp.profile.domain.common.CommonAspspProfileSetting;
import de.adorsys.psd2.aspsp.profile.service.AspspProfileService;
import de.adorsys.psd2.consent.api.authorisation.CreateAuthorisationRequest;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.service.ConfirmationExpirationService;
import de.adorsys.psd2.xs2a.core.authorisation.AuthorisationType;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static de.adorsys.psd2.consent.psu.api.config.CmsPsuApiDefaultValue.DEFAULT_SERVICE_INSTANCE_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsAuthorisationServiceTest {
    private static final String PARENT_ID = "payment ID";
    private static final String AUTHORISATION_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final PsuData PSU_DATA = new PsuData("PSU_ID", null, null, null, null);
    private static final PsuIdData PSU_ID_DATA = new PsuIdData("PSU_ID", null, null, null, null);

    @InjectMocks
    private PisAuthService service;

    @Mock
    private PsuService psuService;
    @Mock
    private AspspProfileService aspspProfileService;
    @Mock
    private AuthorisationService authorisationService;
    @Mock
    private ConfirmationExpirationService<PisCommonPaymentData> confirmationExpirationService;
    @Mock
    private CommonPaymentService commonPaymentService;

    @Mock
    private AspspSettings aspspSettings;
    @Mock
    private CommonAspspProfileSetting commonAspspProfileSetting;

    @Test
    void getAuthorisationsByParentId() {
        service.getAuthorisationsByParentId(PARENT_ID);
        verify(authorisationService, times(1)).findAllByParentExternalIdAndType(PARENT_ID, AuthorisationType.PIS_CREATION);
    }

    @Test
    void getAuthorisationById() {
        service.getAuthorisationById(AUTHORISATION_ID);
        verify(authorisationService, times(1)).findByExternalIdAndType(AUTHORISATION_ID, AuthorisationType.PIS_CREATION);
    }

    @Test
    void saveAuthorisation() {
        PisCommonPaymentData authorisationParent = new PisCommonPaymentData();
        authorisationParent.setPsuDataList(Collections.singletonList(PSU_DATA));
        CreateAuthorisationRequest request = new CreateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);

        when(psuService.mapToPsuData(request.getPsuData(), DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(PSU_DATA);
        when(psuService.definePsuDataForAuthorisation(PSU_DATA, Collections.singletonList(PSU_DATA)))
            .thenReturn(Optional.of(PSU_DATA));
        when(aspspProfileService.getAspspSettings(authorisationParent.getInstanceId())).thenReturn(aspspSettings);
        when(aspspSettings.getCommon()).thenReturn(commonAspspProfileSetting);
        when(commonAspspProfileSetting.getRedirectUrlExpirationTimeMs()).thenReturn(100L);
        when(commonAspspProfileSetting.getAuthorisationExpirationTimeMs()).thenReturn(200L);

        AuthorisationEntity entity = new AuthorisationEntity();
        when(authorisationService.prepareAuthorisationEntity(authorisationParent, request, Optional.of(PSU_DATA), AuthorisationType.PIS_CREATION, 100L, 200L))
            .thenReturn(entity);

        service.saveAuthorisation(request, authorisationParent);

        verify(authorisationService, times(1)).save(entity);
    }

    @Test
    void doUpdateAuthorisation_success() {
        AuthorisationEntity entity = new AuthorisationEntity();
        entity.setScaStatus(ScaStatus.RECEIVED);
        entity.setPsuData(PSU_DATA);
        entity.setParentExternalId(PARENT_ID);
        entity.setAuthenticationMethodId("111");

        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);
        request.setScaStatus(ScaStatus.SCAMETHODSELECTED);
        request.setAuthenticationMethodId("222");

        assertNotEquals(request.getScaStatus(), entity.getScaStatus());
        assertNotEquals(request.getAuthenticationMethodId(), entity.getAuthenticationMethodId());

        when(psuService.mapToPsuData(PSU_ID_DATA, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(PSU_DATA);
        when(psuService.isPsuDataRequestCorrect(PSU_DATA, PSU_DATA)).thenReturn(true);

        PisCommonPaymentData authorisationParent = new PisCommonPaymentData();
        authorisationParent.setPsuDataList(Collections.singletonList(PSU_DATA));
        when(commonPaymentService.findOneByPaymentId(PARENT_ID)).thenReturn(Optional.of(authorisationParent));
        when(psuService.definePsuDataForAuthorisation(PSU_DATA, Collections.singletonList(PSU_DATA)))
            .thenReturn(Optional.of(PSU_DATA));
        when(psuService.enrichPsuData(PSU_DATA, Collections.singletonList(PSU_DATA)))
            .thenReturn(Collections.singletonList(PSU_DATA));

        service.doUpdateAuthorisation(entity, request);

        verify(authorisationService, times(1)).save(entity);

        assertEquals(request.getScaStatus(), entity.getScaStatus());
        assertEquals("222", entity.getAuthenticationMethodId());
    }

    @Test
    void doUpdateAuthorisation_authorisationParentNotFound() {
        AuthorisationEntity entity = new AuthorisationEntity();
        entity.setScaStatus(ScaStatus.RECEIVED);
        entity.setPsuData(PSU_DATA);
        entity.setParentExternalId(PARENT_ID);

        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);
        request.setScaStatus(ScaStatus.PSUIDENTIFIED);
        assertNotEquals(request.getScaStatus(), entity.getScaStatus());

        when(psuService.mapToPsuData(PSU_ID_DATA, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(PSU_DATA);
        when(psuService.isPsuDataRequestCorrect(PSU_DATA, PSU_DATA)).thenReturn(true);

        PisCommonPaymentData authorisationParent = new PisCommonPaymentData();
        authorisationParent.setPsuDataList(Collections.singletonList(PSU_DATA));
        when(commonPaymentService.findOneByPaymentId(PARENT_ID)).thenReturn(Optional.empty());

        service.doUpdateAuthorisation(entity, request);

        verify(authorisationService, never()).save(entity);
    }

    @Test
    void doUpdateAuthorisation_isNotPsuDataRequestCorrect() {
        AuthorisationEntity entity = new AuthorisationEntity();
        entity.setPsuData(PSU_DATA);
        entity.setScaStatus(ScaStatus.RECEIVED);

        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);

        when(psuService.mapToPsuData(PSU_ID_DATA, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(PSU_DATA);
        when(psuService.isPsuDataRequestCorrect(PSU_DATA, PSU_DATA)).thenReturn(false);

        service.doUpdateAuthorisation(entity, request);

        verify(authorisationService, never()).save(entity);
    }

    @Test
    void doUpdateAuthorisation_scaStatusNotReceived() {
        AuthorisationEntity entity = new AuthorisationEntity();
        entity.setScaStatus(ScaStatus.PSUIDENTIFIED);

        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();
        request.setPsuData(PSU_ID_DATA);

        when(psuService.mapToPsuData(PSU_ID_DATA, DEFAULT_SERVICE_INSTANCE_ID)).thenReturn(PSU_DATA);

        service.doUpdateAuthorisation(entity, request);

        verify(authorisationService, never()).save(entity);
    }
}
