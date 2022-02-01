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

package de.adorsys.psd2.consent.service.psu;

import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsConsent;
import de.adorsys.psd2.consent.api.piis.v2.CmsConfirmationOfFundsResponse;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.consent.ConsentEntity;
import de.adorsys.psd2.consent.psu.api.CmsPsuConfirmationOfFundsAuthorisation;
import de.adorsys.psd2.consent.repository.ConsentJpaRepository;
import de.adorsys.psd2.consent.repository.specification.ConfirmationOfFundsConsentSpecification;
import de.adorsys.psd2.consent.service.authorisation.CmsConsentAuthorisationServiceInternal;
import de.adorsys.psd2.consent.service.mapper.*;
import de.adorsys.psd2.core.mapper.ConsentDataMapper;
import de.adorsys.psd2.xs2a.core.consent.ConsentStatus;
import de.adorsys.psd2.xs2a.core.consent.ConsentType;
import de.adorsys.psd2.xs2a.core.exception.AuthorisationIsExpiredException;
import de.adorsys.psd2.xs2a.core.exception.RedirectUrlIsExpiredException;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.sca.AuthenticationDataHolder;
import de.adorsys.psd2.xs2a.core.sca.ScaStatus;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CmsPsuConfirmationOfFundsServiceInternalTest {
    private static final String CONSENT_ID = "4b112130-6a96-4941-a220-2da8a4af2c65";
    private static final String AUTHORISATION_ID = "9304a6a0-8f02-4b79-aeab-00aa7e03a06d";
    private static final String INSTANCE_ID = "UNDEFINED";
    private static final String METHOD_ID = "SMS";
    private static final String AUTHENTICATION_DATA = "123456";
    private static final String TPP_NOK_REDIRECT_URI = "Mock tppNokRedirectUri";

    private AuthenticationDataHolder authenticationDataHolder;

    @InjectMocks
    private CmsPsuConfirmationOfFundsServiceInternal cmsPsuConfirmationOfFundsServiceInternal;

    @Mock
    private CmsConsentAuthorisationServiceInternal consentAuthorisationService;
    @Mock
    private ConfirmationOfFundsConsentSpecification confirmationOfFundsConsentSpecification;
    @Mock
    private CmsPsuConsentServiceInternal cmsPsuConsentServiceInternal;
    @Mock
    private ConsentJpaRepository consentJpaRepository;
    @Mock
    private CmsPsuAuthorisationMapper cmsPsuAuthorisationMapper;
    @Mock
    private CmsPsuConfirmationOfFundsAuthorisation cmsPsuConfirmationOfFundsAuthorisation;

    private CmsConfirmationOfFundsMapper confirmationOfFundsMapper;


    private JsonReader jsonReader = new JsonReader();
    private PsuIdData psuIdData;
    private ConsentEntity consentEntity;
    private AuthorisationEntity authorisationEntity;

    @BeforeEach
    void setUp() {
        psuIdData = jsonReader.getObjectFromFile("json/psu-id-data.json", PsuIdData.class);
        authenticationDataHolder = new AuthenticationDataHolder(METHOD_ID, AUTHENTICATION_DATA);
        consentEntity = jsonReader.getObjectFromFile("json/consent-entity.json", ConsentEntity.class);
        authorisationEntity = jsonReader.getObjectFromFile("json/authorisation-entity.json", AuthorisationEntity.class);

        confirmationOfFundsMapper = new CmsConfirmationOfFundsMapper(new PsuDataMapper(), new TppInfoMapperImpl(),
                                                                     new AuthorisationTemplateMapperImpl(),
                                                                     new ConsentDataMapper());
        cmsPsuConfirmationOfFundsServiceInternal = new CmsPsuConfirmationOfFundsServiceInternal(consentJpaRepository, consentAuthorisationService,
                                                                                                confirmationOfFundsConsentSpecification, confirmationOfFundsMapper,
                                                                                                cmsPsuConsentServiceInternal, cmsPsuAuthorisationMapper);
    }

    @Test
    void updateAuthorisationStatus() throws AuthorisationIsExpiredException {
        when(confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));
        when(consentAuthorisationService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(authorisationEntity));
        when(consentAuthorisationService.updateScaStatusAndAuthenticationData(ScaStatus.RECEIVED, authorisationEntity, authenticationDataHolder))
            .thenReturn(true);

        boolean result = cmsPsuConfirmationOfFundsServiceInternal.updateAuthorisationStatus(psuIdData, CONSENT_ID,
                                                                                            AUTHORISATION_ID, ScaStatus.RECEIVED,
                                                                                            INSTANCE_ID, authenticationDataHolder);
        assertTrue(result);
    }

    @Test
    void updateAuthorisationStatus_getAuthorisationByExternalId_empty() throws AuthorisationIsExpiredException {
        // Given
        when(confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));
        when(consentAuthorisationService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());

        // When
        boolean result = cmsPsuConfirmationOfFundsServiceInternal.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED,
                                                                                            INSTANCE_ID, authenticationDataHolder);

        assertFalse(result);
        verify(consentAuthorisationService, never()).updateScaStatusAndAuthenticationData(any(), any(), any());
    }

    @Test
    void updateAuthorisationStatus_getActualConsent_empty() throws AuthorisationIsExpiredException {
        // Given
        when(confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());

        // When
        boolean result = cmsPsuConfirmationOfFundsServiceInternal.updateAuthorisationStatus(psuIdData, CONSENT_ID, AUTHORISATION_ID, ScaStatus.RECEIVED,
                                                                                            INSTANCE_ID, authenticationDataHolder);

        assertFalse(result);
        verify(consentAuthorisationService, never()).getAuthorisationByAuthorisationId(any(), any());
        verify(consentAuthorisationService, never()).updateScaStatusAndAuthenticationData(any(), any(), any());
    }

    @Test
    void checkRedirectAndGetConsent_success() throws RedirectUrlIsExpiredException {
        // Given
        when(consentAuthorisationService.getAuthorisationByRedirectId(AUTHORISATION_ID, INSTANCE_ID)).thenReturn(Optional.of(authorisationEntity));
        when(consentJpaRepository.findByExternalId(CONSENT_ID)).thenReturn(Optional.of(consentEntity));

        List<AuthorisationEntity> authorisations = Collections.singletonList(authorisationEntity);
        when(consentAuthorisationService.getAuthorisationsByParentExternalId(CONSENT_ID))
            .thenReturn(authorisations);

        // When
        Optional<CmsConfirmationOfFundsResponse> consentResponseOptional = cmsPsuConfirmationOfFundsServiceInternal.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);

        // Then
        assertTrue(consentResponseOptional.isPresent());

        CmsConfirmationOfFundsResponse expected = jsonReader.getObjectFromFile("json/service/psu/piis/confirmation-of-funds-response.json", CmsConfirmationOfFundsResponse.class);
        expected.getConsent().setCreationTimestamp(consentResponseOptional.get().getConsent().getCreationTimestamp());
        assertEquals(expected, consentResponseOptional.get());
    }

    @Test
    void checkRedirectAndGetConsent_authorisationNotFound() throws RedirectUrlIsExpiredException {
        // Given
        when(consentAuthorisationService.getAuthorisationByRedirectId(AUTHORISATION_ID, INSTANCE_ID)).thenReturn(Optional.empty());

        // When
        Optional<CmsConfirmationOfFundsResponse> consentResponseOptional = cmsPsuConfirmationOfFundsServiceInternal.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID);

        // Then
        assertTrue(consentResponseOptional.isEmpty());

        verify(consentJpaRepository, never()).findByExternalId(anyString());
        verify(consentAuthorisationService, never()).getAuthorisationsByParentExternalId(anyString());
    }

    @Test
    void checkRedirectAndGetConsent_redirectUrlIsExpired() throws RedirectUrlIsExpiredException {
        // Given
        when(consentAuthorisationService.getAuthorisationByRedirectId(AUTHORISATION_ID, INSTANCE_ID))
            .thenThrow(new RedirectUrlIsExpiredException(TPP_NOK_REDIRECT_URI));

        // When
        assertThrows(RedirectUrlIsExpiredException.class,
                     () -> cmsPsuConfirmationOfFundsServiceInternal.checkRedirectAndGetConsent(AUTHORISATION_ID, INSTANCE_ID));

        verify(consentJpaRepository, never()).findByExternalId(anyString());
        verify(consentAuthorisationService, never()).getAuthorisationsByParentExternalId(anyString());
    }

    @Test
    void updatePsuDataInConsent_success() throws AuthorisationIsExpiredException {
        //Given
        when(consentAuthorisationService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(authorisationEntity));
        when(cmsPsuConsentServiceInternal.updatePsuData(authorisationEntity, psuIdData, ConsentType.PIIS_ASPSP))
            .thenReturn(true);
        //When
        boolean updatePsuDataInConsent = cmsPsuConfirmationOfFundsServiceInternal.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, INSTANCE_ID);
        //Then
        assertTrue(updatePsuDataInConsent);
    }

    @Test
    void updatePsuDataInConsent_authorisationNotFound() throws AuthorisationIsExpiredException {
        //Given
        when(consentAuthorisationService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());
        //When
        boolean updatePsuDataInConsent = cmsPsuConfirmationOfFundsServiceInternal.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, INSTANCE_ID);
        //Then
        assertFalse(updatePsuDataInConsent);
    }

    @Test
    void updatePsuDataInConsent_updateFailed() throws AuthorisationIsExpiredException {
        //Given
        when(consentAuthorisationService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(authorisationEntity));
        when(cmsPsuConsentServiceInternal.updatePsuData(authorisationEntity, psuIdData, ConsentType.PIIS_ASPSP))
            .thenReturn(false);
        //When
        boolean updatePsuDataInConsent = cmsPsuConfirmationOfFundsServiceInternal.updatePsuDataInConsent(psuIdData, AUTHORISATION_ID, INSTANCE_ID);
        //Then
        assertFalse(updatePsuDataInConsent);
    }

    @Test
    void getAuthorisationByAuthorisationId()  throws AuthorisationIsExpiredException {
        //Given
        when(consentAuthorisationService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.of(authorisationEntity));
        when(cmsPsuAuthorisationMapper.mapToCmsPsuConfirmationOfFundsAuthorisation(authorisationEntity))
            .thenReturn(cmsPsuConfirmationOfFundsAuthorisation);
        //When
        Optional<CmsPsuConfirmationOfFundsAuthorisation> authorisationByAuthorisationId = cmsPsuConfirmationOfFundsServiceInternal.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);
        //Then
        assertTrue(authorisationByAuthorisationId.isPresent());
        assertEquals(cmsPsuConfirmationOfFundsAuthorisation, authorisationByAuthorisationId.get());
    }

    @Test
    void getAuthorisationByAuthorisationId_authorisationNotFound()  throws AuthorisationIsExpiredException {
        //Given
        when(consentAuthorisationService.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID))
            .thenReturn(Optional.empty());
        //When
        Optional<CmsPsuConfirmationOfFundsAuthorisation> authorisationByAuthorisationId = cmsPsuConfirmationOfFundsServiceInternal.getAuthorisationByAuthorisationId(AUTHORISATION_ID, INSTANCE_ID);
        //Then
        assertTrue(authorisationByAuthorisationId.isEmpty());
    }

    @Test
    void updateConsentStatus() {
        //Given
        when(confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));

        ConsentStatus consentStatus = ConsentStatus.VALID;
        //When
        boolean result = cmsPsuConfirmationOfFundsServiceInternal.updateConsentStatus(CONSENT_ID, consentStatus, INSTANCE_ID);
        //Then
        assertTrue(result);
        assertEquals(consentStatus, consentEntity.getConsentStatus());
    }

    @Test
    void updateConsentStatus_getActualConsent_empty() {
        //Given
        when(confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());
        ConsentStatus consentStatus = ConsentStatus.VALID;
        //When
        boolean result = cmsPsuConfirmationOfFundsServiceInternal.updateConsentStatus(CONSENT_ID, consentStatus, INSTANCE_ID);
        //Then
        assertFalse(result);
        assertNotEquals(consentStatus, consentEntity.getConsentStatus());
    }
    @Test
    void getConsent() {
        when(confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.of(consentEntity));

        List<AuthorisationEntity> authorisations = Collections.singletonList(authorisationEntity);
        when(consentAuthorisationService.getAuthorisationsByParentExternalId(CONSENT_ID))
            .thenReturn(authorisations);

        Optional<CmsConfirmationOfFundsConsent> actual = cmsPsuConfirmationOfFundsServiceInternal.getConsent(psuIdData, CONSENT_ID, INSTANCE_ID);

        assertTrue(actual.isPresent());

        CmsConfirmationOfFundsConsent expected = jsonReader.getObjectFromFile("json/service/psu/piis/confirmation-of-funds-consent.json",
                                                                              CmsConfirmationOfFundsConsent.class);

        expected.setCreationTimestamp(actual.get().getCreationTimestamp());
        assertEquals(expected, actual.get());
    }

    @Test
    void getConsent_consentIsNotFound() {
        when(confirmationOfFundsConsentSpecification.byConsentIdAndInstanceId(CONSENT_ID, INSTANCE_ID))
            .thenReturn((root, criteriaQuery, criteriaBuilder) -> null);
        when(consentJpaRepository.findOne(any(Specification.class)))
            .thenReturn(Optional.empty());

        Optional<CmsConfirmationOfFundsConsent> actual = cmsPsuConfirmationOfFundsServiceInternal.getConsent(psuIdData, CONSENT_ID, INSTANCE_ID);

        assertTrue(actual.isEmpty());

        verify(consentAuthorisationService, never()).getAuthorisationsByParentExternalId(anyString());
    }
}
