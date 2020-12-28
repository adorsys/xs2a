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

package de.adorsys.psd2.consent.service.mapper;

import de.adorsys.psd2.consent.api.CmsAddress;
import de.adorsys.psd2.consent.api.pis.proto.PisCommonPaymentResponse;
import de.adorsys.psd2.consent.api.pis.proto.PisPaymentInfo;
import de.adorsys.psd2.consent.domain.AuthorisationEntity;
import de.adorsys.psd2.consent.domain.PsuData;
import de.adorsys.psd2.consent.domain.TppInfoEntity;
import de.adorsys.psd2.consent.domain.payment.PisAddress;
import de.adorsys.psd2.consent.domain.payment.PisCommonPaymentData;
import de.adorsys.psd2.consent.domain.payment.PisPaymentData;
import de.adorsys.psd2.xs2a.core.authorisation.Authorisation;
import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.xs2a.reader.JsonReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PisCommonPaymentMapperTest {
    private final static JsonReader jsonReader = new JsonReader();

    @InjectMocks
    private PisCommonPaymentMapper pisCommonPaymentMapper;
    @Mock
    private AccountReferenceMapper accountReferenceMapper;
    @Mock
    private PsuDataMapper psuDataMapper;
    @Mock
    private TppInfoMapper tppInfoMapper;
    @Mock
    private AuthorisationMapper authorisationMapper;
    @Mock
    private CmsAddressMapper cmsAddressMapper;

    @Test
    void mapToPisCommonPaymentResponse() {
        // Given
        TppInfoEntity tppInfoEntity = jsonReader.getObjectFromFile("json/service/mapper/tpp-info-entity.json", TppInfoEntity.class);
        TppInfo tppInfo = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);
        when(tppInfoMapper.mapToTppInfo(tppInfoEntity)).thenReturn(tppInfo);

        PsuData psuDataEntity = jsonReader.getObjectFromFile("json/service/mapper/psu-data.json", PsuData.class);
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        when(psuDataMapper.mapToPsuIdDataList(Collections.singletonList(psuDataEntity)))
            .thenReturn(Collections.singletonList(psuIdData));

        AuthorisationEntity authorisationEntity = jsonReader.getObjectFromFile("json/service/mapper/pis-authorisation.json", AuthorisationEntity.class);
        Authorisation authorisation = jsonReader.getObjectFromFile("json/service/mapper/authorisation.json", Authorisation.class);
        when(authorisationMapper.mapToAuthorisations(Collections.singletonList(authorisationEntity)))
            .thenReturn(Collections.singletonList(authorisation));

        PisCommonPaymentData pisCommonPaymentDataEntity = jsonReader.getObjectFromFile("json/service/mapper/pis-common-payment-data.json", PisCommonPaymentData.class);
        pisCommonPaymentDataEntity.getPayments().forEach(p -> p.setPaymentData(pisCommonPaymentDataEntity));
        PisCommonPaymentResponse expected = jsonReader.getObjectFromFile("json/service/mapper/pis-common-payment-response.json", PisCommonPaymentResponse.class);

        CmsAddress cms = jsonReader.getObjectFromFile("json/service/mapper/cms-address.json", CmsAddress.class);
        when(cmsAddressMapper.mapToCmsAddress(any(PisAddress.class))).thenReturn(cms);

        // When
        Optional<PisCommonPaymentResponse> actual = pisCommonPaymentMapper.mapToPisCommonPaymentResponse(pisCommonPaymentDataEntity, Collections.singletonList(authorisationEntity));

        // Then
        assertTrue(actual.isPresent());
        assertEquals(expected, actual.get());
    }

    @Test
    void mapToPisCommonPaymentResponse_withNullPaymentData_shouldReturnEmpty() {
        // Given
        List<AuthorisationEntity> authorisations = Collections.singletonList(buildAuthorisationEntity());

        // When
        Optional<PisCommonPaymentResponse> actual = pisCommonPaymentMapper.mapToPisCommonPaymentResponse(null, authorisations);

        // Then
        assertFalse(actual.isPresent());
    }

    @Test
    void mapToPisCommonPaymentData() {
        //Given
        PsuData psuDataEntity = jsonReader.getObjectFromFile("json/service/mapper/psu-data.json", PsuData.class);
        PsuIdData psuIdData = jsonReader.getObjectFromFile("json/service/mapper/psu-id-data.json", PsuIdData.class);
        when(psuDataMapper.mapToPsuDataList(Collections.singletonList(psuIdData), null))
            .thenReturn(Collections.singletonList(psuDataEntity));
        TppInfoEntity tppInfoEntity = jsonReader.getObjectFromFile("json/service/mapper/tpp-info-entity.json", TppInfoEntity.class);
        TppInfo tppInfo = jsonReader.getObjectFromFile("json/service/mapper/tpp-info.json", TppInfo.class);
        when(tppInfoMapper.mapToTppInfoEntity(tppInfo)).thenReturn(tppInfoEntity);
        PisPaymentInfo pisPaymentInfo = jsonReader.getObjectFromFile("json/service/mapper/pis-common-payment-info.json", PisPaymentInfo.class);

        //When
        PisCommonPaymentData pisCommonPaymentData = pisCommonPaymentMapper.mapToPisCommonPaymentData(pisPaymentInfo);
        PisCommonPaymentData pisCommonPaymentDataExpected = jsonReader.getObjectFromFile("json/service/mapper/pis-common-payment-data-creation.json", PisCommonPaymentData.class);
        //Then
        assertEquals(pisCommonPaymentDataExpected, pisCommonPaymentData);
    }

    private AuthorisationEntity buildAuthorisationEntity() {
        AuthorisationEntity pisAuthorization = new AuthorisationEntity();
        PisCommonPaymentData pisCommonPaymentData = new PisCommonPaymentData();
        PisPaymentData pisPaymentData = new PisPaymentData();
        pisPaymentData.setPaymentData(pisCommonPaymentData);
        pisPaymentData.setDebtorAccount(null);
        pisCommonPaymentData.setPayments(Collections.singletonList(pisPaymentData));
        pisCommonPaymentData.setCreationTimestamp(OffsetDateTime.now());
        return pisAuthorization;
    }
}
