/*
 * Copyright 2018-2019 adorsys GmbH & Co KG
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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRedirectUri;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class TppInfoMapperTest {
    private static final String AUTHORISATION_NUMBER = "authorisation number";
    private static final String TPP_NAME = "TPP name";
    private static final List<TppRole> TPP_ROLES = Arrays.asList(TppRole.AISP, TppRole.PISP, TppRole.PIISP);
    private static final String AUTHORITY_ID = "authority id";
    private static final String AUTHORITY_NAME = "authority name";
    private static final String COUNTRY = "some country";
    private static final String ORGANISATION = "some organisation";
    private static final String ORGANISATION_UNIT = "organisation unit";
    private static final String CITY = "some city";
    private static final String STATE = "some state";
    private static final String REDIRECT_URI = "redirect uri";
    private static final String NOK_REDIRECT_URI = "nok redirect uri";

    @InjectMocks
    private TppInfoMapper tppInfoMapper;

    @Test
    public void mapToTppInfoEntity() {
        // Given
        TppInfo tppInfo = buildTppInfo();

        // When
        TppInfoEntity mappedEntity = tppInfoMapper.mapToTppInfoEntity(tppInfo);

        // Then
        assertNotNull(mappedEntity);
        assertEquals(AUTHORISATION_NUMBER, mappedEntity.getAuthorisationNumber());
        assertEquals(TPP_NAME, mappedEntity.getTppName());
        assertEquals(TPP_ROLES, mappedEntity.getTppRoles());
        assertEquals(AUTHORITY_ID, mappedEntity.getAuthorityId());
        assertEquals(AUTHORITY_NAME, mappedEntity.getAuthorityName());
        assertEquals(COUNTRY, mappedEntity.getCountry());
        assertEquals(ORGANISATION, mappedEntity.getOrganisation());
        assertEquals(ORGANISATION_UNIT, mappedEntity.getOrganisationUnit());
        assertEquals(CITY, mappedEntity.getCity());
        assertEquals(STATE, mappedEntity.getState());
        assertEquals(REDIRECT_URI, mappedEntity.getRedirectUri());
        assertEquals(NOK_REDIRECT_URI, mappedEntity.getNokRedirectUri());
    }

    @Test
    public void mapToTppInfoEntity_withNullTppRedirectUri_shouldNotMapTppRedirectUris() {
        // Given
        TppInfo tppInfo = buildTppInfoWithoutUris();

        // When
        TppInfoEntity mappedEntity = tppInfoMapper.mapToTppInfoEntity(tppInfo);

        // Then
        assertNotNull(mappedEntity);
        assertEquals(AUTHORISATION_NUMBER, mappedEntity.getAuthorisationNumber());
        assertEquals(TPP_NAME, mappedEntity.getTppName());
        assertEquals(TPP_ROLES, mappedEntity.getTppRoles());
        assertEquals(AUTHORITY_ID, mappedEntity.getAuthorityId());
        assertEquals(AUTHORITY_NAME, mappedEntity.getAuthorityName());
        assertEquals(COUNTRY, mappedEntity.getCountry());
        assertEquals(ORGANISATION, mappedEntity.getOrganisation());
        assertEquals(ORGANISATION_UNIT, mappedEntity.getOrganisationUnit());
        assertEquals(CITY, mappedEntity.getCity());
        assertEquals(STATE, mappedEntity.getState());

        assertNull(mappedEntity.getRedirectUri());
        assertNull(mappedEntity.getNokRedirectUri());
    }

    @Test
    public void mapToTppInfoEntity_withNullTppRoles_shouldMapRolesToEmptyList() {
        // Given
        TppInfo tppInfo = buildTppInfoWithoutRoles();

        // When
        TppInfoEntity mappedEntity = tppInfoMapper.mapToTppInfoEntity(tppInfo);

        // Then
        assertNotNull(mappedEntity);
        assertNotNull(mappedEntity.getTppRoles());
        assertEquals(Collections.emptyList(), mappedEntity.getTppRoles());
    }

    @Test
    public void mapToTppInfoEntity_withNullTppInfo_shouldReturnNull() {
        // Given
        TppInfoEntity mappedEntity = tppInfoMapper.mapToTppInfoEntity(null);

        // Then
        assertNull(mappedEntity);
    }

    @Test
    public void mapToTppInfo() {
        // Given
        TppInfoEntity tppInfoEntity = buildTppInfoEntity();

        // When
        TppInfo mappedTppInfo = tppInfoMapper.mapToTppInfo(tppInfoEntity);

        // Then
        assertNotNull(mappedTppInfo);
        assertEquals(AUTHORISATION_NUMBER, mappedTppInfo.getAuthorisationNumber());
        assertEquals(TPP_NAME, mappedTppInfo.getTppName());
        assertEquals(TPP_ROLES, mappedTppInfo.getTppRoles());
        assertEquals(AUTHORITY_ID, mappedTppInfo.getAuthorityId());
        assertEquals(AUTHORITY_NAME, mappedTppInfo.getAuthorityName());
        assertEquals(COUNTRY, mappedTppInfo.getCountry());
        assertEquals(ORGANISATION, mappedTppInfo.getOrganisation());
        assertEquals(ORGANISATION_UNIT, mappedTppInfo.getOrganisationUnit());
        assertEquals(CITY, mappedTppInfo.getCity());
        assertEquals(STATE, mappedTppInfo.getState());

        TppRedirectUri tppRedirectUri = mappedTppInfo.getTppRedirectUri();
        assertNotNull(tppRedirectUri);
        assertEquals(REDIRECT_URI, tppRedirectUri.getUri());
        assertEquals(NOK_REDIRECT_URI, tppRedirectUri.getNokUri());
    }

    @Test
    public void mapToTppInfo_withoutLinks_shouldNotMapRedirectUri() {
        // Given
        TppInfoEntity tppInfoEntity = buildTppInfoEntity();

        // When
        TppInfo mappedTppInfo = tppInfoMapper.mapToTppInfo(tppInfoEntity);

        // Then
        assertNotNull(mappedTppInfo);
        assertEquals(AUTHORISATION_NUMBER, mappedTppInfo.getAuthorisationNumber());
        assertEquals(TPP_NAME, mappedTppInfo.getTppName());
        assertEquals(TPP_ROLES, mappedTppInfo.getTppRoles());
        assertEquals(AUTHORITY_ID, mappedTppInfo.getAuthorityId());
        assertEquals(AUTHORITY_NAME, mappedTppInfo.getAuthorityName());
        assertEquals(COUNTRY, mappedTppInfo.getCountry());
        assertEquals(ORGANISATION, mappedTppInfo.getOrganisation());
        assertEquals(ORGANISATION_UNIT, mappedTppInfo.getOrganisationUnit());
        assertEquals(CITY, mappedTppInfo.getCity());
        assertEquals(STATE, mappedTppInfo.getState());

        TppRedirectUri tppRedirectUri = mappedTppInfo.getTppRedirectUri();
        assertNotNull(tppRedirectUri);
        assertEquals(REDIRECT_URI, tppRedirectUri.getUri());
        assertEquals(NOK_REDIRECT_URI, tppRedirectUri.getNokUri());
    }

    @Test
    public void mapToTppInfo_withNullTppRoles_shouldMapRolesToEmptyList() {
        // Given
        TppInfoEntity tppInfoEntity = buildTppInfoEntityWithoutRoles();

        // When
        TppInfo mappedTppInfo = tppInfoMapper.mapToTppInfo(tppInfoEntity);

        // Then
        assertNotNull(mappedTppInfo);
        assertNotNull(mappedTppInfo.getTppRoles());
        assertEquals(Collections.emptyList(), mappedTppInfo.getTppRoles());
    }

    @Test
    public void mapToTppInfo_withNullTppInfoEntity_shouldReturnNull() {
        // When
        TppInfo mappedTppInfo = tppInfoMapper.mapToTppInfo(null);

        // Then
        assertNull(mappedTppInfo);
    }

    private TppInfo buildTppInfo() {
        TppInfo tppInfo = buildTppInfoWithoutUris();
        tppInfo.setTppRedirectUri(new TppRedirectUri(REDIRECT_URI, NOK_REDIRECT_URI));

        return tppInfo;
    }

    private TppInfo buildTppInfoWithoutUris() {
        TppInfo tppInfo = new TppInfo();

        tppInfo.setAuthorisationNumber(AUTHORISATION_NUMBER);
        tppInfo.setTppName(TPP_NAME);
        tppInfo.setTppRoles(TPP_ROLES);
        tppInfo.setAuthorityId(AUTHORITY_ID);
        tppInfo.setAuthorityName(AUTHORITY_NAME);
        tppInfo.setCountry(COUNTRY);
        tppInfo.setOrganisation(ORGANISATION);
        tppInfo.setOrganisationUnit(ORGANISATION_UNIT);
        tppInfo.setCity(CITY);
        tppInfo.setState(STATE);

        return tppInfo;
    }

    private TppInfo buildTppInfoWithoutRoles() {
        return new TppInfo();
    }

    private TppInfoEntity buildTppInfoEntity() {
        TppInfoEntity tppInfoEntity = buildTppInfoEntityWithoutUris();

        tppInfoEntity.setRedirectUri(REDIRECT_URI);
        tppInfoEntity.setNokRedirectUri(NOK_REDIRECT_URI);

        return tppInfoEntity;
    }

    private TppInfoEntity buildTppInfoEntityWithoutUris() {
        TppInfoEntity tppInfoEntity = new TppInfoEntity();

        tppInfoEntity.setAuthorisationNumber(AUTHORISATION_NUMBER);
        tppInfoEntity.setTppName(TPP_NAME);
        tppInfoEntity.setTppRoles(TPP_ROLES);
        tppInfoEntity.setAuthorityId(AUTHORITY_ID);
        tppInfoEntity.setAuthorityName(AUTHORITY_NAME);
        tppInfoEntity.setCountry(COUNTRY);
        tppInfoEntity.setOrganisation(ORGANISATION);
        tppInfoEntity.setOrganisationUnit(ORGANISATION_UNIT);
        tppInfoEntity.setCity(CITY);
        tppInfoEntity.setState(STATE);

        return tppInfoEntity;
    }

    private TppInfoEntity buildTppInfoEntityWithoutRoles() {
        return new TppInfoEntity();
    }
}
