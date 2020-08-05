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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPiisConsentMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static de.adorsys.psd2.consent.api.CmsError.LOGICAL_ERROR;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Xs2aPiisConsentServiceTest {
    private static final String CORRECT_PSU_ID = "marion.mueller";
    private static final String CONSENT_ID = "consent ID";
    private static final PsuIdData PSU_ID_DATA = new PsuIdData(CORRECT_PSU_ID, null, null, null, null);
    private final TppInfo tppInfo = buildTppInfo();

    @InjectMocks
    private Xs2aPiisConsentService xs2aPiisConsentService;
    @Mock
    private Xs2aPiisConsentMapper xs2aPiisConsentMapper;
    @Mock
    private CmsConsent cmsConsent;
    @Mock
    private PiisConsent piisConsent;
    @Mock
    private ConsentServiceEncrypted consentService;

    @Test
    void createConsent_success() throws WrongChecksumException {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(xs2aPiisConsentMapper.mapToCmsConsent(request, PSU_ID_DATA, tppInfo))
            .thenReturn(cmsConsent);
        CmsResponse<CmsCreateConsentResponse> response = CmsResponse.<CmsCreateConsentResponse>builder().payload(new CmsCreateConsentResponse(CONSENT_ID, cmsConsent)).build();
        when(consentService.createConsent(cmsConsent))
            .thenReturn(response);
        when(xs2aPiisConsentMapper.mapToPiisConsent(cmsConsent))
            .thenReturn(piisConsent);

        //When
        Optional<Xs2aCreatePiisConsentResponse> xs2aCreatePiisConsentResponseOptional = xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, tppInfo);

        //Then
        assertTrue(xs2aCreatePiisConsentResponseOptional.isPresent());
        Xs2aCreatePiisConsentResponse xs2aCreatePiisConsentResponse = xs2aCreatePiisConsentResponseOptional.get();
        assertEquals(CONSENT_ID, xs2aCreatePiisConsentResponse.getConsentId());
        assertEquals(piisConsent, xs2aCreatePiisConsentResponse.getPiisConsent());
    }

    @Test
    void createConsent_catchWrongChecksumException() throws WrongChecksumException {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(xs2aPiisConsentMapper.mapToCmsConsent(request, PSU_ID_DATA, tppInfo))
            .thenReturn(cmsConsent);
        when(consentService.createConsent(cmsConsent))
            .thenThrow(new WrongChecksumException());

        //When
        Optional<Xs2aCreatePiisConsentResponse> xs2aCreatePiisConsentResponseOptional = xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, tppInfo);

        //Then
        assertTrue(xs2aCreatePiisConsentResponseOptional.isEmpty());
    }

    @Test
    void createConsent_withError() throws WrongChecksumException {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(xs2aPiisConsentMapper.mapToCmsConsent(request, PSU_ID_DATA, tppInfo))
            .thenReturn(cmsConsent);
        when(consentService.createConsent(cmsConsent))
            .thenReturn(CmsResponse.<CmsCreateConsentResponse>builder()
                            .error(LOGICAL_ERROR)
                            .build());

        //When
        Optional<Xs2aCreatePiisConsentResponse> xs2aCreatePiisConsentResponseOptional = xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, tppInfo);

        //Then
        assertTrue(xs2aCreatePiisConsentResponseOptional.isEmpty());
    }

    @Test
    void getPiisConsentById_success() {
        //Given
        when(consentService.getConsentById(CONSENT_ID))
            .thenReturn(CmsResponse.<CmsConsent>builder().payload(cmsConsent).build());
        when(xs2aPiisConsentMapper.mapToPiisConsent(cmsConsent))
            .thenReturn(piisConsent);
        //When
        Optional<PiisConsent> piisConsentById = xs2aPiisConsentService.getPiisConsentById(CONSENT_ID);
        //Then
        assertTrue(piisConsentById.isPresent());
        assertEquals(piisConsent, piisConsentById.get());
    }

    @Test
    void getPiisConsentById_withError() {
        //Given
        when(consentService.getConsentById(CONSENT_ID))
            .thenReturn(CmsResponse.<CmsConsent>builder().error(LOGICAL_ERROR).build());
        //When
        Optional<PiisConsent> piisConsentById = xs2aPiisConsentService.getPiisConsentById(CONSENT_ID);
        //Then
        assertTrue(piisConsentById.isEmpty());
    }

    @Test
    void updateConsentStatusById_success() throws WrongChecksumException {
        //Given
        //When
        xs2aPiisConsentService.updateConsentStatus(CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED);
        //Then
        verify(consentService, atLeastOnce()).updateConsentStatusById(CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED);
    }

    @Test
    void updateMultilevelScaRequired() throws WrongChecksumException {
        //Given
        //When
        xs2aPiisConsentService.updateMultilevelScaRequired(CONSENT_ID, true);
        //Then
        verify(consentService, atLeastOnce()).updateMultilevelScaRequired(CONSENT_ID, true);
    }

    @Test
    void updateMultilevelScaRequired_throwWrongChecksumException() throws WrongChecksumException {
        //Given
        doThrow(new WrongChecksumException()).when(consentService).updateMultilevelScaRequired(CONSENT_ID, true);
        //When
        xs2aPiisConsentService.updateMultilevelScaRequired(CONSENT_ID, true);
        //Then
        verify(consentService, atLeastOnce()).updateMultilevelScaRequired(CONSENT_ID, true);
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("Test TppId");
        return tppInfo;
    }
}
