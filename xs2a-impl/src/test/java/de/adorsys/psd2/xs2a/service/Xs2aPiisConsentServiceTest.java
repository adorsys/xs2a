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

package de.adorsys.psd2.xs2a.service;

import de.adorsys.psd2.consent.api.CmsError;
import de.adorsys.psd2.consent.api.CmsResponse;
import de.adorsys.psd2.consent.api.WrongChecksumException;
import de.adorsys.psd2.consent.api.ais.CmsConsent;
import de.adorsys.psd2.consent.api.authorisation.UpdateAuthorisationRequest;
import de.adorsys.psd2.consent.api.consent.CmsCreateConsentResponse;
import de.adorsys.psd2.consent.api.service.AisConsentServiceEncrypted;
import de.adorsys.psd2.consent.api.service.ConsentServiceEncrypted;
import de.adorsys.psd2.core.data.AccountAccess;
import de.adorsys.psd2.core.data.piis.v1.PiisConsent;
import de.adorsys.psd2.logger.context.LoggingContextService;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.domain.Xs2aResponse;
import de.adorsys.psd2.xs2a.domain.account.Xs2aCreatePiisConsentResponse;
import de.adorsys.psd2.xs2a.domain.consent.ConsentAuthorisationsParameters;
import de.adorsys.psd2.xs2a.domain.fund.CreatePiisConsentRequest;
import de.adorsys.psd2.xs2a.service.authorization.Xs2aAuthorisationService;
import de.adorsys.psd2.xs2a.service.consent.Xs2aPiisConsentService;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aConsentAuthorisationMapper;
import de.adorsys.psd2.xs2a.service.mapper.cms_xs2a_mappers.Xs2aPiisConsentMapper;
import de.adorsys.xs2a.reader.JsonReader;
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
    private static final String AUTHORISATION_ID = "a01562ea-19ff-4b5a-8188-c45d85bfa20a";
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
    @Mock
    private LoggingContextService loggingContextService;
    @Mock
    private AisConsentServiceEncrypted aisConsentServiceEncrypted;
    @Mock
    private Xs2aAuthorisationService authorisationService;
    @Mock
    private Xs2aConsentAuthorisationMapper consentAuthorisationMapper;
    @Mock
    private ScaApproachResolver scaApproachResolver;
    @Mock
    private RequestProviderService requestProviderService;
    @Mock
    private CmsCreateConsentResponseService cmsCreateConsentResponseService;

    private final JsonReader jsonReader = new JsonReader();

    @Test
    void createConsent_success() {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(xs2aPiisConsentMapper.mapToCmsConsent(request, PSU_ID_DATA, tppInfo))
            .thenReturn(cmsConsent);
        when(cmsCreateConsentResponseService.getCmsCreateConsentResponse(cmsConsent)).thenReturn(Xs2aResponse.<CmsCreateConsentResponse>builder()
                                                                                                     .payload(new CmsCreateConsentResponse(CONSENT_ID, cmsConsent))
                                                                                                     .build());

        when(xs2aPiisConsentMapper.mapToPiisConsent(cmsConsent))
            .thenReturn(piisConsent);

        //When
        Xs2aResponse<Xs2aCreatePiisConsentResponse> actualResult = xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, tppInfo);

        //Then
        assertTrue(actualResult.isSuccessful());
        Xs2aCreatePiisConsentResponse xs2aCreatePiisConsentResponse = actualResult.getPayload();
        assertEquals(CONSENT_ID, xs2aCreatePiisConsentResponse.getConsentId());
        assertEquals(piisConsent, xs2aCreatePiisConsentResponse.getPiisConsent());
    }

    @Test
    void createConsent_catchWrongChecksumException() {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(xs2aPiisConsentMapper.mapToCmsConsent(request, PSU_ID_DATA, tppInfo))
            .thenReturn(cmsConsent);
        when(cmsCreateConsentResponseService.getCmsCreateConsentResponse(cmsConsent)).thenReturn(Xs2aResponse.<CmsCreateConsentResponse>builder()
                                                                                                     .build());

        //When
        Xs2aResponse<Xs2aCreatePiisConsentResponse> xs2aCreatePiisConsentResponseXs2aResponse = xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, tppInfo);

        //Then
        assertTrue(xs2aCreatePiisConsentResponseXs2aResponse.hasError());
    }

    @Test
    void createConsent_withError() {
        //Given
        CreatePiisConsentRequest request = new CreatePiisConsentRequest(null, null, null, null, null);
        when(xs2aPiisConsentMapper.mapToCmsConsent(request, PSU_ID_DATA, tppInfo))
            .thenReturn(cmsConsent);
        when(cmsCreateConsentResponseService.getCmsCreateConsentResponse(cmsConsent)).thenReturn(Xs2aResponse.<CmsCreateConsentResponse>builder()
                                                                                                     .build());

        //When
        Xs2aResponse<Xs2aCreatePiisConsentResponse> xs2aCreatePiisConsentResponseXs2aResponse = xs2aPiisConsentService.createConsent(request, PSU_ID_DATA, tppInfo);

        //Then
        assertTrue(xs2aCreatePiisConsentResponseXs2aResponse.hasError());
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
        CmsResponse<Boolean> cmsResponse = CmsResponse.<Boolean>builder()
                                               .payload(true)
                                               .build();
        when(consentService.updateConsentStatusById(CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED)).thenReturn(cmsResponse);
        //When
        xs2aPiisConsentService.updateConsentStatus(CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED);
        //Then
        verify(consentService, atLeastOnce()).updateConsentStatusById(CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED);
    }

    @Test
    void updateConsentStatus_WrongChecksumException() throws WrongChecksumException {
        // Given
        when(consentService.updateConsentStatusById(CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED))
            .thenThrow(new WrongChecksumException());

        // When
        xs2aPiisConsentService.updateConsentStatus(CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED);

        // Then
        verify(consentService, times(1)).updateConsentStatusById(CONSENT_ID, ConsentStatus.PARTIALLY_AUTHORISED);
        verify(loggingContextService, never()).storeConsentStatus(any(ConsentStatus.class));
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

    @Test
    void updateAspspAccountAccess() throws WrongChecksumException {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/aspect/account-access.json", AccountAccess.class);

        CmsConsent cmsConsent = new CmsConsent();
        when(aisConsentServiceEncrypted.updateAspspAccountAccess(CONSENT_ID, accountAccess))
            .thenReturn(CmsResponse.<CmsConsent>builder().payload(cmsConsent).build());
        when(xs2aPiisConsentMapper.mapToPiisConsent(cmsConsent)).thenReturn(piisConsent);

        CmsResponse<PiisConsent> actual = xs2aPiisConsentService.updateAspspAccountAccess(CONSENT_ID, accountAccess);

        assertFalse(actual.hasError());
        assertEquals(piisConsent, actual.getPayload());
    }

    @Test
    void updateAspspAccountAccess_checksumError() throws WrongChecksumException {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/aspect/account-access.json", AccountAccess.class);

        when(aisConsentServiceEncrypted.updateAspspAccountAccess(CONSENT_ID, accountAccess))
            .thenThrow(new WrongChecksumException());

        CmsResponse<PiisConsent> actual = xs2aPiisConsentService.updateAspspAccountAccess(CONSENT_ID, accountAccess);

        assertTrue(actual.hasError());
        assertEquals(CmsError.CHECKSUM_ERROR, actual.getError());
    }

    @Test
    void updateAspspAccountAccess_updateError() throws WrongChecksumException {
        AccountAccess accountAccess = jsonReader.getObjectFromFile("json/aspect/account-access.json", AccountAccess.class);

        when(aisConsentServiceEncrypted.updateAspspAccountAccess(CONSENT_ID, accountAccess))
            .thenReturn(CmsResponse.<CmsConsent>builder().error(CmsError.TECHNICAL_ERROR).build());

        CmsResponse<PiisConsent> actual = xs2aPiisConsentService.updateAspspAccountAccess(CONSENT_ID, accountAccess);

        assertTrue(actual.hasError());
        assertEquals(CmsError.TECHNICAL_ERROR, actual.getError());
    }

    @Test
    void updateConsentAuthorization() {
        // Given
        ConsentAuthorisationsParameters updateConsentPsuDataReq = new ConsentAuthorisationsParameters();
        updateConsentPsuDataReq.setAuthorizationId(AUTHORISATION_ID);
        UpdateAuthorisationRequest request = new UpdateAuthorisationRequest();

        when(consentAuthorisationMapper.mapToAuthorisationRequest(updateConsentPsuDataReq))
            .thenReturn(request);

        // When
        xs2aPiisConsentService.updateConsentAuthorisation(updateConsentPsuDataReq);

        // Then
        verify(authorisationService, times(1)).updateAuthorisation(request, AUTHORISATION_ID);
    }

    @Test
    void updateConsentAuthorization_nullValue() {
        // When
        xs2aPiisConsentService.updateConsentAuthorisation(null);

        // Then
        verify(authorisationService, never()).updateAuthorisation(any(), any());
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("Test TppId");
        return tppInfo;
    }
}
