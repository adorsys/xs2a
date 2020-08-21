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

package de.adorsys.psd2.xs2a.service.context;

import de.adorsys.psd2.xs2a.core.psu.PsuIdData;
import de.adorsys.psd2.xs2a.core.tpp.TppInfo;
import de.adorsys.psd2.xs2a.core.tpp.TppRole;
import de.adorsys.psd2.xs2a.service.RequestProviderService;
import de.adorsys.psd2.xs2a.service.TppService;
import de.adorsys.psd2.xs2a.service.mapper.spi_xs2a_mappers.Xs2aToSpiPsuDataMapper;
import de.adorsys.psd2.xs2a.spi.domain.SpiContextData;
import de.adorsys.psd2.xs2a.spi.domain.psu.SpiPsuData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpiContextDataProviderTest {
    private static final TppInfo TPP_INFO = buildTppInfo();
    private final static UUID X_REQUEST_ID = UUID.fromString("c818a31f-ccdd-4fff-a404-22ad15ba9754");
    private final static UUID INTERNAL_REQUEST_ID = UUID.fromString("b571c834-4eb1-468f-91b0-f5e83589bc22");
    private final static String PSU_IP_ADDRESS = "IP Address";
    private static final String PSU_IP_PORT = "psuIpPort";
    private static final String PSU_USER_AGENT = "psuUserAgent";
    private static final String PSU_GEO_LOCATION = "psuGeoLocation";
    private static final String PSU_ACCEPT = "psuAccept";
    private static final String PSU_ACCEPT_CHARSET = "psuAcceptCharset";
    private static final String PSU_ACCEPT_ENCODING = "psuAcceptEncoding";
    private static final String PSU_ACCEPT_LANGUAGE = "psuAcceptLanguage";
    private static final String PSU_HTTP_METHOD = "psuHttpMethod";
    private static final UUID PSU_DEVICE_ID = UUID.randomUUID();
    private static final PsuIdData PSU_DATA = new PsuIdData("psuId", "psuIdType", "psuCorporateId", "psuCorporateIdType", "psuIpAddress");
    private static final SpiPsuData SPI_PSU_DATA = SpiPsuData.builder()
                                                       .psuId("psuId")
                                                       .psuIdType("psuIdType")
                                                       .psuCorporateId("psuCorporateId")
                                                       .psuCorporateIdType("psuCorporateIdType")
                                                       .psuIpAddress(PSU_IP_ADDRESS)
                                                       .psuIpPort(PSU_IP_PORT)
                                                       .psuUserAgent(PSU_USER_AGENT)
                                                       .psuGeoLocation(PSU_GEO_LOCATION)
                                                       .psuAccept(PSU_ACCEPT)
                                                       .psuAcceptCharset(PSU_ACCEPT_CHARSET)
                                                       .psuAcceptEncoding(PSU_ACCEPT_ENCODING)
                                                       .psuAcceptLanguage(PSU_ACCEPT_LANGUAGE)
                                                       .psuHttpMethod(PSU_HTTP_METHOD)
                                                       .psuDeviceId(PSU_DEVICE_ID)
                                                       .build();
    private static final SpiContextData SPI_CONTEXT_DATA = buildSpiContextData(null);
    private static final SpiContextData SPI_CONTEXT_DATA_WITH_PSU_DATA = buildSpiContextData(SPI_PSU_DATA);
    private static final String AUTHORISATION = "Bearer 1111111";
    private static final String TPP_BRAND_LOGGING_INFORMATION = "tppBrandLoggingInformation";

    @InjectMocks
    private SpiContextDataProvider spiContextDataProvider;

    @Mock
    private TppService tppService;
    @Mock
    private Xs2aToSpiPsuDataMapper psuDataMapper;
    @Mock
    private RequestProviderService requestProviderService;

    @BeforeEach
    void setUp() {
        when(requestProviderService.getRequestId()).thenReturn(X_REQUEST_ID);
        when(requestProviderService.getInternalRequestId()).thenReturn(INTERNAL_REQUEST_ID);
        when(requestProviderService.getOAuth2Token()).thenReturn(AUTHORISATION);
        when(requestProviderService.getTppBrandLoggingInformationHeader()).thenReturn(TPP_BRAND_LOGGING_INFORMATION);
        when(requestProviderService.getTppRejectionNoFundsPreferred()).thenReturn(null);
    }

    @Test
    void provide_success() {
        //Given
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);

        //When
        SpiContextData actualResponse = spiContextDataProvider.provide();

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(SPI_CONTEXT_DATA);
    }

    @Test
    void provideWithPsuIdData_success() {
        //Given
        when(tppService.getTppInfo())
            .thenReturn(TPP_INFO);

        //When
        SpiContextData actualResponse = spiContextDataProvider.provideWithPsuIdData(PSU_DATA);

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(SPI_CONTEXT_DATA);
    }

    @Test
    void provide_withParameters_success() {
        //Given
        when(psuDataMapper.mapToSpiPsuData(PSU_DATA))
            .thenReturn(SPI_PSU_DATA);

        //When
        SpiContextData actualResponse = spiContextDataProvider.provide(PSU_DATA, TPP_INFO);

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse).isEqualTo(SPI_CONTEXT_DATA_WITH_PSU_DATA);
    }

    @Test
    void provide_withPsuIpAddress_success() {
        //Given
        when(psuDataMapper.mapToSpiPsuData(PSU_DATA))
            .thenReturn(SPI_PSU_DATA);

        //When
        SpiContextData actualResponse = spiContextDataProvider.provide(PSU_DATA, TPP_INFO);

        //Then
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getPsuData()).isEqualTo(SPI_PSU_DATA);
    }

    private static TppInfo buildTppInfo() {
        TppInfo tppInfo = new TppInfo();
        tppInfo.setAuthorisationNumber("registrationNumber");
        tppInfo.setTppName("tppName");
        tppInfo.setTppRoles(Collections.singletonList(TppRole.PISP));
        tppInfo.setAuthorityId("authorityId");
        return tppInfo;
    }

    private static SpiContextData buildSpiContextData(SpiPsuData spiPsuData) {
        return new SpiContextData(spiPsuData, TPP_INFO, X_REQUEST_ID, INTERNAL_REQUEST_ID, AUTHORISATION, TPP_BRAND_LOGGING_INFORMATION, null);
    }
}
